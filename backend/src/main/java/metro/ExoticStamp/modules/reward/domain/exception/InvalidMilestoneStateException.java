package metro.ExoticStamp.modules.reward.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

public class InvalidMilestoneStateException extends DomainException {

    public InvalidMilestoneStateException(String message) {
        super(message);
    }
}
