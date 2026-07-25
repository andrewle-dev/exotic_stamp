package metro.ExoticStamp.modules.reward.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.reward.domain.exception.RewardAlreadyIssuedException;
import metro.ExoticStamp.modules.reward.domain.repository.UserRewardRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Read-only policy checks. Intentionally has <strong>no</strong> {@code @Transactional}:
 * callers such as {@link RewardEvaluationService} already run a write transaction.
 * A class-level {@code @Transactional(readOnly = true)} would join that TX and could
 * mark the shared connection read-only, breaking subsequent inserts (Batch B.1 / F-009).
 */
@Service
@RequiredArgsConstructor
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
