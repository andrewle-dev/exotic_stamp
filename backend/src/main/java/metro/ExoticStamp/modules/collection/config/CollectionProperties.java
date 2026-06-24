package metro.ExoticStamp.modules.collection.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Collection module settings (idempotency window, query limits, GPS).
 */
@Data
@Validated
@ConfigurationProperties(prefix = "collection")
public class CollectionProperties {

    /** How long a client idempotency key is honored for duplicate detection. */
    @NotNull
    private Duration idempotencyWindow = Duration.ofHours(1);

    /** Max page size for history and stamp lists. */
    private int maxPageSize = 50;

    /** Default page size for paginated collection reads. */
    private int defaultPageSize = 20;

    /**
     * TTL for Redis keys that deduplicate {@link metro.ExoticStamp.modules.collection.domain.event.StampCollectedEvent} handling.
     */
    @NotNull
    private Duration stampCollectedEventDedupTtl = Duration.ofHours(48);

    @Valid
    @NotNull
    private Gps gps = new Gps();

    public boolean isGpsVerificationEnabled() {
        return gps != null && gps.isVerificationEnabled();
    }

    @Data
    public static class Gps {
        private boolean verificationEnabled;
        private double maxDistanceMeters;
        private double earthRadiusMeters;
    }
}
