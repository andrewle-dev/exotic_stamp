package metro.ExoticStamp.modules.auth.domain.exception;

public class SecurityBreachException extends RuntimeException {
    public SecurityBreachException(String uid) {
        super("Token reuse detected for userId: " + uid + ". All sessions invalidated.");
    }
}

