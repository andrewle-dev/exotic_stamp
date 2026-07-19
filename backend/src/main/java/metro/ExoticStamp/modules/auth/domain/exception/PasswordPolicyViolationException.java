package metro.ExoticStamp.modules.auth.domain.exception;

public class PasswordPolicyViolationException extends RuntimeException {
    public PasswordPolicyViolationException(String message) {
        super(message);
    }
}
