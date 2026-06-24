package metro.ExoticStamp.modules.reward.presentation.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record VoucherPoolResponse(
        UUID id,
        UUID milestoneId,
        String code,
        String status,
        UUID assignedUserId,
        UUID assignedUserRewardId,
        LocalDateTime assignedAt,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
}
