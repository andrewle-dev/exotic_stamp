package metro.ExoticStamp.modules.collection.application.view;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record CampaignView(
        UUID id,
        String code,
        String name,
        String displayName,
        String description,
        String campaignType,
        String status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String bannerImageUrl,
        String thumbnailImageUrl,
        int priority,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
