package metro.ExoticStamp.modules.reward.application.port;

import java.util.UUID;

public interface UserStampCampaignCountPort {

    long countDistinctStationsByUserIdAndCampaignId(UUID userId, UUID campaignId);
}
