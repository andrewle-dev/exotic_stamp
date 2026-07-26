package metro.ExoticStamp.infra.storage.s3;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Bounded S3 health check (HeadBucket). Exposed via /actuator/health/storage.
 * Intentionally NOT included in readiness: API can serve without uploads when S3 is briefly down.
 */
@Component("s3Storage")
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
public class S3StorageHealthIndicator implements HealthIndicator {

    private final S3StorageService s3StorageService;

    public S3StorageHealthIndicator(S3StorageService s3StorageService) {
        this.s3StorageService = s3StorageService;
    }

    @Override
    public Health health() {
        boolean ok = s3StorageService.probeBucket();
        if (ok) {
            return Health.up().withDetail("storage", "s3").build();
        }
        return Health.down().withDetail("storage", "s3").build();
    }
}
