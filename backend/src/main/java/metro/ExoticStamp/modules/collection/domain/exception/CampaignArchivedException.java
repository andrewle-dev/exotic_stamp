package metro.ExoticStamp.modules.collection.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

import java.util.UUID;

public class CampaignArchivedException extends DomainException {

    public CampaignArchivedException(UUID campaignId) {
        super("Campaign is archived and cannot be modified: " + campaignId);
    }
}
