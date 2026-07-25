package metro.ExoticStamp.config;

import metro.ExoticStamp.ExoticStampApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Context-level fail-fast checks for prod profile without Docker.
 * Positive path is covered by {@link ProdStartupValidatorTest}; these cases assert
 * {@link ProdStartupValidator} rejects unsafe production configuration.
 */
class ProdConfigurationFailFastTest {

    private static final String JWT_TEST_SECRET =
            "dGVzdC1qd3Qtc2VjcmV0LTMyLWJ5dGVzLW1pbmltdW0hIQ==";

    private static final String RATE_LIMIT_PEPPER = "test-rate-limit-pepper-32chars!!";

    private static final String AUTOCONFIG_EXCLUDE =
            "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration";

    private static final String[] VALID_S3_STORAGE = {
            "--storage.provider=s3",
            "--storage.s3.bucket=exotic-media",
            "--storage.s3.region=ap-southeast-1",
            "--storage.public-base-url=https://cdn.example.com"
    };

    @Test
    void missingBackendUrl_failsWithProdValidatorMessage() {
        assertThatThrownBy(() -> runProd(
                "--jwt.secret=" + JWT_TEST_SECRET,
                "--application.security.rate-limit.key-pepper=" + RATE_LIMIT_PEPPER,
                "--spring.datasource.url=jdbc:postgresql://db.internal:5432/exotic_stamp",
                "--spring.datasource.username=exotic_app",
                "--spring.datasource.password=not-a-real-secret",
                "--spring.data.redis.host=redis.internal",
                "--application.frontend.current=https://admin.example.com",
                "--application.backend.current=",
                "--application.cors.allowed-origins=https://admin.example.com",
                "--spring.mail.username=noreply@example.com",
                "--spring.mail.password=not-a-real-secret",
                "--application.mail.from=noreply@example.com",
                VALID_S3_STORAGE[0],
                VALID_S3_STORAGE[1],
                VALID_S3_STORAGE[2],
                VALID_S3_STORAGE[3]
        )).hasRootCauseInstanceOf(IllegalStateException.class)
                .hasRootCauseMessage("Production requires application.backend.current (BACKEND_URL)");
    }

    @Test
    void localStorageProvider_fails() {
        assertThatThrownBy(() -> runProd(
                "--jwt.secret=" + JWT_TEST_SECRET,
                "--application.security.rate-limit.key-pepper=" + RATE_LIMIT_PEPPER,
                "--spring.datasource.url=jdbc:postgresql://db.internal:5432/exotic_stamp",
                "--spring.datasource.username=exotic_app",
                "--spring.datasource.password=not-a-real-secret",
                "--spring.data.redis.host=redis.internal",
                "--application.frontend.current=https://admin.example.com",
                "--application.backend.current=https://api.example.com",
                "--application.cors.allowed-origins=https://admin.example.com",
                "--storage.provider=local",
                "--storage.local.base-path=/var/app/uploads",
                "--spring.mail.username=noreply@example.com",
                "--spring.mail.password=not-a-real-secret",
                "--application.mail.from=noreply@example.com"
        )).hasRootCauseInstanceOf(IllegalStateException.class)
                .rootCause()
                .hasMessageContaining("STORAGE_PROVIDER must be s3");
    }

    @Test
    void emptyCorsOrigins_failsWithProdValidatorMessage() {
        assertThatThrownBy(() -> runProd(
                "--jwt.secret=" + JWT_TEST_SECRET,
                "--application.security.rate-limit.key-pepper=" + RATE_LIMIT_PEPPER,
                "--spring.datasource.url=jdbc:postgresql://db.internal:5432/exotic_stamp",
                "--spring.datasource.username=exotic_app",
                "--spring.datasource.password=not-a-real-secret",
                "--spring.data.redis.host=redis.internal",
                "--application.frontend.current=https://admin.example.com",
                "--application.backend.current=https://api.example.com",
                "--application.cors.allowed-origins=",
                "--spring.mail.username=noreply@example.com",
                "--spring.mail.password=not-a-real-secret",
                "--application.mail.from=noreply@example.com",
                VALID_S3_STORAGE[0],
                VALID_S3_STORAGE[1],
                VALID_S3_STORAGE[2],
                VALID_S3_STORAGE[3]
        )).hasRootCauseInstanceOf(IllegalStateException.class)
                .rootCause()
                .hasMessageContaining("CORS_ALLOWED_ORIGINS");
    }

    private static void runProd(String... extraArgs) {
        var app = new SpringApplication(ExoticStampApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        String[] base = new String[]{
                "--spring.profiles.active=prod",
                "--spring.main.web-application-type=none",
                "--spring.flyway.enabled=false",
                "--spring.jpa.hibernate.ddl-auto=none",
                "--spring.autoconfigure.exclude=" + AUTOCONFIG_EXCLUDE
        };
        String[] args = new String[base.length + extraArgs.length];
        System.arraycopy(base, 0, args, 0, base.length);
        System.arraycopy(extraArgs, 0, args, base.length, extraArgs.length);
        app.run(args);
    }
}
