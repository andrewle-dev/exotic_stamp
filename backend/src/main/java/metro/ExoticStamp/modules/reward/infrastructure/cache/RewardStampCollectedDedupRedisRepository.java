package metro.ExoticStamp.modules.reward.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.reward.application.port.RewardStampCollectedDedupPort;
import metro.ExoticStamp.modules.reward.config.RewardProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RewardStampCollectedDedupRedisRepository implements RewardStampCollectedDedupPort {

    private static final String DONE_PREFIX = "reward:dedup:done:";
    private static final String LOCK_PREFIX = "reward:dedup:lock:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final RewardProperties rewardProperties;

    @Override
    public boolean isProcessed(UUID eventId) {
        try {
            Boolean exists = redisTemplate.hasKey(doneKey(eventId));
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.warn("[Reward] dedup check unavailable eventId={}: {}", eventId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean acquireProcessingLock(UUID eventId) {
        try {
            Duration ttl = rewardProperties.getStampCollectedEventProcessingLockTtl();
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey(eventId), "1", ttl);
            return Boolean.TRUE.equals(acquired);
        } catch (Exception e) {
            log.warn("[Reward] processing lock unavailable eventId={}: {}", eventId, e.getMessage());
            return true;
        }
    }

    @Override
    public void markProcessed(UUID eventId) {
        try {
            Duration ttl = rewardProperties.getStampCollectedEventDedupTtl();
            redisTemplate.opsForValue().set(doneKey(eventId), "1", ttl);
        } catch (Exception e) {
            log.warn("[Reward] mark processed failed eventId={}: {}", eventId, e.getMessage());
        }
    }

    @Override
    public void releaseProcessingLock(UUID eventId) {
        try {
            redisTemplate.delete(lockKey(eventId));
        } catch (Exception e) {
            log.warn("[Reward] release lock failed eventId={}: {}", eventId, e.getMessage());
        }
    }

    private static String doneKey(UUID eventId) {
        return DONE_PREFIX + eventId;
    }

    private static String lockKey(UUID eventId) {
        return LOCK_PREFIX + eventId;
    }
}
