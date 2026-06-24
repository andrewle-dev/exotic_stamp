package metro.ExoticStamp.modules.collection.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

public class GpsAccuracyTooLowException extends DomainException {

    public GpsAccuracyTooLowException(double accuracyMeters, double maxAllowed) {
        super("GPS accuracy " + accuracyMeters + "m exceeds maximum allowed " + maxAllowed + "m");
    }
}
