package metro.ExoticStamp.modules.collection.application.view;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AdminStampDesignView(
        UUID id,
        UUID stationId,
        UUID campaignId,
        String name,
        String artworkUrl,
        String animationUrl,
        String soundUrl,
        boolean limited,
        boolean active
) {
}
