package metro.ExoticStamp.modules.collection.presentation.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record StampPreviewResponse(
        UUID id,
        String name,
        String imageUrl,
        String previewImageUrl,
        String rarity
) {
}
