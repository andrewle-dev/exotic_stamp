package metro.ExoticStamp.modules.reward.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.reward.domain.exception.RewardAlreadyIssuedException;
import metro.ExoticStamp.modules.reward.domain.repository.UserRewardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardIssuancePolicyService {

    private final UserRewardRepository userRewardRepository;

    public boolean isAlreadyIssued(UUID userId, UUID milestoneId) {
        return userRewardRepository.existsByUserIdAndMilestoneId(userId, milestoneId);
    }

    public void assertNotAlreadyIssued(UUID userId, UUID milestoneId) {
        if (isAlreadyIssued(userId, milestoneId)) {
            throw new RewardAlreadyIssuedException(
                    "Reward already issued for user " + userId + " milestone " + milestoneId);
        }
    }
}
