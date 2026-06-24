package metro.ExoticStamp.modules.collection.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

import java.util.UUID;

public class InvalidStationException extends DomainException {
    public InvalidStationException(UUID stationId, String reason) {
        super("Invalid station " + stationId + ": " + reason);
    }
}

