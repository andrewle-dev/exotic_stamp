package metro.ExoticStamp.modules.metro.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

import java.util.UUID;

public class StationInactiveException extends DomainException {

    public StationInactiveException(UUID stationId) {
        super("Station is inactive: " + stationId);
    }
}




