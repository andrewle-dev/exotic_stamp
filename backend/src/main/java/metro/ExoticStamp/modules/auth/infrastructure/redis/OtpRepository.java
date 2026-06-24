package metro.ExoticStamp.modules.auth.infrastructure.redis;

import metro.ExoticStamp.infra.redis.RedisKeyValueSupport;
import metro.ExoticStamp.modules.auth.config.AuthSecurityProperties;
import metro.ExoticStamp.modules.auth.domain.model.OtpType;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@SuppressWarnings("null")
public class OtpRepository extends RedisKeyValueSupport {

    private static final String DOMAIN = "auth.otp";

    private static final String OTP_KEY_PATTERN = "auth:otp:%s:%s";
    private static final String COOLDOWN_PREFIX = "auth:otp:cooldown:";
    private static final String ATTEMPTS_PREFIX = "auth:otp:attempts:";
    private final AuthSecurityProperties authSecurityProperties;

    public OtpRepository(
            RedisTemplate<String, Object> redisTemplate,
            MeterRegistry meterRegistry,
            AuthSecurityProperties authSecurityProperties
    ) {
        super(redisTemplate, meterRegistry);
        this.authSecurityProperties = authSecurityProperties;
    }

    public void save(String email, OtpType type, String otp) {
        putValue(DOMAIN, key(email, type), otp, authSecurityProperties.getOtp().getTtl());
    }

    public Optional<String> find(String email, OtpType type) {
        return getValue(DOMAIN, key(email, type)).map(Object::toString);
    }

    public void delete(String email, OtpType type) {
        deleteValue(DOMAIN, key(email, type));
    }

    public boolean exists(String email, OtpType type) {
        return hasKey(DOMAIN, key(email, type), false);
    }

    public boolean isOnCooldown(String email, OtpType type) {
        return hasKey(DOMAIN, cooldownKey(email, type), false);
    }

    public void saveCooldown(String email, OtpType type) {
        putValue(DOMAIN, cooldownKey(email, type), "1", authSecurityProperties.getOtp().getCooldownTtl());
    }

    public long getCooldownTtlSeconds(String email, OtpType type) {
        return getTtlSeconds(DOMAIN, cooldownKey(email, type));
    }

    public boolean isMaxAttemptsExceeded(String email, OtpType type) {
        return getValue(DOMAIN, attemptsKey(email, type))
                .map(Object::toString)
                .map(value -> {
                    try {
                        return Integer.parseInt(value) >= authSecurityProperties.getOtp().getMaxAttempts();
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .orElse(false);
    }

    public void incrementAttempts(String email, OtpType type) {
        incrementWithTtl(DOMAIN, attemptsKey(email, type), authSecurityProperties.getOtp().getAttemptsTtl());
    }

    public int getAttemptsCount(String email, OtpType type) {
        return getValue(DOMAIN, attemptsKey(email, type))
                .map(Object::toString)
                .map(value -> {
                    try {
                        return Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .orElse(0);
    }

    private static String key(String email, OtpType type) {
        return String.format(OTP_KEY_PATTERN, type.name().toLowerCase(), email);
    }

    private static String cooldownKey(String email, OtpType type) {
        return COOLDOWN_PREFIX + type.name().toLowerCase() + ":" + email;
    }

    private static String attemptsKey(String email, OtpType type) {
        return ATTEMPTS_PREFIX + type.name().toLowerCase() + ":" + email;
    }
}

