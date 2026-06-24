package metro.ExoticStamp.modules.community.presentation.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ReferralCodeResponse(
        UUID id,
        String code,
        String status,
        int totalReferrals,
        LocalDateTime createdAt
) {
}
