package metro.ExoticStamp.config;

import metro.ExoticStamp.infra.security.ratelimit.RateLimitProperties;
import metro.ExoticStamp.infra.storage.StorageProperties;
import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProperties;
import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtSecretValidator;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;

/**
 * Fail-fast production startup checks for currently bound properties.
 * CORS wildcard+credentials is enforced solely by {@link CorsProperties} (not duplicated here).
 */
@Component
@Profile("prod")
public class ProdStartupValidator {

    private static final int MIN_PEPPER_LENGTH = 16;

    private final Environment environment;
    private final JwtProperties jwtProperties;
    private final CorsProperties corsProperties;
    private final ApplicationSiteProperties siteProperties;
    private final StorageProperties storageProperties;
    private final RateLimitProperties rateLimitProperties;

    public ProdStartupValidator(
            Environment environment,
            JwtProperties jwtProperties,
            CorsProperties corsProperties,
            ApplicationSiteProperties siteProperties,
            StorageProperties storageProperties,
            RateLimitProperties rateLimitProperties
    ) {
        this.environment = environment;
        this.jwtProperties = jwtProperties;
        this.corsProperties = corsProperties;
        this.siteProperties = siteProperties;
        this.storageProperties = storageProperties;
        this.rateLimitProperties = rateLimitProperties;
    }

    @PostConstruct
    public void validate() {
        JwtSecretValidator.validateBase64Secret(jwtProperties.getSecret());

        requireText(environment.getProperty("spring.datasource.url"), "spring.datasource.url (DB_URL)");
        requireText(environment.getProperty("spring.datasource.username"), "spring.datasource.username (DB_USERNAME)");
        requireText(environment.getProperty("spring.datasource.password"), "spring.datasource.password (DB_PASSWORD)");
        requireText(environment.getProperty("spring.data.redis.host"), "spring.data.redis.host (REDIS_HOST)");

        if (corsProperties.allowedOriginsList().isEmpty()) {
            throw new IllegalStateException(
                    "Production requires non-empty application.cors.allowed-origins (CORS_ALLOWED_ORIGINS)");
        }

        requireText(siteProperties.getFrontend().getCurrent(), "application.frontend.current (FRONTEND_URL)");
        requireText(siteProperties.getBackend().getCurrent(), "application.backend.current (BACKEND_URL)");

        rejectLocalhostFallback(siteProperties.getFrontend().getCurrent(), "FRONTEND_URL");
        rejectLocalhostFallback(siteProperties.getBackend().getCurrent(), "BACKEND_URL");
        rejectLocalhostFallback(environment.getProperty("spring.datasource.url"), "DB_URL");
        rejectLocalhostFallback(environment.getProperty("spring.data.redis.host"), "REDIS_HOST");

        validateRateLimit();
        validateStorage();
    }

    private void validateStorage() {
        String provider = storageProperties.getProvider();
        if (provider == null || !"s3".equalsIgnoreCase(provider.trim())) {
            throw new IllegalStateException(
                    "Production STORAGE_PROVIDER must be s3 (local filesystem is not permitted)");
        }
        if (storageProperties.getS3() == null
                || storageProperties.getS3().getBucket() == null
                || storageProperties.getS3().getBucket().isBlank()) {
            throw new IllegalStateException("Production requires storage.s3.bucket (AWS_S3_BUCKET)");
        }
        if (storageProperties.getS3().getRegion() == null
                || storageProperties.getS3().getRegion().isBlank()) {
            throw new IllegalStateException("Production requires storage.s3.region (AWS_REGION)");
        }
        if (storageProperties.getPublicBaseUrl() == null
                || storageProperties.getPublicBaseUrl().isBlank()) {
            throw new IllegalStateException(
                    "Production requires storage.public-base-url (STORAGE_PUBLIC_BASE_URL) for public media");
        }
        rejectLocalhostFallback(storageProperties.getPublicBaseUrl(), "STORAGE_PUBLIC_BASE_URL");
    }

    private void validateRateLimit() {
        String pepper = rateLimitProperties.getKeyPepper();
        if (pepper == null || pepper.isBlank()) {
            throw new IllegalStateException(
                    "Production requires application.security.rate-limit.key-pepper (RATE_LIMIT_KEY_PEPPER)");
        }
        if (pepper.length() < MIN_PEPPER_LENGTH) {
            throw new IllegalStateException(
                    "Production RATE_LIMIT_KEY_PEPPER must be at least " + MIN_PEPPER_LENGTH + " characters");
        }
        String backend = rateLimitProperties.getBackend();
        if (backend == null || !"redis".equalsIgnoreCase(backend.trim())) {
            throw new IllegalStateException(
                    "Production application.security.rate-limit.backend must be redis");
        }
    }

    static boolean isWindowsDrivePath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        String normalized = path.trim();
        return normalized.matches("^[A-Za-z]:[\\\\/].*") || normalized.startsWith("\\\\");
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Production requires " + name);
        }
    }

    private static void rejectLocalhostFallback(String value, String name) {
        if (value == null) {
            return;
        }
        String lower = value.toLowerCase();
        if (lower.contains("localhost") || lower.contains("127.0.0.1")) {
            throw new IllegalStateException(
                    "Production " + name + " must not target localhost / 127.0.0.1");
        }
    }
}
