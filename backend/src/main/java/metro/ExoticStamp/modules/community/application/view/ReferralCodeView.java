package metro.ExoticStamp.modules.community.application.view;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ReferralCodeView(
        UUID id,
        String code,
        String status,
        int totalReferrals,
        LocalDateTime createdAt
) {
}
