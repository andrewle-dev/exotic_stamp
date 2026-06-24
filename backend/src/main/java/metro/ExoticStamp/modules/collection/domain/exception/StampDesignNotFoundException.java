package metro.ExoticStamp.modules.collection.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

import java.util.UUID;

public class StampDesignNotFoundException extends DomainException {

    public StampDesignNotFoundException(UUID campaignId, UUID stationId) {
        super("No active stamp design for campaign " + campaignId + " and station " + stationId);
    }
}
