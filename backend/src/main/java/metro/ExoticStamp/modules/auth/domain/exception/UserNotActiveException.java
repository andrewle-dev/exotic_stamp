package metro.ExoticStamp.modules.auth.domain.exception;

public class UserNotActiveException extends RuntimeException {
    public UserNotActiveException() {
        super("Account not active. Please verify your email.");
    }
}

