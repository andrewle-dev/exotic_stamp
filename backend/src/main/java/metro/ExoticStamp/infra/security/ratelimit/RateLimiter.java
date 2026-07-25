package metro.ExoticStamp.infra.security.ratelimit;

/**
 * Token-bucket rate limiter backed by Redis or in-memory storage.
 */
public interface RateLimiter {

    RateLimitDecision tryConsume(String bucketKey, RateLimitProperties.Policy policy);
}
