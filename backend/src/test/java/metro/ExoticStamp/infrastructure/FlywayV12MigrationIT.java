package metro.ExoticStamp.infrastructure;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class FlywayV12MigrationIT {

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
    void appliesV12Migration() throws Exception {
        try (Connection connection = postgres.createConnection("");
             Statement statement = connection.createStatement()) {

            ResultSet version = statement.executeQuery(
                    "SELECT 1 FROM flyway_schema_history WHERE version = '12'");
            assertTrue(version.next(), "V12 migration should be applied");

            assertConstraintExists(statement, "chk_users_status");
            assertConstraintExists(statement, "fk_user_stamps_user_id");
            assertConstraintExists(statement, "chk_milestones_stamps_required");
        }
    }

    @Test
    void rejectsInvalidUserRewardStatus() throws Exception {
        try (Connection connection = postgres.createConnection("");
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO users (id, username, email, phone_number, password, gender)
                    VALUES ('11111111-1111-1111-1111-111111111111', 'u1', 'u1@test.com', '+10000000001', 'hash', false)
                    ON CONFLICT DO NOTHING
                    """);
            statement.execute("""
                    INSERT INTO lines (id, code, name, sort_order, status)
                    VALUES ('22222222-2222-2222-2222-222222222222', 'L1', 'Line 1', 0, 'ACTIVE')
                    ON CONFLICT DO NOTHING
                    """);
            statement.execute("""
                    INSERT INTO milestones (
                        id, line_id, code, stamps_required, name, reward_type, reward_title, status, sort_order, is_active
                    ) VALUES (
                        '33333333-3333-3333-3333-333333333333',
                        '22222222-2222-2222-2222-222222222222',
                        'M1', 1, 'M1', 'VOUCHER', 'R1', 'ACTIVE', 0, true
                    )
                    ON CONFLICT DO NOTHING
                    """);

            boolean rejected = false;
            try {
                statement.execute("""
                        INSERT INTO user_rewards (id, user_id, milestone_id, status)
                        VALUES ('55555555-5555-5555-5555-555555555555',
                                '11111111-1111-1111-1111-111111111111',
                                '33333333-3333-3333-3333-333333333333',
                                'INVALID')
                        """);
            } catch (Exception e) {
                rejected = true;
            }
            assertTrue(rejected, "chk_user_rewards_status should reject invalid status");
        }
    }

    private static void assertConstraintExists(Statement statement, String constraintName) throws Exception {
        ResultSet rs = statement.executeQuery(
                "SELECT 1 FROM pg_constraint WHERE conname = '" + constraintName + "'");
        assertTrue(rs.next(), "Expected constraint " + constraintName);
    }
}
