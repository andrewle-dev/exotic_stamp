package metro.ExoticStamp.modules.auth.domain.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid email/username or password");
    }
}

