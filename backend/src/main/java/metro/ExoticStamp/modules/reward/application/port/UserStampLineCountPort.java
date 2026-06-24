package metro.ExoticStamp.modules.reward.application.port;

import java.util.UUID;

public interface UserStampLineCountPort {

    long countDistinctStationsOnLineForUserAndCampaign(UUID userId, UUID lineId, UUID campaignId);
}
