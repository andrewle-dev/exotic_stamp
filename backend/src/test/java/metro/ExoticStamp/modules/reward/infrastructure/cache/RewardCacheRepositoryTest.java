package metro.ExoticStamp.modules.reward.infrastructure.cache;



import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import metro.ExoticStamp.common.response.PageResponse;

import metro.ExoticStamp.modules.reward.application.view.UserRewardView;

import metro.ExoticStamp.modules.reward.config.RewardProperties;

import metro.ExoticStamp.modules.reward.domain.model.RewardStatus;

import metro.ExoticStamp.modules.reward.domain.model.RewardType;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.redis.core.RedisCallback;

import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.data.redis.core.ValueOperations;



import java.time.Duration;

import java.time.LocalDateTime;

import java.util.List;

import java.util.UUID;



import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.anyList;

import static org.mockito.ArgumentMatchers.anyString;

import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.doThrow;

import static org.mockito.Mockito.never;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import static org.mockito.Mockito.when;



@ExtendWith(MockitoExtension.class)

class RewardCacheRepositoryTest {



    @Mock private RedisTemplate<String, Object> redisTemplate;

    @Mock private ValueOperations<String, Object> valueOperations;



    private RewardCacheRepository repository;

    private SimpleMeterRegistry meterRegistry;



    @BeforeEach

    void setUp() {

        meterRegistry = new SimpleMeterRegistry();

        RewardProperties properties = new RewardProperties();

        properties.setUserRewardCacheTtl(Duration.ofMinutes(30));

        repository = new RewardCacheRepository(redisTemplate, meterRegistry, properties);

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    }



    @Test

    void getUserRewardDetail_cacheHit() {

        UUID userId = UUID.randomUUID();

        UUID rewardId = UUID.randomUUID();

        UserRewardView view = sampleView(userId, rewardId);

        when(valueOperations.get("user_reward:detail:" + userId + ":" + rewardId)).thenReturn(view);



        var result = repository.getUserRewardDetail(userId, rewardId);



        assertTrue(result.isPresent());

        assertEquals(rewardId, result.get().id());

        assertEquals(1.0, meterRegistry.counter("cache.hit", "domain", "user_reward").count());

    }



    @Test

    void getUserRewardDetail_cacheMiss() {

        UUID userId = UUID.randomUUID();

        UUID rewardId = UUID.randomUUID();

        when(valueOperations.get(anyString())).thenReturn(null);



        var result = repository.getUserRewardDetail(userId, rewardId);



        assertTrue(result.isEmpty());

        assertEquals(1.0, meterRegistry.counter("cache.miss", "domain", "user_reward").count());

    }



    @Test

    void getUserRewardDetail_redisFailureReturnsEmpty() {

        UUID userId = UUID.randomUUID();

        UUID rewardId = UUID.randomUUID();

        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("redis down"));



        var result = repository.getUserRewardDetail(userId, rewardId);



        assertTrue(result.isEmpty());

        assertEquals(1.0, meterRegistry.counter("cache.error", "domain", "user_reward").count());

    }



    @Test

    void getUserRewardDetail_castFailureReturnsEmpty() {

        UUID userId = UUID.randomUUID();

        UUID rewardId = UUID.randomUUID();

        when(valueOperations.get(anyString())).thenReturn("not-a-view");



        var result = repository.getUserRewardDetail(userId, rewardId);



        assertTrue(result.isEmpty());

        assertEquals(1.0, meterRegistry.counter("cache.error", "domain", "user_reward").count());

    }



    @Test

    void putUserRewardDetail_redisFailureIsSwallowed() {

        UUID userId = UUID.randomUUID();

        UUID rewardId = UUID.randomUUID();

        doThrow(new RuntimeException("write failed"))

                .when(valueOperations).set(anyString(), any(), any(Duration.class));



        repository.putUserRewardDetail(userId, rewardId, sampleView(userId, rewardId));



        verify(valueOperations).set(

                eq("user_reward:detail:" + userId + ":" + rewardId),

                any(UserRewardView.class),

                eq(Duration.ofMinutes(30)));

    }



    @Test

    void getUserRewardList_cacheHit() {

        UUID userId = UUID.randomUUID();

        PageResponse<UserRewardView> page = PageResponse.of(List.of(), 0, 0, 0, 20);

        when(valueOperations.get("user_reward:list:" + userId + ":0:20")).thenReturn(page);



        var result = repository.getUserRewardList(userId, 0, 20);



        assertTrue(result.isPresent());

        assertEquals(1.0, meterRegistry.counter("cache.hit", "domain", "user_reward").count());

    }



    @Test

    void evictUserRewardDetail_deletesKey() {

        UUID userId = UUID.randomUUID();

        UUID rewardId = UUID.randomUUID();



        repository.evictUserRewardDetail(userId, rewardId);



        verify(redisTemplate).delete("user_reward:detail:" + userId + ":" + rewardId);

    }



    @Test

    void evictUserRewardDetail_redisFailureIsSwallowed() {

        UUID userId = UUID.randomUUID();

        UUID rewardId = UUID.randomUUID();

        doThrow(new RuntimeException("delete failed")).when(redisTemplate).delete(anyString());



        repository.evictUserRewardDetail(userId, rewardId);



        verify(redisTemplate).delete(anyString());

    }



    @Test

    void evictUserRewardListAll_scansAndDeletesMatchingKeys() {

        UUID userId = UUID.randomUUID();

        when(redisTemplate.execute(any(RedisCallback.class))).thenReturn(List.of(

                "user_reward:list:" + userId + ":0:20",

                "user_reward:list:" + userId + ":1:20"

        ));



        repository.evictUserRewardListAll(userId);



        verify(redisTemplate).delete(anyList());

    }



    @Test

    void evictUserRewardListAll_scanFailureIsSwallowed() {

        UUID userId = UUID.randomUUID();

        when(redisTemplate.execute(any(RedisCallback.class))).thenThrow(new RuntimeException("scan failed"));



        repository.evictUserRewardListAll(userId);



        verify(redisTemplate, never()).delete(anyList());

    }



    private static UserRewardView sampleView(UUID userId, UUID rewardId) {

        return UserRewardView.builder()

                .id(rewardId)

                .userId(userId)

                .campaignId(UUID.randomUUID())

                .milestoneId(UUID.randomUUID())

                .rewardType(RewardType.VOUCHER)

                .issuedAt(LocalDateTime.of(2026, 4, 12, 10, 0))

                .status(RewardStatus.ISSUED)

                .build();

    }

}

