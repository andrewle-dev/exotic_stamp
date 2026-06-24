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
class FlywayV13MigrationIT {

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
    void appliesV13AsLatestMigration() throws Exception {
        try (Connection connection = postgres.createConnection("");
             Statement statement = connection.createStatement()) {
            ResultSet version = statement.executeQuery(
                    "SELECT version FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1");
            assertTrue(version.next());
            assertEquals("13", version.getString("version"));
            assertConstraintExists(statement, "chk_lines_status");
            assertConstraintExists(statement, "uq_stations_line_code");
            assertConstraintExists(statement, "chk_stations_scan_key_status");
        }
    }

    @Test
    void rejectsInvalidLineStatus() throws Exception {
        try (Connection connection = postgres.createConnection("");
             Statement statement = connection.createStatement()) {
            boolean rejected = false;
            try {
                statement.execute("""
                        INSERT INTO lines (id, code, name, sort_order, status)
                        VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'LX', 'Bad', 0, 'BAD')
                        """);
            } catch (Exception e) {
                rejected = true;
            }
            assertTrue(rejected);
        }
    }

    @Test
    void rejectsDuplicateNfcTag() throws Exception {
        try (Connection connection = postgres.createConnection("");
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO lines (id, code, name, sort_order, status)
                    VALUES ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'L2', 'Line 2', 0, 'ACTIVE')
                    ON CONFLICT DO NOTHING
                    """);
            statement.execute("""
                    INSERT INTO stations (id, line_id, code, name, sort_order, status, scan_key_status, nfc_tag_id, collector_count)
                    VALUES ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
                            'S1', 'Station 1', 0, 'ACTIVE', 'ACTIVE', 'NFC-DUP-1', 0)
                    ON CONFLICT DO NOTHING
                    """);
            boolean rejected = false;
            try {
                statement.execute("""
                        INSERT INTO stations (id, line_id, code, name, sort_order, status, scan_key_status, nfc_tag_id, collector_count)
                        VALUES ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
                                'S2', 'Station 2', 1, 'ACTIVE', 'ACTIVE', 'NFC-DUP-1', 0)
                        """);
            } catch (Exception e) {
                rejected = true;
            }
            assertTrue(rejected);
        }
    }

    private static void assertConstraintExists(Statement statement, String constraintName) throws Exception {
        ResultSet rs = statement.executeQuery(
                "SELECT 1 FROM pg_constraint WHERE conname = '" + constraintName + "'");
        assertTrue(rs.next(), "Expected constraint " + constraintName);
    }
}
