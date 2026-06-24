package metro.ExoticStamp.modules.reward.application.view;

import lombok.Builder;
import metro.ExoticStamp.modules.reward.domain.model.MilestoneStatus;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record MilestoneView(
        UUID id,
        UUID campaignId,
        String code,
        int requiredStampCount,
        String name,
        String description,
        RewardType rewardType,
        String rewardTitle,
        String rewardDescription,
        String rewardImageUrl,
        MilestoneStatus status,
        int sortOrder,
        LocalDateTime deletedAt
) {
}
