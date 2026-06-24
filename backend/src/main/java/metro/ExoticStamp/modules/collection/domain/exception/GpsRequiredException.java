package metro.ExoticStamp.modules.collection.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

public class GpsRequiredException extends DomainException {

    public GpsRequiredException(String message) {
        super(message);
    }
}
