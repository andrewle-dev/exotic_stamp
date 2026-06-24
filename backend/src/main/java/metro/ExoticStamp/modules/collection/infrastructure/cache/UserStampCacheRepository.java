package metro.ExoticStamp.modules.collection.infrastructure.cache;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.config.CacheProperties;
import metro.ExoticStamp.infra.cache.BaseCacheRepository;
import metro.ExoticStamp.modules.collection.application.port.UserStampCachePort;
import metro.ExoticStamp.modules.collection.application.view.ProgressView;
import metro.ExoticStamp.modules.collection.application.view.StampBookView;
import metro.ExoticStamp.modules.collection.application.view.UserStampView;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Repository;

import org.springframework.data.redis.core.Cursor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
public class UserStampCacheRepository extends BaseCacheRepository<Object> implements UserStampCachePort {

    private final Duration userStampsTtl;
    private final Duration userProgressTtl;
    private final Duration userHistoryTtl;
    private final Duration stampBookTtl;

    public UserStampCacheRepository(
            RedisTemplate<String, Object> redisTemplate,
            MeterRegistry meterRegistry,
            CacheProperties cacheProperties
    ) {
        super(redisTemplate, meterRegistry);
        this.userStampsTtl = cacheProperties.getTtl().getCollectionUserStamps();
        this.userProgressTtl = cacheProperties.getTtl().getCollectionUserProgress();
        this.userHistoryTtl = cacheProperties.getTtl().getCollectionUserHistory();
        this.stampBookTtl = cacheProperties.getTtl().getCollectionStampBook();
    }

    @Override
    protected String prefix() {
        return "collection:";
    }

    @Override
    protected Duration ttl() {
        return userStampsTtl;
    }

    @Override
    protected Class<Object> type() {
        return Object.class;
    }

    @Override
    public Optional<PageResponse<UserStampView>> getUserStamps(UUID userId, UUID lineId, UUID campaignId, int page, int size) {
        return getPage(userStampsKey(userId, lineId, campaignId, page, size));
    }

    @Override
    public void putUserStamps(UUID userId, UUID lineId, UUID campaignId, int page, int size, PageResponse<UserStampView> value) {
        putString(userStampsKey(userId, lineId, campaignId, page, size), value, userStampsTtl);
    }

    @Override
    public void evictUserStampsForLine(UUID userId, UUID lineId) {
        deleteKeysMatching("user-stamps:" + userId + ":" + lineId + ":*");
    }

    @Override
    public Optional<ProgressView> getUserProgress(UUID userId, UUID lineId) {
        Object raw = getStringRaw(userProgressKey(userId, lineId));
        if (raw == null) {
            return Optional.empty();
        }
        try {
            return Optional.of((ProgressView) raw);
        } catch (Exception e) {
            meterRegistry.counter("cache.error", "domain", "collection").increment();
            log.warn("[UserStampCache] get progress failed key={}: {}", userProgressKey(userId, lineId), e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void putUserProgress(UUID userId, UUID lineId, ProgressView value) {
        putString(userProgressKey(userId, lineId), value, userProgressTtl);
    }

    @Override
    public void evictUserProgress(UUID userId, UUID lineId) {
        deleteKey(userProgressKey(userId, lineId));
    }

    @Override
    public Optional<PageResponse<UserStampView>> getUserHistory(UUID userId, int page, int size) {
        return getPage(userHistoryKey(userId, page, size));
    }

    @Override
    public void putUserHistory(UUID userId, int page, int size, PageResponse<UserStampView> value) {
        putString(userHistoryKey(userId, page, size), value, userHistoryTtl);
    }

    @Override
    public void evictUserHistoryAll(UUID userId) {
        deleteKeysMatching("user-history:" + userId + ":*");
    }

    @Override
    public Optional<StampBookView> getStampBook(UUID userId, UUID lineId, UUID campaignId) {
        Object raw = getStringRaw(stampBookKey(userId, lineId, campaignId));
        if (raw == null) {
            return Optional.empty();
        }
        try {
            return Optional.of((StampBookView) raw);
        } catch (Exception e) {
            meterRegistry.counter("cache.error", "domain", "collection").increment();
            log.warn("[UserStampCache] get stamp-book failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void putStampBook(UUID userId, UUID lineId, UUID campaignId, StampBookView value) {
        putString(stampBookKey(userId, lineId, campaignId), value, stampBookTtl);
    }

    @Override
    public void evictStampBook(UUID userId, UUID lineId, UUID campaignId) {
        deleteKey(stampBookKey(userId, lineId, campaignId));
    }

    @Override
    public void evictAllForUserCollection(UUID userId, UUID lineId, UUID campaignId) {
        evictUserStampsForLine(userId, lineId);
        evictUserProgress(userId, lineId);
        evictStampBook(userId, lineId, campaignId);
        evictUserHistoryAll(userId);
    }

    private String userStampsKey(UUID userId, UUID lineId, UUID campaignId, int page, int size) {
        String c = campaignId != null ? campaignId.toString() : "default";
        return "user-stamps:" + userId + ":" + lineId + ":" + c + ":" + page + ":" + size;
    }

    private String userProgressKey(UUID userId, UUID lineId) {
        return "user-progress:" + userId + ":" + lineId;
    }

    private String userHistoryKey(UUID userId, int page, int size) {
        return "user-history:" + userId + ":" + page + ":" + size;
    }

    private String stampBookKey(UUID userId, UUID lineId, UUID campaignId) {
        String c = campaignId != null ? campaignId.toString() : "default";
        return "user-stamp-book:" + userId + ":" + lineId + ":" + c;
    }

    private Optional<PageResponse<UserStampView>> getPage(String key) {
        Object raw = getStringRaw(key);
        if (raw == null) {
            return Optional.empty();
        }
        try {
            @SuppressWarnings("unchecked")
            PageResponse<UserStampView> casted = (PageResponse<UserStampView>) raw;
            return Optional.of(casted);
        } catch (Exception e) {
            meterRegistry.counter("cache.error", "domain", "collection").increment();
            log.warn("[UserStampCache] get page failed key={}: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    private Object getStringRaw(String key) {
        try {
            Object raw = redisTemplate.opsForValue().get(key);
            if (raw == null) {
                meterRegistry.counter("cache.miss", "domain", "collection").increment();
                return null;
            }
            meterRegistry.counter("cache.hit", "domain", "collection").increment();
            return raw;
        } catch (Exception e) {
            meterRegistry.counter("cache.error", "domain", "collection").increment();
            log.warn("[UserStampCache] get failed key={}: {}", key, e.getMessage());
            return null;
        }
    }

    private void putString(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.warn("[UserStampCache] put failed key={}: {}", key, e.getMessage());
        }
    }

    private void deleteKey(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("[UserStampCache] delete failed key={}: {}", key, e.getMessage());
        }
    }

    private void deleteKeysMatching(String pattern) {
        try {
            List<String> keys = redisTemplate.execute((RedisCallback<List<String>>) connection -> {
                List<String> out = new ArrayList<>();
                ScanOptions options = ScanOptions.scanOptions().match(pattern).count(256).build();
                try (Cursor<byte[]> cursor = connection.scan(options)) {
                    while (cursor.hasNext()) {
                        out.add(new String(cursor.next()));
                    }
                }
                return out;
            });
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("[UserStampCache] deleteKeysMatching pattern={}: {}", pattern, e.getMessage());
        }
    }
}
