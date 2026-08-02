package metro.ExoticStamp.infra.security.ratelimit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * In-process token-bucket limiter for unit tests and local dev only.
 * Not suitable for multi-instance production.
 */
@Component
@ConditionalOnProperty(
        name = "application.security.rate-limit.backend",
        havingValue = "memory"
)
public class InMemoryRateLimiter implements RateLimiter {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public RateLimitDecision tryConsume(String bucketKey, RateLimitProperties.Policy policy) {
        long capacity = Math.max(1, policy.getCapacity());
        long refillTokens = Math.max(1, policy.getRefillTokens());
        long refillPeriodMs = Math.max(1, policy.getRefillPeriod().toMillis());
        long ttlMs = Math.max(refillPeriodMs, policy.getTtl().toMillis());
        long nowMs = System.currentTimeMillis();

        Bucket bucket = buckets.compute(bucketKey, (k, existing) -> {
            if (existing == null || nowMs - existing.createdAtMs > ttlMs) {
                return new Bucket(capacity, nowMs, nowMs);
            }
            return existing;
        });

        synchronized (bucket) {
            // Re-check TTL under lock
            if (nowMs - bucket.createdAtMs > ttlMs) {
                bucket.tokens = capacity;
                bucket.lastRefillMs = nowMs;
                bucket.createdAtMs = nowMs;
            }

            long elapsed = Math.max(0, nowMs - bucket.lastRefillMs);
            long periods = elapsed / refillPeriodMs;
            if (periods > 0) {
                bucket.tokens = Math.min(capacity, bucket.tokens + periods * refillTokens);
                bucket.lastRefillMs = bucket.lastRefillMs + periods * refillPeriodMs;
            }

            if (bucket.tokens >= 1) {
                bucket.tokens -= 1;
                return new RateLimitDecision(true, 0);
            }

            long need = 1 - bucket.tokens;
            long retryMs = (long) Math.ceil((need / (double) refillTokens) * refillPeriodMs);
            long retryAfter = Math.max(1, (retryMs + 999) / 1000);
            long ttlRemainingSec = Math.max(1, (ttlMs - (nowMs - bucket.createdAtMs) + 999) / 1000);
            // Prefer refill wait; if somehow zero use TTL remaining
            if (retryAfter < 1) {
                retryAfter = ttlRemainingSec;
            }
            return new RateLimitDecision(false, retryAfter);
        }
    }

    /** Visible for tests — clears all buckets. */
    void clear() {
        buckets.clear();
    }

    private static final class Bucket {
        private long tokens;
        private long lastRefillMs;
        private long createdAtMs;

        private Bucket(long tokens, long lastRefillMs, long createdAtMs) {
            this.tokens = tokens;
            this.lastRefillMs = lastRefillMs;
            this.createdAtMs = createdAtMs;
        }
    }
}
