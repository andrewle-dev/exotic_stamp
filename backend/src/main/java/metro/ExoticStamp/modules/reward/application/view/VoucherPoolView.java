package metro.ExoticStamp.modules.reward.application.view;

import lombok.Builder;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPoolStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record VoucherPoolView(
        UUID id,
        UUID milestoneId,
        String code,
        VoucherPoolStatus status,
        UUID assignedUserId,
        UUID assignedUserRewardId,
        LocalDateTime assignedAt,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
}
