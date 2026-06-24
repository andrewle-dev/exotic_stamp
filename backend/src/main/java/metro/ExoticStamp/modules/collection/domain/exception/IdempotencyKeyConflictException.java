package metro.ExoticStamp.modules.collection.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

/**
 * Same idempotency key was used within the window by another user.
 */
public class IdempotencyKeyConflictException extends DomainException {

    public IdempotencyKeyConflictException() {
        super("Idempotency key already used by another user within the allowed time window");
    }
}
