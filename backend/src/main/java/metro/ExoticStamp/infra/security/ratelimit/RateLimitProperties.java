package metro.ExoticStamp.infra.security.ratelimit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "application.security.rate-limit")
@Getter
@Setter
public class RateLimitProperties {

    /**
     * Master switch. When false, all rate-limit checks are skipped.
     */
    private boolean enabled = true;

    /**
     * HMAC pepper for hashing identifiers into rate-limit keys.
     * Bound from {@code RATE_LIMIT_KEY_PEPPER} / {@code application.security.rate-limit.key-pepper}.
     */
    private String keyPepper;

    /**
     * Storage backend: {@code redis} (default) or {@code memory} (tests/dev only).
     */
    private String backend = "redis";

    private Policy login = defaultPolicy(10, 10, Duration.ofMinutes(1), Duration.ofMinutes(2));
    private Policy register = defaultPolicy(5, 5, Duration.ofMinutes(1), Duration.ofMinutes(2));
    private Policy otpIssue = defaultPolicy(5, 5, Duration.ofMinutes(1), Duration.ofMinutes(2));
    private Policy otpVerify = defaultPolicy(10, 10, Duration.ofMinutes(1), Duration.ofMinutes(2));
    private Policy refresh = defaultPolicy(30, 30, Duration.ofMinutes(1), Duration.ofMinutes(2));
    private Policy scanResolve = defaultPolicy(60, 60, Duration.ofMinutes(1), Duration.ofMinutes(2));
    private Policy collect = defaultPolicy(30, 30, Duration.ofMinutes(1), Duration.ofMinutes(2));

    @Getter
    @Setter
    public static class Policy {
        private boolean enabled = true;
        private long capacity = 10;
        private long refillTokens = 10;
        private Duration refillPeriod = Duration.ofMinutes(1);
        private Duration ttl = Duration.ofMinutes(2);
    }

    private static Policy defaultPolicy(long capacity, long refillTokens, Duration refillPeriod, Duration ttl) {
        Policy p = new Policy();
        p.setEnabled(true);
        p.setCapacity(capacity);
        p.setRefillTokens(refillTokens);
        p.setRefillPeriod(refillPeriod);
        p.setTtl(ttl);
        return p;
    }

    public Policy policyFor(RateLimitPolicyName name) {
        return switch (name) {
            case LOGIN -> login;
            case REGISTER -> register;
            case OTP_ISSUE -> otpIssue;
            case OTP_VERIFY -> otpVerify;
            case REFRESH -> refresh;
            case SCAN_RESOLVE -> scanResolve;
            case COLLECT -> collect;
        };
    }
}
