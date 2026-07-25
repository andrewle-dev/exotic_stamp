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
        AuthSecurityProperties.PurposeSettings settings = settingsFor(type);
        putValueRequired(DOMAIN, key(email, type), otp, settings.getTtl());
    }

    public Optional<String> find(String email, OtpType type) {
        return getValueRequired(DOMAIN, key(email, type)).map(Object::toString);
    }

    public void delete(String email, OtpType type) {
        deleteValueRequired(DOMAIN, key(email, type));
    }

    public boolean exists(String email, OtpType type) {
        return hasKeyRequired(DOMAIN, key(email, type));
    }

    public boolean isOnCooldown(String email, OtpType type) {
        return hasKeyRequired(DOMAIN, cooldownKey(email, type));
    }

    public void saveCooldown(String email, OtpType type) {
        AuthSecurityProperties.PurposeSettings settings = settingsFor(type);
        putValueRequired(DOMAIN, cooldownKey(email, type), "1", settings.getCooldownTtl());
    }

    public long getCooldownTtlSeconds(String email, OtpType type) {
        return getTtlSecondsRequired(DOMAIN, cooldownKey(email, type));
    }

    public boolean isMaxAttemptsExceeded(String email, OtpType type) {
        AuthSecurityProperties.PurposeSettings settings = settingsFor(type);
        return getValueRequired(DOMAIN, attemptsKey(email, type))
                .map(Object::toString)
                .map(value -> {
                    try {
                        return Integer.parseInt(value) >= settings.getMaxAttempts();
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .orElse(false);
    }

    public void incrementAttempts(String email, OtpType type) {
        AuthSecurityProperties.PurposeSettings settings = settingsFor(type);
        incrementWithTtlRequired(DOMAIN, attemptsKey(email, type), settings.getAttemptsTtl());
    }

    public int getAttemptsCount(String email, OtpType type) {
        return getValueRequired(DOMAIN, attemptsKey(email, type))
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

    private AuthSecurityProperties.PurposeSettings settingsFor(OtpType type) {
        return authSecurityProperties.getOtp().forType(type);
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
