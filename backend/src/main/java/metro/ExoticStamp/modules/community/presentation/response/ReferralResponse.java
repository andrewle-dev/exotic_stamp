package metro.ExoticStamp.modules.community.presentation.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ReferralResponse(
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
