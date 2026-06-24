package metro.ExoticStamp.modules.reward.application.command;

import java.util.UUID;

public record UpdateMilestoneCommand(
        UUID id,
        String code,
        Integer requiredStampCount,
        String name,
        String description,
        String rewardType,
        String rewardTitle,
        String rewardDescription,
        String rewardImageUrl,
        String status,
        Integer sortOrder
) {
}
