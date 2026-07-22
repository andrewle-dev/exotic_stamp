package metro.ExoticStamp.modules.auth.domain.exception;

/** Cookie and body refresh credentials conflict. */
public class ConflictingRefreshCredentialsException extends RuntimeException {
    public ConflictingRefreshCredentialsException() {
        super("Conflicting refresh credentials in cookie and body");
    }
}
