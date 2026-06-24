package metro.ExoticStamp.modules.collection.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

public class DefaultCampaignAmbiguousException extends DomainException {

    public DefaultCampaignAmbiguousException(int matchCount) {
        super("Multiple active default campaigns found (" + matchCount
                + "); provide lineId to disambiguate or configure a single global default");
    }
}
