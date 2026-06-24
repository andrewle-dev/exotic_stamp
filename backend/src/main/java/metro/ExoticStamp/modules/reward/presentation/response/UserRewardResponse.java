package metro.ExoticStamp.modules.reward.presentation.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserRewardResponse(
        UUID id,
        UUID campaignId,
        UUID milestoneId,
        String milestoneCode,
        String milestoneName,
        String rewardType,
        String rewardTitle,
        String rewardDescription,
        String rewardImageUrl,
        LocalDateTime issuedAt,
        LocalDateTime expiresAt,
        LocalDateTime redeemedAt,
        String status,
        UserRewardVoucherResponse voucher
) {
}
