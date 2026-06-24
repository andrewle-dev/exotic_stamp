package metro.ExoticStamp.modules.collection.domain.factory;

import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.CampaignStatus;
import metro.ExoticStamp.modules.collection.domain.model.CampaignType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Shared construction of default-per-line campaigns (bootstrap + line-created events).
 */
public final class DefaultCampaignFactory {

    private DefaultCampaignFactory() {}

    public static String defaultCampaignCode(UUID lineId) {
        String compact = lineId.toString().replace("-", "");
        return ("DEF-" + compact).substring(0, 30);
    }

    public static Campaign createDefaultForLine(UUID lineId, String lineName, String lineCode) {
        LocalDateTime now = LocalDateTime.now();
        String name = "Default campaign: " + lineName;
        return Campaign.builder()
                .lineId(lineId)
                .isDefault(true)
                .status(CampaignStatus.ACTIVE)
                .campaignType(CampaignType.STANDARD)
                .code(defaultCampaignCode(lineId))
                .name(name)
                .displayName(name)
                .description("Auto-created default campaign for line " + lineCode)
                .startAt(now)
                .endAt(now.plusYears(50))
                .priority(0)
                .createdAt(now)
                .build();
    }
}
