package metro.ExoticStamp.modules.collection.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

import java.util.UUID;

public class StampAlreadyCollectedException extends DomainException {
    public StampAlreadyCollectedException(UUID stationId) {
        super("You have already collected a stamp at this station: " + stationId);
    }
}

