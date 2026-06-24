package metro.ExoticStamp.modules.reward.presentation.response;

import lombok.Builder;

@Builder
public record VoucherPoolStatsResponse(
        long availableCount,
        long redeemedCount
) {
}
