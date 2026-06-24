package metro.ExoticStamp.modules.collection.presentation.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record ActiveCampaignResponse(
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
        List<ActiveCampaignStationResponse> stations
) {
}
