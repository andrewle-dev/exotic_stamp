package metro.ExoticStamp.modules.collection.presentation.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ActiveCampaignStationResponse(
        UUID id,
        String name,
        String displayName,
        int sortOrder,
        StampPreviewResponse stampPreview
) {
}
