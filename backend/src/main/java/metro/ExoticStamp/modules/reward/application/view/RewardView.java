package metro.ExoticStamp.modules.reward.application.view;

import lombok.Builder;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record RewardView(
        UUID id,
        UUID milestoneId,
        UUID partnerId,
        RewardType rewardType,
        String name,
        String description,
        BigDecimal valueAmount,
        Integer expiryDays,
        Integer totalStock,
        int issuedCount,
        boolean active
) {
}
