package metro.ExoticStamp.modules.collection.application.view;

import lombok.Builder;

import java.util.UUID;

@Builder
public record StampPreviewView(
        UUID id,
        String name,
        String imageUrl,
        String previewImageUrl,
        String rarity
) {
}
