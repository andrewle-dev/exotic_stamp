package metro.ExoticStamp.modules.collection.presentation.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record StampDesignResponse(
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
