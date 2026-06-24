package metro.ExoticStamp.modules.collection.application.view;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record ActiveCampaignView(
        UUID id,
        String code,
        String name,
        String displayName,
        String description,
        String campaignType,
        String bannerImageUrl,
        String thumbnailImageUrl,
        int priority,
        LocalDateTime startAt,
        LocalDateTime endAt,
        List<ActiveCampaignStationView> stations
) {
}
