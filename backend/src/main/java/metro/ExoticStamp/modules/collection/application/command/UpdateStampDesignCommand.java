package metro.ExoticStamp.modules.collection.application.command;

import java.util.UUID;

public record UpdateStampDesignCommand(
        UUID id,
        UUID campaignId,
        UUID stationId,
        String name,
        String description,
        String imageUrl,
        String previewImageUrl,
        String rarity,
        String status,
        Integer sortOrder
) {
}
