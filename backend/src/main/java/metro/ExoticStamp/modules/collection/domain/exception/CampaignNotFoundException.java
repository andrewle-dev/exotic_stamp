package metro.ExoticStamp.modules.collection.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

import java.util.UUID;

public class CampaignNotFoundException extends DomainException {
    public CampaignNotFoundException(UUID campaignId) {
        super(campaignId == null ? "Default campaign not found" : "Campaign not found: " + campaignId);
    }
}

