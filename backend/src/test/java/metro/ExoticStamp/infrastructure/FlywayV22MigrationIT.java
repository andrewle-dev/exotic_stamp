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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class FlywayV22MigrationIT {

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
    void appliesV22_andCreatesIntegrityIndexes() throws Exception {
        try (Connection connection = postgres.createConnection("");
             Statement statement = connection.createStatement()) {
            ResultSet version = statement.executeQuery(
                    "SELECT 1 FROM flyway_schema_history WHERE version = '22'");
            assertTrue(version.next(), "V22 migration should be applied");

            assertIndexExists(statement, "uq_user_rewards_voucher_pool_id");
            assertIndexExists(statement, "uq_campaigns_default_per_line");
        }
    }

    @Test
    void softDeletedDefaultDoesNotBlockNewDefault() throws Exception {
        try (Connection connection = postgres.createConnection("");
             Statement statement = connection.createStatement()) {
            UUID lineId = UUID.randomUUID();
            UUID deletedCampaign = UUID.randomUUID();
            UUID activeCampaign = UUID.randomUUID();

            statement.execute("""
                    INSERT INTO lines (id, code, name, display_name, total_stations, status, sort_order)
                    VALUES ('%s', 'L22', 'L22', 'L22', 1, 'ACTIVE', 0)
                    """.formatted(lineId));

            statement.execute("""
                    INSERT INTO campaigns (
                        id, code, name, display_name, campaign_type, status,
                        start_at, end_at, priority, line_id, is_default, deleted_at
                    ) VALUES (
                        '%s', 'C-DEL', 'Deleted', 'Deleted', 'STANDARD', 'ACTIVE',
                        NOW(), NOW() + INTERVAL '1 year', 0, '%s', TRUE, NOW()
                    )
                    """.formatted(deletedCampaign, lineId));

            statement.execute("""
                    INSERT INTO campaigns (
                        id, code, name, display_name, campaign_type, status,
                        start_at, end_at, priority, line_id, is_default
                    ) VALUES (
                        '%s', 'C-NEW', 'New', 'New', 'STANDARD', 'ACTIVE',
                        NOW(), NOW() + INTERVAL '1 year', 0, '%s', TRUE
                    )
                    """.formatted(activeCampaign, lineId));
        }
    }

    @Test
    void voucherPoolIdLinksToAtMostOneUserReward() throws Exception {
        try (Connection connection = postgres.createConnection("");
             Statement statement = connection.createStatement()) {
            UUID user1 = UUID.randomUUID();
            UUID user2 = UUID.randomUUID();
            UUID lineId = UUID.randomUUID();
            UUID campaignId = UUID.randomUUID();
            UUID milestoneId = UUID.randomUUID();
            UUID milestoneId2 = UUID.randomUUID();
            UUID voucherId = UUID.randomUUID();
            UUID reward1 = UUID.randomUUID();
            UUID reward2 = UUID.randomUUID();

            seedUsersAndMilestone(statement, user1, user2, lineId, campaignId, milestoneId, milestoneId2);
            statement.execute("""
                    INSERT INTO voucher_pool (id, milestone_id, code, status, is_redeemed, created_at)
                    VALUES ('%s', '%s', 'CODE-%s', 'AVAILABLE', FALSE, NOW())
                    """.formatted(voucherId, milestoneId, voucherId.toString().substring(0, 8)));

            statement.execute("""
                    INSERT INTO user_rewards (id, user_id, campaign_id, milestone_id, issued_at, status, voucher_pool_id)
                    VALUES ('%s', '%s', '%s', '%s', NOW(), 'ISSUED', '%s')
                    """.formatted(reward1, user1, campaignId, milestoneId, voucherId));

            SQLException thrown = assertThrows(SQLException.class, () ->
                    statement.execute("""
                            INSERT INTO user_rewards (id, user_id, campaign_id, milestone_id, issued_at, status, voucher_pool_id)
                            VALUES ('%s', '%s', '%s', '%s', NOW(), 'ISSUED', '%s')
                            """.formatted(reward2, user2, campaignId, milestoneId2, voucherId)));
            String msg = thrown.getMessage();
            if (thrown.getNextException() != null) {
                msg = msg + " " + thrown.getNextException().getMessage();
            }
            assertTrue(msg.contains("uq_user_rewards_voucher_pool_id"),
                    "Expected voucher pool unique violation, got: " + msg);
        }
    }

    private static void seedUsersAndMilestone(
            Statement statement,
            UUID user1,
            UUID user2,
            UUID lineId,
            UUID campaignId,
            UUID milestoneId,
            UUID milestoneId2) throws Exception {
        for (UUID userId : new UUID[]{user1, user2}) {
            statement.execute("""
                    INSERT INTO users (id, username, email, phone_number, password, status, token_version, created_at)
                    VALUES ('%s', 'u-%s', 'u-%s@ex.com', '+1555%s', 'x', 'ACTIVE', 0, NOW())
                    """.formatted(
                    userId,
                    userId.toString().substring(0, 8),
                    userId.toString().substring(0, 8),
                    userId.toString().replace("-", "").substring(0, 7)));
        }
        statement.execute("""
                INSERT INTO lines (id, code, name, display_name, total_stations, status, sort_order)
                VALUES ('%s', 'LV', 'LV', 'LV', 1, 'ACTIVE', 0)
                """.formatted(lineId));
        statement.execute("""
                INSERT INTO campaigns (
                    id, code, name, display_name, campaign_type, status,
                    start_at, end_at, priority, line_id, is_default
                ) VALUES (
                    '%s', 'CV', 'CV', 'CV', 'STANDARD', 'ACTIVE',
                    NOW(), NOW() + INTERVAL '1 year', 0, '%s', FALSE
                )
                """.formatted(campaignId, lineId));
        statement.execute("""
                INSERT INTO milestones (id, line_id, campaign_id, code, stamps_required, name,
                    reward_type, reward_title, status, sort_order, is_active)
                VALUES ('%s', '%s', '%s', 'M1', 1, 'M1', 'VOUCHER', 'V', 'ACTIVE', 0, TRUE)
                """.formatted(milestoneId, lineId, campaignId));
        statement.execute("""
                INSERT INTO milestones (id, line_id, campaign_id, code, stamps_required, name,
                    reward_type, reward_title, status, sort_order, is_active)
                VALUES ('%s', '%s', '%s', 'M2', 2, 'M2', 'DIGITAL_STICKER', 'P', 'ACTIVE', 1, TRUE)
                """.formatted(milestoneId2, lineId, campaignId));
    }

    private static void assertIndexExists(Statement statement, String name) throws Exception {
        ResultSet rs = statement.executeQuery("""
                SELECT 1 FROM pg_indexes WHERE indexname = '%s'
                """.formatted(name));
        assertTrue(rs.next(), "Index " + name + " should exist");
    }
}
