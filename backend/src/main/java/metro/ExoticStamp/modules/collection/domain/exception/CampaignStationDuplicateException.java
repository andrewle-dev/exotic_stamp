package metro.ExoticStamp.modules.collection.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

public class CampaignStationDuplicateException extends DomainException {

    public CampaignStationDuplicateException() {
        super("Station already assigned to this campaign");
    }
}
