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
import java.util.concurrent.atomic.AtomicBoolean;

@Repository
public class RefreshTokenRedisRepository extends RedisKeyValueSupport {

    private static final String DOMAIN = "auth.refresh_token";
    private static final String KEY_VALID_PATTERN = "auth:refresh_token:valid:%s:%s";
    private static final String KEY_REVOKED_PATTERN = "auth:refresh_token:revoked:%s";
    private static final String KEY_GRACE_PATTERN = "auth:refresh_token:grace:%s";
    private static final String GRACE_SEPARATOR = "\n";

    private final CacheProperties cacheProperties;
    private final AtomicBoolean lastOpHealthy = new AtomicBoolean(true);

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

    /**
     * Explicit revoked-key presence only. Redis errors → false (do not treat as revoked).
     */
    public boolean isKnownRevoked(String tokenHash) {
        try {
            Boolean has = redisTemplate.hasKey(keyRevoked(tokenHash));
            lastOpHealthy.set(true);
            return Boolean.TRUE.equals(has);
        } catch (Exception e) {
            lastOpHealthy.set(false);
            markError(DOMAIN);
            return false;
        }
    }

    public boolean isHealthy() {
        return lastOpHealthy.get();
    }

    public void putGraceCredentials(String oldTokenHash, String accessToken, String refreshToken, Duration ttl) {
        String payload = accessToken + GRACE_SEPARATOR + refreshToken;
        putValue(DOMAIN, keyGrace(oldTokenHash), payload, ttl);
    }

    public Optional<GracePayload> findGraceCredentials(String oldTokenHash) {
        return getValue(DOMAIN, keyGrace(oldTokenHash)).map(Object::toString).flatMap(raw -> {
            int idx = raw.indexOf(GRACE_SEPARATOR);
            if (idx <= 0 || idx >= raw.length() - 1) {
                return Optional.empty();
            }
            return Optional.of(new GracePayload(raw.substring(0, idx), raw.substring(idx + 1)));
        });
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

    private static String keyGrace(String tokenHash) {
        return String.format(KEY_GRACE_PATTERN, tokenHash);
    }

    public record GracePayload(String accessToken, String refreshToken) {}
}
