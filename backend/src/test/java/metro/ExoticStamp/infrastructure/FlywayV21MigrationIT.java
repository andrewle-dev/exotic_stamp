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
class FlywayV21MigrationIT {

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
    void appliesV21Migration_andCreatesStoredAssets() throws Exception {
        try (Connection connection = postgres.createConnection("");
             Statement statement = connection.createStatement()) {
            ResultSet version = statement.executeQuery(
                    "SELECT 1 FROM flyway_schema_history WHERE version = '21'");
            assertTrue(version.next(), "V21 migration should be applied");

            ResultSet table = statement.executeQuery("""
                    SELECT 1 FROM information_schema.tables
                    WHERE table_schema = 'public' AND table_name = 'stored_assets'
                    """);
            assertTrue(table.next(), "stored_assets table should exist");

            assertConstraintExists(statement, "uq_stored_assets_object_key");
            assertConstraintExists(statement, "chk_stored_assets_visibility");
            assertConstraintExists(statement, "chk_stored_assets_status");
        }
    }

    @Test
    void widensLegacyImageUrlColumnsTo512() throws Exception {
        try (Connection connection = postgres.createConnection("");
             Statement statement = connection.createStatement()) {
            assertColumnLength(statement, "stations", "image_url", 512);
            assertColumnLength(statement, "stations", "stamp_preview_url", 512);
            assertColumnLength(statement, "campaigns", "banner_image_url", 512);
            assertColumnLength(statement, "campaigns", "thumbnail_image_url", 512);
            assertColumnLength(statement, "stamp_designs", "image_url", 512);
            assertColumnLength(statement, "stamp_designs", "preview_image_url", 512);
            assertColumnLength(statement, "partners", "logo_url", 512);
            assertColumnLength(statement, "partners", "banner_image_url", 512);
            assertColumnLength(statement, "milestones", "reward_image_url", 512);
        }
    }

    private static void assertConstraintExists(Statement statement, String name) throws Exception {
        ResultSet rs = statement.executeQuery("""
                SELECT 1 FROM pg_constraint WHERE conname = '%s'
                """.formatted(name));
        assertTrue(rs.next(), "Constraint " + name + " should exist");
    }

    private static void assertColumnLength(
            Statement statement, String table, String column, int expected) throws Exception {
        ResultSet rs = statement.executeQuery("""
                SELECT character_maximum_length
                FROM information_schema.columns
                WHERE table_schema = 'public'
                  AND table_name = '%s'
                  AND column_name = '%s'
                """.formatted(table, column));
        assertTrue(rs.next(), table + "." + column + " should exist");
        assertEquals(expected, rs.getInt(1), table + "." + column + " length");
    }
}
