package metro.ExoticStamp.modules.reward.infrastructure.cache;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.infra.cache.BaseCacheRepository;
import metro.ExoticStamp.modules.reward.application.port.RewardCachePort;
import metro.ExoticStamp.modules.reward.application.view.UserRewardView;
import metro.ExoticStamp.modules.reward.config.RewardProperties;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.Cursor;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
public class RewardCacheRepository extends BaseCacheRepository<UserRewardView> implements RewardCachePort {

    private final Duration listTtl;

    public RewardCacheRepository(
            RedisTemplate<String, Object> redisTemplate,
            MeterRegistry meterRegistry,
            RewardProperties rewardProperties
    ) {
        super(redisTemplate, meterRegistry);
        this.listTtl = rewardProperties.getUserRewardCacheTtl();
    }

    @Override
    protected String prefix() {
        return "user_reward:";
    }

    @Override
    protected Duration ttl() {
        return listTtl;
    }

    @Override
    protected Class<UserRewardView> type() {
        return UserRewardView.class;
    }

    @Override
    public Optional<UserRewardView> getUserRewardDetail(UUID userId, UUID userRewardId) {
        return getRawDetail(detailKey(userId, userRewardId));
    }

    @Override
    public void putUserRewardDetail(UUID userId, UUID userRewardId, UserRewardView view) {
        putString(detailKey(userId, userRewardId), view, listTtl);
    }

    @Override
    public void evictUserRewardDetail(UUID userId, UUID userRewardId) {
        deleteKey(detailKey(userId, userRewardId));
    }

    @Override
    public Optional<PageResponse<UserRewardView>> getUserRewardList(UUID userId, int page, int size) {
        return getPage(listKey(userId, page, size));
    }

    @Override
    public void putUserRewardList(UUID userId, int page, int size, PageResponse<UserRewardView> pageResponse) {
        putString(listKey(userId, page, size), pageResponse, listTtl);
    }

    @Override
    public void evictUserRewardListAll(UUID userId) {
        deleteKeysMatching("user_reward:list:" + userId + ":*");
    }

    private String detailKey(UUID userId, UUID userRewardId) {
        return "user_reward:detail:" + userId + ":" + userRewardId;
    }

    private String listKey(UUID userId, int page, int size) {
        return "user_reward:list:" + userId + ":" + page + ":" + size;
    }

    private Optional<UserRewardView> getRawDetail(String key) {
        Object raw = getStringRaw(key);
        if (raw == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(type().cast(raw));
        } catch (Exception e) {
            meterRegistry.counter("cache.error", "domain", "user_reward").increment();
            log.warn("[RewardCache] get detail failed key={}: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<PageResponse<UserRewardView>> getPage(String key) {
        Object raw = getStringRaw(key);
        if (raw == null) {
            return Optional.empty();
        }
        try {
            @SuppressWarnings("unchecked")
            PageResponse<UserRewardView> casted = (PageResponse<UserRewardView>) raw;
            return Optional.of(casted);
        } catch (Exception e) {
            meterRegistry.counter("cache.error", "domain", "user_reward").increment();
            log.warn("[RewardCache] get list failed key={}: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    private Object getStringRaw(String key) {
        try {
            Object raw = redisTemplate.opsForValue().get(key);
            if (raw == null) {
                meterRegistry.counter("cache.miss", "domain", "user_reward").increment();
                return null;
            }
            meterRegistry.counter("cache.hit", "domain", "user_reward").increment();
            return raw;
        } catch (Exception e) {
            meterRegistry.counter("cache.error", "domain", "user_reward").increment();
            log.warn("[RewardCache] get failed key={}: {}", key, e.getMessage());
            return null;
        }
    }

    private void putString(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.warn("[RewardCache] put failed key={}: {}", key, e.getMessage());
        }
    }

    private void deleteKey(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("[RewardCache] delete failed key={}: {}", key, e.getMessage());
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
            log.warn("[RewardCache] deleteKeysMatching pattern={}: {}", pattern, e.getMessage());
        }
    }
}
