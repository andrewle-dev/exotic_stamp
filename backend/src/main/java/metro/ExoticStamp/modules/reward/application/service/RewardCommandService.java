package metro.ExoticStamp.modules.reward.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.reward.domain.exception.RedeemNotSupportedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RewardCommandService {

    private final RewardEvaluationService rewardEvaluationService;

    /**
     * Runs after stamp collection: evaluate milestones and issue rewards (idempotent per milestone).
     */
    @Transactional
    public void handleStampCollected(UUID userId, UUID lineId, UUID campaignId) {
        rewardEvaluationService.handleStampCollected(userId, campaignId);
    }

    /**
     * @deprecated Use {@link RewardEvaluationService}; kept for legacy tests and callers.
     */
    @Deprecated
    @Transactional
    public void issueReward(UUID userId, UUID milestoneId, UUID lineId) {
        log.debug("[Reward] issueReward deprecated direct call milestoneId={}", milestoneId);
    }

    /**
     * Redeem is outside Mobile MVP scope. Path variable {@code userRewardId} is documented but mutation is disabled.
     */
    @Transactional
    public void redeemVoucher(UUID userId, UUID userRewardId) {
        throw new RedeemNotSupportedException(
                "Voucher redeem is not supported in MVP. userRewardId=" + userRewardId + " userId=" + userId);
    }
}
