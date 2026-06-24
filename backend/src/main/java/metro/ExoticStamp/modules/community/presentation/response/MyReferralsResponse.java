package metro.ExoticStamp.modules.community.presentation.response;

import lombok.Builder;

import java.util.List;

@Builder
public record MyReferralsResponse(
        ReferralResponse referredBy,
        List<ReferralResponse> referredUsers,
        long pendingCount,
        long completedCount,
        long rewardedCount
) {
}
