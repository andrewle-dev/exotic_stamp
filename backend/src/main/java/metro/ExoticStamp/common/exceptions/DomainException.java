package metro.ExoticStamp.common.exceptions;

/**
 * Base type for domain-level errors.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
