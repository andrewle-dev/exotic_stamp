package metro.ExoticStamp.modules.collection.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

/** Same user + idempotency key was reused for a different logical collect operation. */
public class IdempotencyConflictException extends DomainException {

    public IdempotencyConflictException() {
        super("Idempotency key was already used for a different collect operation");
    }
}
