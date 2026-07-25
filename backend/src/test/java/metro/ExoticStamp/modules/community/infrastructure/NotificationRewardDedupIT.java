package metro.ExoticStamp.modules.community.infrastructure;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves V23 {@code uq_notifications_user_type_ref} dedup for reward-issued notifications (Batch E.2 Phase 4).
 */
@Testcontainers(disabledWithoutDocker = true)
class NotificationRewardDedupIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @BeforeAll
    static void migrate() {
        Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    @Test
    void rewardIssued_nullReferenceIdBypassesUniqueIndex_appMustNotUseNull() throws Exception {
        UUID userId = seedUser();
        try (Connection c = postgres.createConnection(""); Statement s = c.createStatement()) {
            s.execute("""
                    INSERT INTO notifications (id, user_id, type, title, body, reference_id, created_at)
                    VALUES ('%s', '%s', 'REWARD', 't', 'b', NULL, NOW())
                    """.formatted(UUID.randomUUID(), userId));
            s.execute("""
                    INSERT INTO notifications (id, user_id, type, title, body, reference_id, created_at)
                    VALUES ('%s', '%s', 'REWARD', 't2', 'b2', NULL, NOW())
                    """.formatted(UUID.randomUUID(), userId));
            ResultSet rs = s.executeQuery(
                    "SELECT COUNT(*) FROM notifications WHERE user_id = '%s' AND type = 'REWARD' AND reference_id IS NULL"
                            .formatted(userId));
            assertTrue(rs.next());
            assertEquals(2, rs.getInt(1),
                    "null reference_id bypasses unique index — app must always set reward id for REWARD");
        }
    }

    @Test
    void duplicateRewardReference_rejectedByUniqueIndex() throws Exception {
        UUID userId = seedUser();
        UUID rewardId = UUID.randomUUID();
        try (Connection c = postgres.createConnection(""); Statement s = c.createStatement()) {
            s.execute("""
                    INSERT INTO notifications (id, user_id, type, title, body, reference_id, created_at)
                    VALUES ('%s', '%s', 'REWARD', 'New reward earned', 'body', '%s', NOW())
                    """.formatted(UUID.randomUUID(), userId, rewardId));
            assertThrows(SQLException.class, () -> s.execute("""
                    INSERT INTO notifications (id, user_id, type, title, body, reference_id, created_at)
                    VALUES ('%s', '%s', 'REWARD', 'New reward earned', 'body', '%s', NOW())
                    """.formatted(UUID.randomUUID(), userId, rewardId)));
            ResultSet rs = s.executeQuery(
                    "SELECT COUNT(*) FROM notifications WHERE user_id = '%s' AND type = 'REWARD' AND reference_id = '%s'"
                            .formatted(userId, rewardId));
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void concurrentInsertSameRewardRef_oneWinner() throws Exception {
        UUID userId = seedUser();
        UUID rewardId = UUID.randomUUID();
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger wins = new AtomicInteger();
        AtomicInteger losses = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 2; i++) {
            pool.submit(() -> {
                try {
                    start.await(5, TimeUnit.SECONDS);
                    try (Connection c = postgres.createConnection("");
                         PreparedStatement ps = c.prepareStatement("""
                                 INSERT INTO notifications (id, user_id, type, title, body, reference_id, created_at)
                                 VALUES (?, ?, 'REWARD', 'New reward earned', 'body', ?, ?)
                                 """)) {
                        ps.setObject(1, UUID.randomUUID());
                        ps.setObject(2, userId);
                        ps.setString(3, rewardId.toString());
                        ps.setObject(4, LocalDateTime.now());
                        ps.executeUpdate();
                        wins.incrementAndGet();
                    }
                } catch (Exception e) {
                    losses.incrementAndGet();
                }
            });
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(20, TimeUnit.SECONDS));
        assertEquals(1, wins.get());
        assertEquals(1, losses.get());
        try (Connection c = postgres.createConnection(""); Statement s = c.createStatement()) {
            ResultSet rs = s.executeQuery(
                    "SELECT COUNT(*) FROM notifications WHERE user_id = '%s' AND reference_id = '%s'"
                            .formatted(userId, rewardId));
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
    }

    private UUID seedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        try (Connection c = postgres.createConnection("");
             PreparedStatement ps = c.prepareStatement("""
                     INSERT INTO users (id, username, email, phone_number, password, status, token_version, created_at)
                     VALUES (?, ?, ?, ?, ?, 'ACTIVE', 0, ?)
                     """)) {
            ps.setObject(1, userId);
            ps.setString(2, "n-" + userId.toString().substring(0, 8));
            ps.setString(3, "n-" + userId.toString().substring(0, 8) + "@example.com");
            ps.setString(4, "+1777" + userId.toString().replace("-", "").substring(0, 7));
            ps.setString(5, "hashed");
            ps.setObject(6, LocalDateTime.now());
            ps.executeUpdate();
        }
        return userId;
    }
}
