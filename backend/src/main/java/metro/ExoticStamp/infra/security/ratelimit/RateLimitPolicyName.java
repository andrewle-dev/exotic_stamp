package metro.ExoticStamp.infra.security.ratelimit;

public enum RateLimitPolicyName {
    LOGIN,
    REGISTER,
    OTP_ISSUE,
    OTP_VERIFY,
    REFRESH,
    SCAN_RESOLVE,
    COLLECT
}
