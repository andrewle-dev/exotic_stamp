package metro.ExoticStamp.modules.reward.presentation.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record PromotionalPartnerBannerResponse(
        UUID partnerId,
        String partnerName,
        String logoUrl,
        String bannerImageUrl,
        LocalDate contractStart,
        LocalDate contractEnd
) {
}
