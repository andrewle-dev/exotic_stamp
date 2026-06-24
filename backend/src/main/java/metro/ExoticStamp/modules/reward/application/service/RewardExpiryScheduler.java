package metro.ExoticStamp.modules.reward.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.reward.domain.repository.UserRewardRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RewardExpiryScheduler {

    private final UserRewardRepository userRewardRepository;
    private final java.time.Clock clock;

    @Scheduled(cron = "${reward.expiry-cron}")
    @Transactional
    public void expireRewards() {
        LocalDateTime now = LocalDateTime.now(clock);
        int updated = userRewardRepository.expireIssuedBefore(now);
        log.info("[Reward] expiry batch updated {} user_rewards to EXPIRED (before {})", updated, now);
    }
}
