package metro.ExoticStamp.modules.collection.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

/**
 * Client GPS could not be verified against the station (missing data, distance, or configuration).
 */
public class GpsVerificationFailedException extends DomainException {

    public GpsVerificationFailedException(String message) {
        super(message);
    }
}
