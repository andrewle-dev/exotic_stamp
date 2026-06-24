package metro.ExoticStamp.modules.collection.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

import java.util.UUID;

/**
 * Campaign is inactive or outside its configured date window.
 */
public class CampaignNotActiveException extends DomainException {

    public CampaignNotActiveException(UUID campaignId) {
        super(campaignId == null ? "Campaign is not active" : "Campaign is not active: " + campaignId);
    }
}
