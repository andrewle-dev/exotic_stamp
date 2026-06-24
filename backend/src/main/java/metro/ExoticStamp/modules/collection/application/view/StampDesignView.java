package metro.ExoticStamp.modules.collection.application.view;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record StampDesignView(
        UUID id,
        UUID campaignId,
        UUID stationId,
        String name,
        String description,
        String imageUrl,
        String previewImageUrl,
        String rarity,
        String status,
        int sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
