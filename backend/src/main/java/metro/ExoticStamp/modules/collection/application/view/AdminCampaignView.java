package metro.ExoticStamp.modules.collection.application.view;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record AdminCampaignView(
        UUID id,
        UUID lineId,
        UUID partnerId,
        String code,
        String name,
        String description,
        String bannerUrl,
        LocalDateTime startDate,
        LocalDateTime endDate,
        boolean active,
        boolean defaultCampaign
) {
}
