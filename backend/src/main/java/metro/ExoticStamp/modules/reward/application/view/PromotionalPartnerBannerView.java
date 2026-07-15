package metro.ExoticStamp.modules.reward.application.view;

import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record PromotionalPartnerBannerView(
        UUID partnerId,
        String partnerName,
        String logoUrl,
        String bannerImageUrl,
        LocalDate contractStart,
        LocalDate contractEnd
) {
}
