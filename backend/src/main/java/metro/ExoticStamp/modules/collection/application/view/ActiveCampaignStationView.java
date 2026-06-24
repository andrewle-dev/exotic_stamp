package metro.ExoticStamp.modules.collection.application.view;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ActiveCampaignStationView(
        UUID id,
        String name,
        String displayName,
        int sortOrder,
        StampPreviewView stampPreview
) {
}
