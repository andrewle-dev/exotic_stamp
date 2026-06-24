package metro.ExoticStamp.modules.metro.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

import java.util.UUID;

public class StationNotFoundException extends DomainException {

    public StationNotFoundException(String field, String value) {
        super("Station not found with " + field + ": " + value);
    }

    public StationNotFoundException(UUID stationId) {
        super("Station not found with id: " + stationId);
    }
}




