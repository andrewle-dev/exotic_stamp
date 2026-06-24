package metro.ExoticStamp.modules.metro.domain.exception;

public class DuplicateLineCodeException extends RuntimeException {

    public DuplicateLineCodeException(String code) {
        super("Line code already exists: " + code);
    }
}
