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
class FlywayV14MigrationIT {

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
    void appliesV14Migration() throws Exception {
        try (Connection connection = postgres.createConnection("");
             Statement statement = connection.createStatement()) {
            ResultSet version = statement.executeQuery(
                    "SELECT 1 FROM flyway_schema_history WHERE version = '14'");
            assertTrue(version.next(), "V14 migration should be applied");
            assertConstraintExists(statement, "chk_campaigns_status");
            assertConstraintExists(statement, "chk_stamp_designs_rarity");
            assertIndexExists(statement, "uq_stamp_design_active_per_campaign_station");
        }
    }

    @Test
    void rejectsInvalidCampaignStatus() throws Exception {
        try (Connection connection = postgres.createConnection("");
             Statement statement = connection.createStatement()) {
            boolean rejected = false;
            try {
                statement.execute("""
                        INSERT INTO campaigns (id, code, name, display_name, campaign_type, status, priority, start_at, end_at, is_default)
                        VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'BAD1', 'Bad', 'Bad', 'STANDARD', 'BAD',
                                0, NOW(), NOW() + INTERVAL '1 day', FALSE)
                        """);
            } catch (Exception e) {
                rejected = true;
            }
            assertTrue(rejected);
        }
    }

    @Test
    void rejectsDuplicateCampaignCode() throws Exception {
        try (Connection connection = postgres.createConnection("");
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO campaigns (id, code, name, display_name, campaign_type, status, priority, start_at, end_at, is_default)
                    VALUES ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'DUP-CODE', 'One', 'One', 'STANDARD', 'DRAFT',
                            0, NOW(), NOW() + INTERVAL '1 day', FALSE)
                    ON CONFLICT DO NOTHING
                    """);
            boolean rejected = false;
            try {
                statement.execute("""
                        INSERT INTO campaigns (id, code, name, display_name, campaign_type, status, priority, start_at, end_at, is_default)
                        VALUES ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'DUP-CODE', 'Two', 'Two', 'STANDARD', 'DRAFT',
                                0, NOW(), NOW() + INTERVAL '1 day', FALSE)
                        """);
            } catch (Exception e) {
                rejected = true;
            }
            assertTrue(rejected);
        }
    }

    private static void assertConstraintExists(Statement statement, String name) throws Exception {
        ResultSet rs = statement.executeQuery("""
                SELECT 1 FROM pg_constraint WHERE conname = '%s'
                """.formatted(name));
        assertTrue(rs.next(), "Expected constraint " + name);
    }

    private static void assertIndexExists(Statement statement, String name) throws Exception {
        ResultSet rs = statement.executeQuery("""
                SELECT 1 FROM pg_indexes WHERE indexname = '%s'
                """.formatted(name));
        assertTrue(rs.next(), "Expected index " + name);
    }
}
