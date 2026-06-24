package metro.ExoticStamp.modules.reward.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateRewardCommand(
        UUID id,
        UUID milestoneId,
        UUID partnerId,
        String rewardType,
        String name,
        String description,
        BigDecimal valueAmount,
        Integer expiryDays,
        Integer totalStock
) {
}
