package metro.ExoticStamp.modules.collection.presentation.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CampaignStationResponse(
        UUID stationId,
        String name,
        String displayName,
        UUID lineId,
        int sortOrder
) {
}
