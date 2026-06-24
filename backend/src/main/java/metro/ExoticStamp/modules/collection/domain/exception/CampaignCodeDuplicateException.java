package metro.ExoticStamp.modules.collection.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

public class CampaignCodeDuplicateException extends DomainException {

    public CampaignCodeDuplicateException(String code) {
        super("Campaign code already exists: " + code);
    }
}
