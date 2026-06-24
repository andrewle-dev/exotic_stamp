package metro.ExoticStamp.common.exceptions;

/**
 * Thrown when a domain business rule is violated.
 * Maps to HTTP 422 Unprocessable Entity — request is valid syntax
 * but violates business logic (e.g. cannot reactivate banned user).
 * Distinct from IllegalArgumentException (400 bad input format).
 */
public class DomainRuleViolationException extends DomainException {

    public DomainRuleViolationException(String message) {
        super(message);
    }

}
