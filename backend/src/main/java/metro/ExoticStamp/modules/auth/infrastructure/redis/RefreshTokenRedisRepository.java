package metro.ExoticStamp.modules.auth.infrastructure.redis;

import metro.ExoticStamp.config.CacheProperties;
import metro.ExoticStamp.infra.redis.RedisKeyValueSupport;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public class RefreshTokenRedisRepository extends RedisKeyValueSupport {

    private static final String DOMAIN = "auth.refresh_token";
    private static final String KEY_VALID_PATTERN = "auth:refresh_token:valid:%s:%s";
    private static final String KEY_REVOKED_PATTERN = "auth:refresh_token:revoked:%s";

    private final CacheProperties cacheProperties;

    public RefreshTokenRedisRepository(
            RedisTemplate<String, Object> redisTemplate,
            MeterRegistry meterRegistry,
            CacheProperties cacheProperties
    ) {
        super(redisTemplate, meterRegistry);
        this.cacheProperties = cacheProperties;
    }

    public void save(UUID userId, String deviceFp, String tokenHash) {
        putValue(DOMAIN, keyValid(userId, deviceFp), tokenHash, refreshTtl());
    }

    public Optional<String> findHash(UUID userId, String deviceFp) {
        return getValue(DOMAIN, keyValid(userId, deviceFp)).map(Object::toString);
    }

    public void revoke(UUID userId, String deviceFp, String tokenHash) {
        deleteValue(DOMAIN, keyValid(userId, deviceFp));
        putValue(DOMAIN, keyRevoked(tokenHash), userId.toString(), refreshTtl());
    }

    public void revokeAllForUser(UUID userId) {
        Set<String> keys = findKeys(DOMAIN, keyValid(userId, "*"));
        deleteValues(DOMAIN, keys);
    }

    public boolean isRevoked(String tokenHash) {
        // fail-safe: if Redis is unhealthy, treat token as revoked
        return hasKey(DOMAIN, keyRevoked(tokenHash), true);
    }

    private Duration refreshTtl() {
        return cacheProperties.getRefreshTokenTtl();
    }

    private static String keyValid(UUID userId, String deviceFp) {
        return String.format(KEY_VALID_PATTERN, userId, deviceFp);
    }

    private static String keyRevoked(String tokenHash) {
        return String.format(KEY_REVOKED_PATTERN, tokenHash);
    }
}

