package metro.ExoticStamp.modules.reward.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

public class RedeemNotSupportedException extends DomainException {

    public RedeemNotSupportedException(String message) {
        super(message);
    }
}
