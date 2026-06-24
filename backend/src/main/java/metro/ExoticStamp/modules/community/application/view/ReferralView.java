package metro.ExoticStamp.modules.community.application.view;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ReferralView(
        UUID id,
        UUID referrerUserId,
        UUID referredUserId,
        UUID referralCodeId,
        String status,
        LocalDateTime appliedAt,
        LocalDateTime completedAt,
        LocalDateTime rewardedAt
) {
}
