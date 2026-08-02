package metro.ExoticStamp.infra.redis;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.common.exceptions.security.SecurityDependencyUnavailableException;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Slf4j
public abstract class RedisKeyValueSupport {

    private static final String UNAVAILABLE_MESSAGE = "Security dependency temporarily unavailable";

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

    /**
     * Fail-closed put for security-critical keys (OTP, denylist writes).
     */
    protected void putValueRequired(String domain, String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            markError(domain);
            log.warn("[Redis][{}] put required failed key={}", domain, key);
            throw new SecurityDependencyUnavailableException(UNAVAILABLE_MESSAGE, e);
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

    /**
     * Fail-closed get; empty is allowed (key miss). Redis errors throw.
     */
    protected Optional<Object> getValueRequired(String domain, String key) {
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
            log.warn("[Redis][{}] get required failed key={}", domain, key);
            throw new SecurityDependencyUnavailableException(UNAVAILABLE_MESSAGE, e);
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

    protected void deleteValueRequired(String domain, String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            markError(domain);
            log.warn("[Redis][{}] delete required failed key={}", domain, key);
            throw new SecurityDependencyUnavailableException(UNAVAILABLE_MESSAGE, e);
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

    protected boolean hasKeyRequired(String domain, String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            markError(domain);
            log.warn("[Redis][{}] hasKey required failed key={}", domain, key);
            throw new SecurityDependencyUnavailableException(UNAVAILABLE_MESSAGE, e);
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

    protected long getTtlSecondsRequired(String domain, String key) {
        try {
            Long seconds = redisTemplate.getExpire(key, java.util.concurrent.TimeUnit.SECONDS);
            return seconds != null ? seconds : -1;
        } catch (Exception e) {
            markError(domain);
            log.warn("[Redis][{}] getExpire required failed key={}", domain, key);
            throw new SecurityDependencyUnavailableException(UNAVAILABLE_MESSAGE, e);
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

    protected long incrementWithTtlRequired(String domain, String key, Duration ttl) {
        try {
            Long current = redisTemplate.opsForValue().increment(key);
            if (current != null && current == 1L) {
                redisTemplate.expire(key, ttl);
            }
            return current != null ? current : 0L;
        } catch (Exception e) {
            markError(domain);
            log.warn("[Redis][{}] increment required failed key={}", domain, key);
            throw new SecurityDependencyUnavailableException(UNAVAILABLE_MESSAGE, e);
        }
    }

    private void markHit(String domain) {
        meterRegistry.counter("cache.hit", "domain", domain).increment();
    }

    private void markMiss(String domain) {
        meterRegistry.counter("cache.miss", "domain", domain).increment();
    }

    protected void markError(String domain) {
        meterRegistry.counter("cache.error", "domain", domain).increment();
    }
}
