package metro.ExoticStamp.infra.cache;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

// Global cache repo for other domain reutilize
// Kết quả visible trên Grafana/Datadog — biết được cache đang hoạt động tốt không.
@Slf4j
public abstract class BaseCacheRepository<T> {

    protected final RedisTemplate<String, Object> redisTemplate;
    protected final MeterRegistry meterRegistry;

    protected BaseCacheRepository(RedisTemplate<String, Object> redisTemplate, MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.meterRegistry = meterRegistry;
    }

    protected abstract String prefix();
    protected abstract Duration ttl();
    protected abstract Class<T> type();

    public void put(UUID id, T value) {
        try {
            redisTemplate.opsForValue().set(prefix() + id, value, ttl());
        } catch (Exception e) {
            log.warn("[{}Cache] put failed id={}: {}", type().getSimpleName(), id, e.getMessage());
        }
    }

    public Optional<T> get(UUID id) {
        try {
            Object raw = redisTemplate.opsForValue().get(prefix() + id);
            if (raw == null) {
                meterRegistry.counter("cache.miss", "domain", metricDomain()).increment();
                return Optional.empty();
            }
            meterRegistry.counter("cache.hit", "domain", metricDomain()).increment();
            return Optional.of(type().cast(raw));
        } catch (Exception e) {
            meterRegistry.counter("cache.error", "domain", metricDomain()).increment();
            log.warn("[{}Cache] get failed id={}: {}", type().getSimpleName(), id, e.getMessage());
            return Optional.empty();
        }
    }

    public void evict(UUID id) {
        try {
            redisTemplate.delete(prefix() + id);
        } catch (Exception e) {
            log.warn("[{}Cache] evict failed id={}: {}", type().getSimpleName(), id, e.getMessage());
        }
    }

    private String metricDomain() {
        String p = prefix();
        if (p == null || p.isBlank()) {
            return "unknown";
        }
        return p.endsWith(":") ? p.substring(0, p.length() - 1) : p;
    }
}
