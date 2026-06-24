package metro.ExoticStamp.infra.redis;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Slf4j
public abstract class RedisKeyValueSupport {

    protected final RedisTemplate<String, Object> redisTemplate;
    protected final MeterRegistry meterRegistry;

    protected RedisKeyValueSupport(RedisTemplate<String, Object> redisTemplate, MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.meterRegistry = meterRegistry;
    }

    protected void putValue(String domain, String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            markError(domain);
            log.warn("[Redis][{}] put failed key={} err={}", domain, key, e.getMessage());
        }
    }

    protected Optional<Object> getValue(String domain, String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                markMiss(domain);
                return Optional.empty();
            }
            markHit(domain);
            return Optional.of(value);
        } catch (Exception e) {
            markError(domain);
            log.warn("[Redis][{}] get failed key={} err={}", domain, key, e.getMessage());
            return Optional.empty();
        }
    }

    protected void deleteValue(String domain, String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            markError(domain);
            log.warn("[Redis][{}] delete failed key={} err={}", domain, key, e.getMessage());
        }
    }

    protected void deleteValues(String domain, Set<String> keys) {
        try {
            if (keys == null || keys.isEmpty()) {
                return;
            }
            redisTemplate.delete(keys);
        } catch (Exception e) {
            markError(domain);
            log.warn("[Redis][{}] bulk delete failed err={}", domain, e.getMessage());
        }
    }

    protected Set<String> findKeys(String domain, String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            return keys != null ? keys : Collections.emptySet();
        } catch (Exception e) {
            markError(domain);
            log.warn("[Redis][{}] keys lookup failed pattern={} err={}", domain, pattern, e.getMessage());
            return Collections.emptySet();
        }
    }

    protected boolean hasKey(String domain, String key, boolean fallbackOnError) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            markError(domain);
            log.warn("[Redis][{}] hasKey failed key={} err={}", domain, key, e.getMessage());
            return fallbackOnError;
        }
    }

    protected long getTtlSeconds(String domain, String key) {
        try {
            Long seconds = redisTemplate.getExpire(key, java.util.concurrent.TimeUnit.SECONDS);
            return seconds != null ? seconds : -1;
        } catch (Exception e) {
            markError(domain);
            log.warn("[Redis][{}] getExpire failed key={} err={}", domain, key, e.getMessage());
            return -1;
        }
    }

    protected long incrementWithTtl(String domain, String key, Duration ttl) {
        try {
            Long current = redisTemplate.opsForValue().increment(key);
            if (current != null && current == 1L) {
                redisTemplate.expire(key, ttl);
            }
            return current != null ? current : 0L;
        } catch (Exception e) {
            markError(domain);
            log.warn("[Redis][{}] increment failed key={} err={}", domain, key, e.getMessage());
            return 0L;
        }
    }

    private void markHit(String domain) {
        meterRegistry.counter("cache.hit", "domain", domain).increment();
    }

    private void markMiss(String domain) {
        meterRegistry.counter("cache.miss", "domain", domain).increment();
    }

    private void markError(String domain) {
        meterRegistry.counter("cache.error", "domain", domain).increment();
    }
}
