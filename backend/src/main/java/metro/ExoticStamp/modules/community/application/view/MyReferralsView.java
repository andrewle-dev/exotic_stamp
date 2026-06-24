package metro.ExoticStamp.modules.community.application.view;

import lombok.Builder;

import java.util.List;

@Builder
public record MyReferralsView(
        ReferralView referredBy,
        List<ReferralView> referredUsers,
        long pendingCount,
        long completedCount,
        long rewardedCount
) {
}
