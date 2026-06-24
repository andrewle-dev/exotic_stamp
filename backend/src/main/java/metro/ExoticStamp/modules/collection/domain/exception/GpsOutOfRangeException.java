package metro.ExoticStamp.modules.collection.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

public class GpsOutOfRangeException extends DomainException {

    public GpsOutOfRangeException(double distanceMeters, double allowedRadiusMeters) {
        super("GPS distance " + String.format("%.1f", distanceMeters)
                + "m exceeds allowed radius " + String.format("%.1f", allowedRadiusMeters) + "m");
    }
}
