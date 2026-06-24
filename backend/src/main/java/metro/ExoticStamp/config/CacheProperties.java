package metro.ExoticStamp.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@ConfigurationProperties(prefix = "cache")
@Component
@Data
public class CacheProperties {

    @NotNull
    @DurationMin(seconds = 1)
    private Duration userTtl     = Duration.ofMinutes(30);   // Set default value in code, no need to declare yml
    private Duration salesTtl    = Duration.ofMinutes(10);
    private Duration bookingTtl  = Duration.ofMinutes(10);
    private Duration customerTtl = Duration.ofMinutes(10);

    @NotNull
    @DurationMin(seconds = 30)  // realtime không được dưới 30s
    @DurationMax(seconds = 120) // và không được quá 120s
    private Duration realtimeTtl = Duration.ofSeconds(70);   // đặc thù realtime module

    private Duration refreshTokenTtl = Duration.ofDays(7);

    /** TTL for {@code user:{id}:tokenVersion} cache; align with access-token TTL or longer. */
    @NotNull
    @DurationMin(seconds = 60)
    private Duration accessTokenVersionTtl = Duration.ofMinutes(15);

    /**
     * Metro / other nested TTLs (see application.yml {@code cache.ttl.*}).
     */
    private Ttl ttl = new Ttl();

    @Data
    public static class Ttl {
        @NotNull
        @DurationMin(seconds = 1)
        private Duration stationDetail = Duration.ofSeconds(1800);

        @NotNull
        @DurationMin(seconds = 1)
        private Duration metroStationScan = Duration.ofSeconds(1800);

        /** Collection read caches: 10–30 minutes (600–1800s). */
        @NotNull
        @DurationMin(seconds = 600)
        @DurationMax(seconds = 1800)
        private Duration collectionUserStamps = Duration.ofMinutes(10);

        @NotNull
        @DurationMin(seconds = 600)
        @DurationMax(seconds = 1800)
        private Duration collectionUserProgress = Duration.ofMinutes(10);

        @NotNull
        @DurationMin(seconds = 600)
        @DurationMax(seconds = 1800)
        private Duration collectionUserHistory = Duration.ofMinutes(15);

        @NotNull
        @DurationMin(seconds = 600)
        @DurationMax(seconds = 1800)
        private Duration collectionStampBook = Duration.ofMinutes(15);
    }

    /**
     * private Duration userTtl; // No default, have to declare in yml
     * In this case, we combine default value in code + override by yml
    */
}