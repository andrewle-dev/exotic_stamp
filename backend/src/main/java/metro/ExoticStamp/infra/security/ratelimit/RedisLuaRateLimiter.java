package metro.ExoticStamp.infra.security.ratelimit;

import metro.ExoticStamp.common.exceptions.security.SecurityDependencyUnavailableException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Atomic Redis token-bucket limiter. Fail-closed on Redis errors
 * ({@link SecurityDependencyUnavailableException}).
 */
@Component
@ConditionalOnProperty(
        name = "application.security.rate-limit.backend",
        havingValue = "redis",
        matchIfMissing = true
)
public class RedisLuaRateLimiter implements RateLimiter {

    private static final String LUA = """
            local key = KEYS[1]
            local capacity = tonumber(ARGV[1])
            local refill = tonumber(ARGV[2])
            local period = tonumber(ARGV[3])
            local now = tonumber(ARGV[4])
            local requested = tonumber(ARGV[5])
            local ttl = tonumber(ARGV[6])
            local data = redis.call('GET', key)
            local tokens
            local last
            if data == false then
              tokens = capacity
              last = now
            else
              local sep = string.find(data, ':')
              tokens = tonumber(string.sub(data, 1, sep-1))
              last = tonumber(string.sub(data, sep+1))
            end
            local elapsed = math.max(0, now - last)
            local add = math.floor(elapsed / period) * refill
            if add > 0 then
              tokens = math.min(capacity, tokens + add)
              last = last + math.floor(elapsed / period) * period
            end
            local allowed = 0
            local retry = 0
            if tokens >= requested then
              tokens = tokens - requested
              allowed = 1
            else
              local need = requested - tokens
              retry = math.ceil((need / refill) * period / 1000)
              if retry < 1 then retry = 1 end
            end
            redis.call('SET', key, tostring(tokens) .. ':' .. tostring(last), 'PX', ttl)
            return {allowed, retry, tokens}
            """;

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List> script;

    public RedisLuaRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.script = new DefaultRedisScript<>();
        this.script.setScriptText(LUA);
        this.script.setResultType(List.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public RateLimitDecision tryConsume(String bucketKey, RateLimitProperties.Policy policy) {
        long capacity = Math.max(1, policy.getCapacity());
        long refillTokens = Math.max(1, policy.getRefillTokens());
        long refillPeriodMs = Math.max(1, policy.getRefillPeriod().toMillis());
        long ttlMs = Math.max(refillPeriodMs, policy.getTtl().toMillis());
        long nowMs = System.currentTimeMillis();

        try {
            List<Long> result = (List<Long>) redisTemplate.execute(
                    script,
                    Collections.singletonList(bucketKey),
                    String.valueOf(capacity),
                    String.valueOf(refillTokens),
                    String.valueOf(refillPeriodMs),
                    String.valueOf(nowMs),
                    "1",
                    String.valueOf(ttlMs)
            );
            if (result == null || result.size() < 2) {
                throw new SecurityDependencyUnavailableException();
            }
            long allowed = toLong(result.get(0));
            long retry = toLong(result.get(1));
            if (allowed == 1) {
                return new RateLimitDecision(true, 0);
            }
            // Prefer computed wait; fall back to TTL seconds if script returned 0
            long retryAfter = retry > 0 ? retry : Math.max(1, (ttlMs + 999) / 1000);
            return new RateLimitDecision(false, retryAfter);
        } catch (SecurityDependencyUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new SecurityDependencyUnavailableException(e);
        }
    }

    private static long toLong(Object value) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
}
