package metro.ExoticStamp.modules.collection.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

public class GpsInvalidException extends DomainException {

    public GpsInvalidException(String message) {
        super(message);
    }
}
