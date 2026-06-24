package metro.ExoticStamp.modules.collection.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.collection.application.port.StampCollectedDedupPort;
import metro.ExoticStamp.modules.collection.config.CollectionProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StampCollectedDedupRedisRepository implements StampCollectedDedupPort {

    private static final String PREFIX = "stamp-collected:event:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final CollectionProperties collectionProperties;

    @Override
    public boolean claimFirstProcessing(UUID eventId) {
        try {
            String key = PREFIX + eventId;
            Duration ttl = collectionProperties.getStampCollectedEventDedupTtl();
            Boolean first = redisTemplate.opsForValue().setIfAbsent(key, "1", ttl);
            return Boolean.TRUE.equals(first);
        } catch (Exception e) {
            log.warn("[Collection] stamp collected dedup unavailable eventId={}: {}", eventId, e.getMessage());
            return true;
        }
    }
}
