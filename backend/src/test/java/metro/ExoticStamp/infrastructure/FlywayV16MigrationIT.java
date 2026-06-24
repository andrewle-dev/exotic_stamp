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
class FlywayV16MigrationIT {

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
    void appliesV16Migration() throws Exception {
        try (Connection connection = postgres.createConnection("");
             Statement statement = connection.createStatement()) {
            ResultSet version = statement.executeQuery(
                    "SELECT 1 FROM flyway_schema_history WHERE version = '16'");
            assertTrue(version.next(), "V16 migration should be applied");
            assertConstraintExists(statement, "chk_milestones_status");
            assertConstraintExists(statement, "chk_voucher_pool_status");
            assertConstraintExists(statement, "chk_user_rewards_status");
        }
    }

    @Test
    void seedsStage5Permissions() throws Exception {
        try (Connection connection = postgres.createConnection("");
             Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery("""
                    SELECT COUNT(*) FROM permissions
                    WHERE permission IN ('REWARD_MILESTONE_MANAGE', 'VOUCHER_POOL_MANAGE')
                    """);
            rs.next();
            assertTrue(rs.getInt(1) >= 2);
        }
    }

    private static void assertConstraintExists(Statement statement, String name) throws Exception {
        ResultSet rs = statement.executeQuery("""
                SELECT 1 FROM pg_constraint WHERE conname = '%s'
                """.formatted(name));
        assertTrue(rs.next(), "Constraint " + name + " should exist");
    }
}
