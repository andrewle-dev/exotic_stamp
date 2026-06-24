package metro.ExoticStamp.modules.auth.infrastructure.redis;

import metro.ExoticStamp.infra.redis.RedisKeyValueSupport;
import metro.ExoticStamp.modules.auth.config.AuthSecurityProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class VerifyTokenRepository extends RedisKeyValueSupport {

    private static final String DOMAIN = "auth.verify_token";

    private static final String TOKEN_PREFIX = "auth:verify:token:";
    private static final String COOLDOWN_PREFIX = "auth:verify:cooldown:";
    private static final String OWNER_PREFIX = "auth:verify:owner:";
    private final AuthSecurityProperties authSecurityProperties;

    public VerifyTokenRepository(
            RedisTemplate<String, Object> redisTemplate,
            MeterRegistry meterRegistry,
            AuthSecurityProperties authSecurityProperties
    ) {
        super(redisTemplate, meterRegistry);
        this.authSecurityProperties = authSecurityProperties;
    }

    public void saveToken(String token, UUID userId) {
        String ownerKey = OWNER_PREFIX + userId;
        Optional<Object> existing = getValue(DOMAIN, ownerKey);
        if (existing.isPresent()) {
            String oldToken = existing.get().toString();
            deleteValue(DOMAIN, TOKEN_PREFIX + oldToken);
            deleteValue(DOMAIN, ownerKey);
        }

        putValue(DOMAIN, TOKEN_PREFIX + token, userId.toString(), authSecurityProperties.getVerification().getTokenTtl());
        putValue(DOMAIN, ownerKey, token, authSecurityProperties.getVerification().getTokenTtl());
    }

    public Optional<UUID> findUserIdByToken(String token) {
        return getValue(DOMAIN, TOKEN_PREFIX + token)
                .map(Object::toString)
                .map(UUID::fromString);
    }

    public void deleteToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        Optional<Object> userIdObj = getValue(DOMAIN, tokenKey);
        deleteValue(DOMAIN, tokenKey);
        userIdObj.ifPresent(uid -> deleteValue(DOMAIN, OWNER_PREFIX + uid));
    }

    public boolean isOnCooldown(String email) {
        return hasKey(DOMAIN, COOLDOWN_PREFIX + email, false);
    }

    public void saveCooldown(String email) {
        putValue(
                DOMAIN,
                COOLDOWN_PREFIX + email,
                "1",
                authSecurityProperties.getVerification().getResendCooldownTtl()
        );
    }

    public long getCooldownTtlSeconds(String email) {
        return getTtlSeconds(DOMAIN, COOLDOWN_PREFIX + email);
    }
}
