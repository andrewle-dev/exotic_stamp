package metro.ExoticStamp.infra.security.ratelimit;

/**
 * Outcome of a token-bucket consume attempt.
 *
 * @param allowed            whether the request may proceed
 * @param retryAfterSeconds  suggested wait when denied (0 when allowed)
 */
public record RateLimitDecision(boolean allowed, long retryAfterSeconds) {
}
