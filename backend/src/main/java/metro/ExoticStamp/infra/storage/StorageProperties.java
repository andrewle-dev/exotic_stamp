package metro.ExoticStamp.infra.storage;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@Data
@Validated
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    @NotBlank
    private String provider = "local";

    /**
     * Public media base URL without trailing slash. Public URL = base + "/" + object_key.
     * Required for provider=s3 in production.
     */
    private String publicBaseUrl;

    private Local local = new Local();

    private FileConstraints file = new FileConstraints();

    private S3 s3 = new S3();

    private Cleanup cleanup = new Cleanup();

    @Data
    public static class Local {
        private String basePath;
        private String baseUrl;
    }

    @Data
    public static class FileConstraints {
        private long maxSizeMb = 5;
        private List<String> allowedTypes = List.of("image/jpeg", "image/png", "image/webp");
        private int maxWidth = 2560;
        private int maxHeight = 2560;
        private long maxPixels = 2560L * 2560L;
    }

    @Data
    public static class S3 {
        private String bucket;
        private String region;
        /** Optional override for LocalStack / custom endpoints only. */
        private String endpoint;
        /** Path-style access — LocalStack only. */
        private boolean pathStyleAccess = false;
        private Duration connectTimeout = Duration.ofSeconds(5);
        private Duration apiCallTimeout = Duration.ofSeconds(60);
        private Duration apiCallAttemptTimeout = Duration.ofSeconds(20);
        private int maxRetries = 2;
        private Duration presignExpiry = Duration.ofMinutes(15);
    }

    @Data
    public static class Cleanup {
        /** Delay before orphaned objects become eligible for hard delete. */
        private Duration orphanRetention = Duration.ofDays(14);
        private int batchSize = 50;
        private Duration maxRunDuration = Duration.ofSeconds(30);
        private boolean dryRun = false;
        /** Cron for orphan reconciliation; empty disables. */
        private String cron = "0 15 3 * * *";
    }
}
