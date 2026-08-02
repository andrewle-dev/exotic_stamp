package metro.ExoticStamp.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Under the Maven {@code ci} profile, reject datasource URLs that look like
 * shared developer/production hosts. Testcontainers JDBC URLs remain allowed.
 */
@EnabledIfSystemProperty(named = "ci.require-docker", matches = "true")
class CiDatasourceUrlGuardIT {

    @Test
    void datasourceUrlMustBeTestcontainersOrLocalEphemeral() {
        String url = System.getProperty(
                "spring.datasource.url",
                System.getenv().getOrDefault("DB_URL", ""));
        if (url == null || url.isBlank()) {
            // DynamicPropertySource / Testcontainers sets URL per IT; empty here is OK.
            return;
        }
        String lower = url.toLowerCase();
        assertTrue(
                lower.startsWith("jdbc:tc:")
                        || lower.contains("testcontainers")
                        || lower.contains("@localhost")
                        || lower.contains("//localhost")
                        || lower.contains("127.0.0.1"),
                () -> "CI datasource URL must use Testcontainers or localhost, got: " + redact(url));
        assertFalse(looksLikeSharedHost(lower), () -> "CI rejected shared/prod-like JDBC URL: " + redact(url));
    }

    private static boolean looksLikeSharedHost(String lowerUrl) {
        return lowerUrl.contains("amazonaws.com")
                || lowerUrl.contains("rds.")
                || lowerUrl.contains("azure.com")
                || lowerUrl.contains("neon.tech")
                || lowerUrl.contains("supabase.co")
                || lowerUrl.contains("facewashfox")
                || lowerUrl.contains("exoticstamp.com");
    }

    private static String redact(String url) {
        return url.replaceAll("(?i)(password=)[^&;]+", "$1***");
    }
}
