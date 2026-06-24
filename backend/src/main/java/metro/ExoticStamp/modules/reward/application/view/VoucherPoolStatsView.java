package metro.ExoticStamp.modules.reward.application.view;

import lombok.Builder;

@Builder
public record VoucherPoolStatsView(
        long availableCount,
        long redeemedCount
) {
}
