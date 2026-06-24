package metro.ExoticStamp.modules.auth.infrastructure.redis;

import metro.ExoticStamp.config.CacheProperties;
import metro.ExoticStamp.infra.redis.RedisKeyValueSupport;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Redis cache/denylist for access-token revocation (not source of truth; DB holds {@code token_version}).
 */
@Repository
public class AccessTokenRevocationRedisRepository extends RedisKeyValueSupport {

    private static final String DOMAIN = "auth.access_revocation";
    private static final String KEY_DENYLIST = "denylist:%s";
    private static final String KEY_USER_VERSION = "user:%s:tokenVersion";
    private static final String KEY_DEVICE_JTI = "auth:access_jti:%s:%s";

    private static final String DENYLIST_PLACEHOLDER = "1";

    private final CacheProperties cacheProperties;

    public AccessTokenRevocationRedisRepository(
            RedisTemplate<String, Object> redisTemplate,
            MeterRegistry meterRegistry,
            CacheProperties cacheProperties
    ) {
        super(redisTemplate, meterRegistry);
        this.cacheProperties = cacheProperties;
    }

    public void addToDenylist(String jti, Duration accessTokenTtl) {
        putValue(DOMAIN, keyDenylist(jti), DENYLIST_PLACEHOLDER, accessTokenTtl);
    }

    /** Redis down → {@code false} (fail-open: do not treat as revoked). */
    public boolean isDenylisted(String jti) {
        return hasKey(DOMAIN, keyDenylist(jti), false);
    }

    public Optional<Long> getCachedTokenVersion(UUID userId) {
        Optional<Object> raw = getValue(DOMAIN, keyUserVersion(userId));
        return raw.flatMap(AccessTokenRevocationRedisRepository::toLong);
    }

    public void setCachedTokenVersion(UUID userId, long version) {
        putValue(DOMAIN, keyUserVersion(userId), version, userVersionCacheTtl());
    }

    public void setDeviceAccessJti(UUID userId, String deviceFingerprint, String jti) {
        putValue(DOMAIN, keyDeviceJti(userId, deviceFingerprint), jti, cacheProperties.getRefreshTokenTtl());
    }

    public Optional<String> getDeviceAccessJti(UUID userId, String deviceFingerprint) {
        return getValue(DOMAIN, keyDeviceJti(userId, deviceFingerprint)).map(Object::toString);
    }

    public void deleteDeviceAccessJti(UUID userId, String deviceFingerprint) {
        deleteValue(DOMAIN, keyDeviceJti(userId, deviceFingerprint));
    }

    private Duration userVersionCacheTtl() {
        return cacheProperties.getAccessTokenVersionTtl();
    }

    private static String keyDenylist(String jti) {
        return String.format(KEY_DENYLIST, jti);
    }

    private static String keyUserVersion(UUID userId) {
        return String.format(KEY_USER_VERSION, userId);
    }

    private static String keyDeviceJti(UUID userId, String deviceFingerprint) {
        return String.format(KEY_DEVICE_JTI, userId, deviceFingerprint);
    }

    private static Optional<Long> toLong(Object o) {
        if (o == null) {
            return Optional.empty();
        }
        if (o instanceof Number n) {
            return Optional.of(n.longValue());
        }
        try {
            return Optional.of(Long.parseLong(o.toString()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
