package metro.ExoticStamp.infra.security.ratelimit;

import metro.ExoticStamp.common.exceptions.security.SecurityDependencyUnavailableException;

/**
 * Orchestrates policy lookup and token-bucket consumption.
 */
public class RateLimitService {

    private final RateLimitProperties properties;
    private final RateLimiter rateLimiter;

    public RateLimitService(RateLimitProperties properties, RateLimiter rateLimiter) {
        this.properties = properties;
        this.rateLimiter = rateLimiter;
    }

    /**
     * Attempts to consume one token for the given policy and bucket key.
     *
     * @throws RateLimitExceededException              when the bucket is empty
     * @throws SecurityDependencyUnavailableException  when the backing store fails (fail-closed)
     */
    public void tryConsume(RateLimitPolicyName policyName, String bucketKey) {
        if (!properties.isEnabled()) {
            return;
        }
        RateLimitProperties.Policy policy = properties.policyFor(policyName);
        if (policy == null || !policy.isEnabled()) {
            return;
        }
        RateLimitDecision decision = rateLimiter.tryConsume(bucketKey, policy);
        if (!decision.allowed()) {
            throw new RateLimitExceededException(decision.retryAfterSeconds());
        }
    }
}
