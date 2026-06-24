package metro.ExoticStamp.modules.reward.presentation.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record MilestoneResponse(
        UUID id,
        UUID campaignId,
        String code,
        int requiredStampCount,
        String name,
        String description,
        String rewardType,
        String rewardTitle,
        String rewardDescription,
        String rewardImageUrl,
        String status,
        int sortOrder,
        LocalDateTime deletedAt
) {
}
