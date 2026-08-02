package metro.ExoticStamp.config;

import metro.ExoticStamp.infra.security.ratelimit.RateLimitProperties;
import metro.ExoticStamp.infra.storage.StorageProperties;
import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ProdStartupValidatorTest {

    private static final String VALID_JWT_SECRET = Base64.getEncoder()
            .encodeToString("test-jwt-secret-32-bytes-minimum!!".getBytes());

    @Mock
    private JwtProperties jwtProperties;

    private CorsProperties corsProperties;
    private ApplicationSiteProperties siteProperties;
    private StorageProperties storageProperties;
    private RateLimitProperties rateLimitProperties;
    private MockEnvironment environment;

    @BeforeEach
    void setUp() {
        corsProperties = new CorsProperties();
        corsProperties.setAllowedOrigins("https://admin.example.com");
        corsProperties.setAllowCredentials(true);

        siteProperties = new ApplicationSiteProperties();
        siteProperties.getFrontend().setCurrent("https://admin.example.com");
        siteProperties.getBackend().setCurrent("https://api.example.com");

        storageProperties = new StorageProperties();
        storageProperties.setProvider("s3");
        storageProperties.setPublicBaseUrl("https://cdn.example.com");
        storageProperties.getS3().setBucket("exotic-media");
        storageProperties.getS3().setRegion("ap-southeast-1");

        rateLimitProperties = new RateLimitProperties();
        rateLimitProperties.setBackend("redis");
        rateLimitProperties.setKeyPepper("rate-limit-pepper-at-least-16");

        environment = new MockEnvironment();
        environment.setProperty("spring.datasource.url", "jdbc:postgresql://db.internal:5432/exotic_stamp");
        environment.setProperty("spring.datasource.username", "exotic_app");
        environment.setProperty("spring.datasource.password", "not-a-real-secret");
        environment.setProperty("spring.data.redis.host", "redis.internal");
    }

    @Test
    void validMinimumProdConfiguration_passes() {
        org.mockito.Mockito.when(jwtProperties.getSecret()).thenReturn(VALID_JWT_SECRET);
        ProdStartupValidator validator = newValidator(environment);
        assertThatCode(validator::validate).doesNotThrowAnyException();
    }

    @Test
    void missingFrontendUrl_fails() {
        org.mockito.Mockito.when(jwtProperties.getSecret()).thenReturn(VALID_JWT_SECRET);
        siteProperties.getFrontend().setCurrent(" ");
        assertThatThrownBy(() -> newValidator(environment).validate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FRONTEND_URL");
    }

    @Test
    void missingRedisHost_fails() {
        org.mockito.Mockito.when(jwtProperties.getSecret()).thenReturn(VALID_JWT_SECRET);
        environment.setProperty("spring.data.redis.host", "");
        assertThatThrownBy(() -> newValidator(environment).validate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("REDIS_HOST");
    }

    @Test
    void emptyCorsOrigins_fails() {
        org.mockito.Mockito.when(jwtProperties.getSecret()).thenReturn(VALID_JWT_SECRET);
        corsProperties.setAllowedOrigins("  ");
        assertThatThrownBy(() -> newValidator(environment).validate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CORS_ALLOWED_ORIGINS");
    }

    @Test
    void localStorageProvider_fails() {
        org.mockito.Mockito.when(jwtProperties.getSecret()).thenReturn(VALID_JWT_SECRET);
        storageProperties.setProvider("local");
        assertThatThrownBy(() -> newValidator(environment).validate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("STORAGE_PROVIDER must be s3");
    }

    @Test
    void missingS3Bucket_fails() {
        org.mockito.Mockito.when(jwtProperties.getSecret()).thenReturn(VALID_JWT_SECRET);
        storageProperties.getS3().setBucket(" ");
        assertThatThrownBy(() -> newValidator(environment).validate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("AWS_S3_BUCKET");
    }

    @Test
    void missingPublicBaseUrl_fails() {
        org.mockito.Mockito.when(jwtProperties.getSecret()).thenReturn(VALID_JWT_SECRET);
        storageProperties.setPublicBaseUrl(" ");
        assertThatThrownBy(() -> newValidator(environment).validate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("STORAGE_PUBLIC_BASE_URL");
    }

    @Test
    void localhostBackendUrl_fails() {
        org.mockito.Mockito.when(jwtProperties.getSecret()).thenReturn(VALID_JWT_SECRET);
        siteProperties.getBackend().setCurrent("http://localhost:8080");
        assertThatThrownBy(() -> newValidator(environment).validate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("localhost");
    }

    @Test
    void nonBase64JwtSecret_fails() {
        org.mockito.Mockito.when(jwtProperties.getSecret()).thenReturn("%%%not-valid-base64%%%");
        assertThatThrownBy(() -> newValidator(environment).validate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Base64");
    }

    @Test
    void shortDecodedJwtSecret_fails() {
        org.mockito.Mockito.when(jwtProperties.getSecret())
                .thenReturn(Base64.getEncoder().encodeToString(new byte[8]));
        assertThatThrownBy(() -> newValidator(environment).validate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }

    @Test
    void missingRateLimitPepper_fails() {
        org.mockito.Mockito.when(jwtProperties.getSecret()).thenReturn(VALID_JWT_SECRET);
        rateLimitProperties.setKeyPepper(" ");
        assertThatThrownBy(() -> newValidator(environment).validate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RATE_LIMIT_KEY_PEPPER");
    }

    @Test
    void memoryRateLimitBackend_fails() {
        org.mockito.Mockito.when(jwtProperties.getSecret()).thenReturn(VALID_JWT_SECRET);
        rateLimitProperties.setBackend("memory");
        assertThatThrownBy(() -> newValidator(environment).validate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("redis");
    }

    @Test
    void isWindowsDrivePath_detectsDriveLetters() {
        assertThat(ProdStartupValidator.isWindowsDrivePath("D:/uploads")).isTrue();
        assertThat(ProdStartupValidator.isWindowsDrivePath("C:\\data\\uploads")).isTrue();
        assertThat(ProdStartupValidator.isWindowsDrivePath("/var/app/uploads")).isFalse();
        assertThat(ProdStartupValidator.isWindowsDrivePath("./uploads")).isFalse();
    }

    private ProdStartupValidator newValidator(Environment env) {
        return new ProdStartupValidator(
                env, jwtProperties, corsProperties, siteProperties, storageProperties, rateLimitProperties);
    }
}
