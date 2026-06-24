package metro.ExoticStamp.modules.collection.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

public class DuplicateActiveStampDesignException extends DomainException {

    public DuplicateActiveStampDesignException() {
        super("An active stamp design already exists for this campaign and station");
    }
}
