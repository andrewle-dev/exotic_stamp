package metro.ExoticStamp.modules.reward.application.view;

import lombok.Builder;
import metro.ExoticStamp.modules.reward.domain.model.RewardStatus;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserRewardView(
        UUID id,
        UUID userId,
        UUID campaignId,
        UUID milestoneId,
        String milestoneCode,
        String milestoneName,
        RewardType rewardType,
        String rewardTitle,
        String rewardDescription,
        String rewardImageUrl,
        LocalDateTime issuedAt,
        LocalDateTime expiresAt,
        LocalDateTime redeemedAt,
        RewardStatus status,
        UserRewardVoucherView voucher
) {
}
