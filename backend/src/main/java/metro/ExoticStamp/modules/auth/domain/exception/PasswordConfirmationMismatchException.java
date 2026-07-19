package metro.ExoticStamp.modules.auth.domain.exception;

public class PasswordConfirmationMismatchException extends RuntimeException {
    public PasswordConfirmationMismatchException() {
        super("New password and confirmation do not match");
    }
}
