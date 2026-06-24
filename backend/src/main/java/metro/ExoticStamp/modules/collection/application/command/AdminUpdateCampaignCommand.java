package metro.ExoticStamp.modules.collection.application.command;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUpdateCampaignCommand(
        UUID id,
        UUID partnerId,
        String code,
        String name,
        String description,
        String bannerUrl,
        LocalDateTime startDate,
        LocalDateTime endDate,
        boolean active,
        boolean defaultCampaign
) {
}
