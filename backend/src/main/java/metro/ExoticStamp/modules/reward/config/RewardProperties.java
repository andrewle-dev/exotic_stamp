package metro.ExoticStamp.modules.reward.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Data
@Validated
@ConfigurationProperties(prefix = "reward")
public class RewardProperties {

    private int defaultPageSize = 20;

    private int maxPageSize = 50;

    @NotNull
    private Duration userRewardCacheTtl = Duration.ofMinutes(30);

    @NotNull
    private Duration stampCollectedEventDedupTtl = Duration.ofHours(48);

    @NotNull
    private Duration stampCollectedEventProcessingLockTtl = Duration.ofMinutes(2);

    private int stampCollectedEventMaxAttempts = 3;

    @NotNull
    private Duration stampCollectedEventRetryBackoff = Duration.ofMillis(200);

    /**
     * Cron for nightly reward expiry batch (Spring {@code @Scheduled} expression).
     */
    @NotNull
    private String expiryCron = "0 0 2 * * *";

    /**
     * Cron for reward reconcile after missed AFTER_COMMIT listeners (R-P1-01 Option B).
     */
    @NotNull
    private String reconcileCron = "0 20 */1 * * *";

    /** How far back to scan stamps for missing rewards. */
    @NotNull
    private Duration reconcileLookback = Duration.ofHours(48);

    private int reconcileBatchSize = 50;

    @NotNull
    private Duration reconcileMaxDuration = Duration.ofSeconds(30);

    private int reconcileMaxBatches = 5;

    private int reconcileMaxBatchSize = 100;
}
