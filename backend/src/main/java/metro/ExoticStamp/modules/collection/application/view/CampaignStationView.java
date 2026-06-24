package metro.ExoticStamp.modules.collection.application.view;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CampaignStationView(
        UUID stationId,
        String name,
        String displayName,
        UUID lineId,
        int sortOrder
) {
}
