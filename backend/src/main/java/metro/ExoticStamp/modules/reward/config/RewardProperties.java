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
}
