package metro.ExoticStamp.modules.reward.application.port;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public interface RewardReconcileCandidatePort {

    record UserCampaignPair(UUID userId, UUID campaignId) {
    }

    List<UserCampaignPair> findMissingRewardCandidates(Duration lookback, int limit);

    List<UUID> claimPendingStockRewardIds(Duration lookback, int limit);

    List<UUID> peekPendingStockRewardIds(Duration lookback, int limit);
}
