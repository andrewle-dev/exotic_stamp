package metro.ExoticStamp.infrastructure;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
class FlywayV23MigrationIT {

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
    void appliesV23_fingerprintAndNotificationDedup() throws Exception {
        try (Connection c = postgres.createConnection(""); Statement s = c.createStatement()) {
            assertTrue(s.executeQuery("SELECT 1 FROM flyway_schema_history WHERE version = '23'").next());
            ResultSet col = s.executeQuery("""
                    SELECT 1 FROM information_schema.columns
                    WHERE table_name = 'user_stamps' AND column_name = 'idempotency_fingerprint'
                    """);
            assertTrue(col.next());
            assertTrue(s.executeQuery("SELECT 1 FROM pg_indexes WHERE indexname = 'uq_notifications_user_type_ref'").next());
            assertTrue(s.executeQuery("SELECT 1 FROM pg_indexes WHERE indexname = 'idx_user_rewards_pending_stock'").next());
        }
    }

    @Test
    void campaignDefaultInvariant_allowsNonDefaults_rejectsSecondActiveDefault() throws Exception {
        try (Connection c = postgres.createConnection(""); Statement s = c.createStatement()) {
            UUID lineId = UUID.randomUUID();
            s.execute("""
                    INSERT INTO lines (id, code, name, display_name, total_stations, status, sort_order)
                    VALUES ('%s', 'L23', 'L23', 'L23', 1, 'ACTIVE', 0)
                    """.formatted(lineId));
            // two non-default active campaigns allowed
            for (String code : new String[]{"A", "B"}) {
                s.execute("""
                        INSERT INTO campaigns (id, code, name, display_name, campaign_type, status,
                            start_at, end_at, priority, line_id, is_default)
                        VALUES ('%s', '%s', '%s', '%s', 'STANDARD', 'ACTIVE',
                            NOW(), NOW() + INTERVAL '1 year', 0, '%s', FALSE)
                        """.formatted(UUID.randomUUID(), code, code, code, lineId));
            }
            UUID def1 = UUID.randomUUID();
            s.execute("""
                    INSERT INTO campaigns (id, code, name, display_name, campaign_type, status,
                        start_at, end_at, priority, line_id, is_default)
                    VALUES ('%s', 'DEF1', 'DEF1', 'DEF1', 'STANDARD', 'ACTIVE',
                        NOW(), NOW() + INTERVAL '1 year', 0, '%s', TRUE)
                    """.formatted(def1, lineId));
            assertThrows(SQLException.class, () -> s.execute("""
                    INSERT INTO campaigns (id, code, name, display_name, campaign_type, status,
                        start_at, end_at, priority, line_id, is_default)
                    VALUES ('%s', 'DEF2', 'DEF2', 'DEF2', 'STANDARD', 'ACTIVE',
                        NOW(), NOW() + INTERVAL '1 year', 0, '%s', TRUE)
                    """.formatted(UUID.randomUUID(), lineId)));
            // soft-deleted default does not block replacement (historical is_default may remain true)
            s.execute("UPDATE campaigns SET deleted_at = NOW() WHERE id = '%s'".formatted(def1));
            s.execute("""
                    INSERT INTO campaigns (id, code, name, display_name, campaign_type, status,
                        start_at, end_at, priority, line_id, is_default)
                    VALUES ('%s', 'DEF3', 'DEF3', 'DEF3', 'STANDARD', 'ACTIVE',
                        NOW(), NOW() + INTERVAL '1 year', 0, '%s', TRUE)
                    """.formatted(UUID.randomUUID(), lineId));
        }
    }

    @Test
    void concurrentActiveDefault_oneWinner() throws Exception {
        UUID lineId = UUID.randomUUID();
        try (Connection setup = postgres.createConnection(""); Statement s = setup.createStatement()) {
            s.execute("""
                    INSERT INTO lines (id, code, name, display_name, total_stations, status, sort_order)
                    VALUES ('%s', 'LC', 'LC', 'LC', 1, 'ACTIVE', 0)
                    """.formatted(lineId));
        }
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger wins = new AtomicInteger();
        AtomicInteger losses = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 2; i++) {
            int n = i;
            pool.submit(() -> {
                try {
                    start.await(5, TimeUnit.SECONDS);
                    try (Connection c = postgres.createConnection(""); Statement s = c.createStatement()) {
                        s.execute("""
                                INSERT INTO campaigns (id, code, name, display_name, campaign_type, status,
                                    start_at, end_at, priority, line_id, is_default)
                                VALUES ('%s', 'C%d', 'C%d', 'C%d', 'STANDARD', 'ACTIVE',
                                    NOW(), NOW() + INTERVAL '1 year', 0, '%s', TRUE)
                                """.formatted(UUID.randomUUID(), n, n, n, lineId));
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
    }
}
