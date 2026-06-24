package metro.ExoticStamp.modules.collection.infrastructure;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import metro.ExoticStamp.config.CacheProperties;
import metro.ExoticStamp.modules.collection.application.view.ProgressView;
import metro.ExoticStamp.modules.collection.infrastructure.cache.UserStampCacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class UserStampCacheIntegrationIT {

    @Container
    @SuppressWarnings("resource")
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    private UserStampCacheRepository cache;

    @BeforeEach
    void setUp() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redis.getHost(), redis.getMappedPort(6379));
        factory.afterPropertiesSet();
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();

        CacheProperties cacheProperties = new CacheProperties();
        cacheProperties.getTtl().setCollectionUserStamps(Duration.ofMinutes(10));
        cacheProperties.getTtl().setCollectionUserProgress(Duration.ofMinutes(10));
        cacheProperties.getTtl().setCollectionUserHistory(Duration.ofMinutes(15));
        cacheProperties.getTtl().setCollectionStampBook(Duration.ofMinutes(15));

        cache = new UserStampCacheRepository(template, new SimpleMeterRegistry(), cacheProperties);
    }

    @Test
    void progress_putGetEvict_roundTrip() {
        UUID userId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        ProgressView pv = ProgressView.builder().lineId(lineId).collected(1).total(10).percentage(10).build();
        cache.putUserProgress(userId, lineId, pv);
        Optional<ProgressView> got = cache.getUserProgress(userId, lineId);
        assertTrue(got.isPresent());
        assertEquals(10, got.get().percentage());
        cache.evictUserProgress(userId, lineId);
        assertTrue(cache.getUserProgress(userId, lineId).isEmpty());
    }

    @Test
    void evictAllForUserCollection_clearsProgress() {
        UUID userId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        cache.putUserProgress(userId, lineId, ProgressView.builder().lineId(lineId).collected(0).total(1).percentage(0).build());
        cache.evictAllForUserCollection(userId, lineId, campaignId);
        assertTrue(cache.getUserProgress(userId, lineId).isEmpty());
    }
}
