package metro.ExoticStamp.modules.reward.presentation.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record RewardResponse(
        UUID id,
        UUID milestoneId,
        UUID partnerId,
        String rewardType,
        String name,
        String description,
        BigDecimal valueAmount,
        Integer expiryDays,
        Integer totalStock,
        int issuedCount,
        boolean active
) {
}
