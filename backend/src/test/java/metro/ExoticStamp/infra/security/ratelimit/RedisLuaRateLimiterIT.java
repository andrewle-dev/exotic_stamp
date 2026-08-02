package metro.ExoticStamp.infra.security.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Docker-backed proof that Redis Lua rate limiting is atomic under concurrency.
 */
@Testcontainers(disabledWithoutDocker = true)
class RedisLuaRateLimiterIT {

    @Container
    @SuppressWarnings("resource")
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Test
    void concurrentConsumes_doNotExceedCapacity() throws Exception {
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(
                redis.getHost(), redis.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();
        connectionFactory.start();

        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        template.afterPropertiesSet();

        try {
            RedisLuaRateLimiter limiter = new RedisLuaRateLimiter(template);
            RateLimitProperties.Policy policy = new RateLimitProperties.Policy();
            policy.setEnabled(true);
            policy.setCapacity(20);
            policy.setRefillTokens(20);
            policy.setRefillPeriod(Duration.ofMinutes(1));
            policy.setTtl(Duration.ofMinutes(2));

            String key = "rl:it:concurrent:" + System.nanoTime();
            int threads = 40;
            ExecutorService pool = Executors.newFixedThreadPool(threads);
            AtomicInteger allowed = new AtomicInteger();
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                tasks.add(() -> {
                    if (limiter.tryConsume(key, policy).allowed()) {
                        allowed.incrementAndGet();
                    }
                    return null;
                });
            }
            List<Future<Void>> futures = pool.invokeAll(tasks);
            for (Future<Void> f : futures) {
                f.get();
            }
            pool.shutdown();

            assertEquals(20, allowed.get());
            assertTrue(limiter.tryConsume(key, policy).retryAfterSeconds() >= 1);
        } finally {
            connectionFactory.destroy();
        }
    }
}
