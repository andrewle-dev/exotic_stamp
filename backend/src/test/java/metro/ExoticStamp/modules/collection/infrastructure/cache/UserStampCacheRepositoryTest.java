package metro.ExoticStamp.modules.collection.infrastructure.cache;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.config.CacheProperties;
import metro.ExoticStamp.modules.collection.application.view.ProgressView;
import metro.ExoticStamp.modules.collection.application.view.StampBookView;
import metro.ExoticStamp.modules.collection.application.view.UserStampView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserStampCacheRepositoryTest {

    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations;

    private UserStampCacheRepository repository;
    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        CacheProperties cacheProperties = new CacheProperties();
        repository = new UserStampCacheRepository(redisTemplate, meterRegistry, cacheProperties);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void getUserProgress_cacheHit() {
        UUID userId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        ProgressView progress = ProgressView.builder()
                .lineId(lineId)
                .collected(3)
                .total(10)
                .percentage(30)
                .build();
        when(valueOperations.get("user-progress:" + userId + ":" + lineId)).thenReturn(progress);

        assertTrue(repository.getUserProgress(userId, lineId).isPresent());
        assertEquals(1.0, meterRegistry.counter("cache.hit", "domain", "collection").count());
    }

    @Test
    void getUserProgress_redisFailureReturnsEmpty() {
        UUID userId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("redis down"));

        assertTrue(repository.getUserProgress(userId, lineId).isEmpty());
        assertEquals(1.0, meterRegistry.counter("cache.error", "domain", "collection").count());
    }

    @Test
    void getUserProgress_castFailureReturnsEmpty() {
        UUID userId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        when(valueOperations.get(anyString())).thenReturn("not-progress");

        assertTrue(repository.getUserProgress(userId, lineId).isEmpty());
        assertEquals(1.0, meterRegistry.counter("cache.error", "domain", "collection").count());
    }

    @Test
    void putUserProgress_redisFailureSwallowed() {
        UUID userId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        doThrow(new RuntimeException("write failed"))
                .when(valueOperations).set(anyString(), any(), any(Duration.class));

        repository.putUserProgress(userId, lineId, ProgressView.builder()
                .lineId(lineId)
                .collected(1)
                .total(5)
                .percentage(20)
                .build());

        verify(valueOperations).set(eq("user-progress:" + userId + ":" + lineId), any(), any(Duration.class));
    }

    @Test
    void getStampBook_cacheMiss() {
        UUID userId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        when(valueOperations.get(anyString())).thenReturn(null);

        assertTrue(repository.getStampBook(userId, lineId, campaignId).isEmpty());
        assertEquals(1.0, meterRegistry.counter("cache.miss", "domain", "collection").count());
    }

    @Test
    void getUserHistory_pageHit() {
        UUID userId = UUID.randomUUID();
        PageResponse<UserStampView> page = PageResponse.of(List.of(), 0, 0, 0, 20);
        when(valueOperations.get("user-history:" + userId + ":0:20")).thenReturn(page);

        assertTrue(repository.getUserHistory(userId, 0, 20).isPresent());
    }

    @Test
    void evictUserStampsForLine_scansAndDeletes() {
        UUID userId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        when(redisTemplate.execute(any(RedisCallback.class))).thenReturn(List.of(
                "user-stamps:" + userId + ":" + lineId + ":default:0:20"));

        repository.evictUserStampsForLine(userId, lineId);

        verify(redisTemplate).delete(anyList());
    }

    @Test
    void evictUserStampsForLine_scanFailureSwallowed() {
        UUID userId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        when(redisTemplate.execute(any(RedisCallback.class))).thenThrow(new RuntimeException("scan failed"));

        repository.evictUserStampsForLine(userId, lineId);

        verify(redisTemplate, never()).delete(anyList());
    }

    @Test
    void evictAllForUserCollection_clearsProgressHistoryAndStampBook() {
        UUID userId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        when(redisTemplate.execute(any(RedisCallback.class))).thenReturn(List.of());

        repository.evictAllForUserCollection(userId, lineId, campaignId);

        verify(redisTemplate).delete("user-stamp-book:" + userId + ":" + lineId + ":" + campaignId);
    }
}
