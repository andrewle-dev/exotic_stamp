package metro.ExoticStamp.modules.collection.domain.policy;

import metro.ExoticStamp.modules.collection.domain.exception.CampaignNotActiveException;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignStationNotEligibleException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Campaign / station eligibility for collection.
 * Application loads campaign and membership flags; this policy decides.
 */
public final class CollectionEligibilityPolicy {

    private CollectionEligibilityPolicy() {
    }

    public static void assertCampaignStationEligible(boolean campaignStationExists, UUID campaignId, UUID stationId) {
        if (!campaignStationExists) {
            throw new CampaignStationNotEligibleException(campaignId, stationId);
        }
    }

    public static void assertCampaignCollectable(Campaign campaign, LocalDateTime now) {
        if (campaign == null || !campaign.isActiveForCollection() || !isInWindow(campaign, now)) {
            UUID id = campaign != null ? campaign.getId() : null;
            throw new CampaignNotActiveException(id);
        }
    }

    public static boolean isInWindow(Campaign campaign, LocalDateTime now) {
        if (campaign == null || now == null || campaign.getStartAt() == null || campaign.getEndAt() == null) {
            return false;
        }
        return !now.isBefore(campaign.getStartAt()) && !now.isAfter(campaign.getEndAt());
    }
}
