package metro.ExoticStamp.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Shared DynamicPropertySource helpers for Docker-backed SpringBoot ITs.
 * Host rewriting avoids ProdStartupValidator localhost rejection while still
 * targeting the local Testcontainers publish ports.
 */
public final class IntegrationTestSupport {

    public static final String JWT_TEST_SECRET =
            "dGVzdC1qd3Qtc2VjcmV0LTMyLWJ5dGVzLW1pbmltdW0hIQ==";

    private IntegrationTestSupport() {
    }

    public static String rewriteLoopbackHost(String host) {
        if (host == null || host.isBlank()) {
            return "127.0.0.2";
        }
        if ("localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host)) {
            return "127.0.0.2";
        }
        return host;
    }

    public static String rewriteJdbcUrl(String jdbcUrl) {
        return jdbcUrl
                .replace("://localhost:", "://127.0.0.2:")
                .replace("://127.0.0.1:", "://127.0.0.2:");
    }

    public static void registerPostgresAndRedis(
            DynamicPropertyRegistry registry,
            PostgreSQLContainer<?> postgres,
            GenericContainer<?> redis
    ) {
        registry.add("spring.datasource.url", () -> rewriteJdbcUrl(postgres.getJdbcUrl()));
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", () -> rewriteLoopbackHost(redis.getHost()));
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
        registry.add("DB_URL", () -> rewriteJdbcUrl(postgres.getJdbcUrl()));
        registry.add("DB_USERNAME", postgres::getUsername);
        registry.add("DB_PASSWORD", postgres::getPassword);
        registry.add("REDIS_HOST", () -> rewriteLoopbackHost(redis.getHost()));
        registry.add("REDIS_PORT", () -> redis.getMappedPort(6379).toString());
    }

    public static void registerCommonSecrets(DynamicPropertyRegistry registry) {
        registry.add("jwt.secret", () -> JWT_TEST_SECRET);
        registry.add("JWT_SECRET", () -> JWT_TEST_SECRET);
        registry.add("spring.mail.username", () -> "test@example.com");
        registry.add("spring.mail.password", () -> "test-mail-password");
        registry.add("MAIL_USERNAME", () -> "test@example.com");
        registry.add("MAIL_PASSWORD", () -> "test-mail-password");
        registry.add("MAIL_FROM", () -> "test@example.com");
        registry.add("application.mail.from", () -> "test@example.com");
        registry.add("application.security.rate-limit.key-pepper", () -> "test-rate-limit-pepper-32chars!!");
        registry.add("RATE_LIMIT_KEY_PEPPER", () -> "test-rate-limit-pepper-32chars!!");
        registry.add("storage.local.base-path", () -> "/tmp/exotic-stamp-uploads");
        registry.add("STORAGE_LOCAL_PATH", () -> "/tmp/exotic-stamp-uploads");
    }

    public static void registerDevBootstrap(DynamicPropertyRegistry registry) {
        registry.add("application.bootstrap.admin-password", () -> "test-admin-pass");
        registry.add("application.bootstrap.demo-user-password", () -> "test-demo-pass");
        registry.add("ADMIN_SEED_PASSWORD", () -> "test-admin-pass");
        registry.add("DEMO_USER_PASSWORD", () -> "test-demo-pass");
        registry.add("application.security.rate-limit.backend", () -> "memory");
    }

    public static void registerProdSite(DynamicPropertyRegistry registry) {
        registry.add("FRONTEND_URL", () -> "https://admin.example.com");
        registry.add("BACKEND_URL", () -> "https://api.example.com");
        registry.add("CORS_ALLOWED_ORIGINS", () -> "https://admin.example.com");
        registry.add("application.frontend.current", () -> "https://admin.example.com");
        registry.add("application.backend.current", () -> "https://api.example.com");
        registry.add("application.cors.allowed-origins", () -> "https://admin.example.com");
        registry.add("application.security.rate-limit.backend", () -> "redis");
        // Align with application-prod.yml: precompiled templates (not developmentMode).
        registry.add("gg.jte.use-precompiled-templates", () -> "true");
        registry.add("gg.jte.development-mode", () -> "false");
        // Batch D: prod requires S3 (DefaultCredentialsProvider; no live AWS calls at boot).
        registry.add("STORAGE_PROVIDER", () -> "s3");
        registry.add("storage.provider", () -> "s3");
        registry.add("AWS_REGION", () -> "ap-southeast-1");
        registry.add("storage.s3.region", () -> "ap-southeast-1");
        registry.add("AWS_S3_BUCKET", () -> "exotic-stamp-it");
        registry.add("storage.s3.bucket", () -> "exotic-stamp-it");
        registry.add("STORAGE_PUBLIC_BASE_URL", () -> "https://cdn.example.com");
        registry.add("storage.public-base-url", () -> "https://cdn.example.com");
        // Avoid real AWS resolution during IT boot; LocalStack-style endpoint unused unless set.
        registry.add("aws.region", () -> "ap-southeast-1");
    }
}
