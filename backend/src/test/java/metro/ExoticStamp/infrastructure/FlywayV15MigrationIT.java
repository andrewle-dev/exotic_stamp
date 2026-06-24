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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class FlywayV15MigrationIT {

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
    void appliesV15AsLatestMigration() throws Exception {
        try (Connection connection = postgres.createConnection("");
             Statement statement = connection.createStatement()) {
            ResultSet version = statement.executeQuery(
                    "SELECT version FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1");
            assertTrue(version.next());
            assertEquals("15", version.getString("version"));
            assertColumnExists(statement, "user_stamps", "source_scan_type");
            assertColumnExists(statement, "user_stamps", "collection_policy");
            assertIndexExists(statement, "idx_user_stamps_user_campaign");
            assertIndexExists(statement, "uq_user_stamps_user_idempotency");
        }
    }

    private static void assertColumnExists(Statement statement, String table, String column) throws Exception {
        ResultSet rs = statement.executeQuery("""
                SELECT 1 FROM information_schema.columns
                WHERE table_name = '%s' AND column_name = '%s'
                """.formatted(table, column));
        assertTrue(rs.next(), "Expected column " + table + "." + column);
    }

    private static void assertIndexExists(Statement statement, String name) throws Exception {
        ResultSet rs = statement.executeQuery("""
                SELECT 1 FROM pg_indexes WHERE indexname = '%s'
                """.formatted(name));
        assertTrue(rs.next(), "Expected index " + name);
    }
}
