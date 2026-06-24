package metro.ExoticStamp.modules.reward.application.command;

import java.util.UUID;

public record CreateMilestoneCommand(
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
        int sortOrder
) {
}
