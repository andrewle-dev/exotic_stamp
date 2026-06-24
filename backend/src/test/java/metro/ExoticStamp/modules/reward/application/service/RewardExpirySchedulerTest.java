package metro.ExoticStamp.modules.reward.application.service;

import metro.ExoticStamp.modules.reward.domain.repository.UserRewardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardExpirySchedulerTest {

    @Mock
    private UserRewardRepository userRewardRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-04-12T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void expireRewards_delegatesToRepository() {
        when(userRewardRepository.expireIssuedBefore(any())).thenReturn(3);
        RewardExpiryScheduler s = new RewardExpiryScheduler(userRewardRepository, clock);
        s.expireRewards();
        verify(userRewardRepository).expireIssuedBefore(any());
    }
}
