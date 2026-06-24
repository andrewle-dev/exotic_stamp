package metro.ExoticStamp.modules.collection.application.command;

import java.time.LocalDateTime;

public record CreateCampaignCommand(
        String code,
        String name,
        String displayName,
        String description,
        String campaignType,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String bannerImageUrl,
        String thumbnailImageUrl,
        Integer priority
) {
}
