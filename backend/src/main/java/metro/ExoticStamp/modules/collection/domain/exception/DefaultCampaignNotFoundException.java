package metro.ExoticStamp.modules.collection.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

import java.util.UUID;

public class DefaultCampaignNotFoundException extends DomainException {

    public DefaultCampaignNotFoundException(UUID lineId) {
        super(lineId != null
                ? "No active default campaign found for line: " + lineId
                : "No active default campaign found");
    }
}
