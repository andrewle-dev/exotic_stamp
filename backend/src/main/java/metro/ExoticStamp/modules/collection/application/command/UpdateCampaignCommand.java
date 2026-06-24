package metro.ExoticStamp.modules.collection.application.command;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateCampaignCommand(
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
        Integer priority
) {
}
