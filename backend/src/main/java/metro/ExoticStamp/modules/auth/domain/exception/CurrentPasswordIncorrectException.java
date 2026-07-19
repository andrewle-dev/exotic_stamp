package metro.ExoticStamp.modules.auth.domain.exception;

public class CurrentPasswordIncorrectException extends RuntimeException {
    public CurrentPasswordIncorrectException() {
        super("Current password is incorrect");
    }
}
