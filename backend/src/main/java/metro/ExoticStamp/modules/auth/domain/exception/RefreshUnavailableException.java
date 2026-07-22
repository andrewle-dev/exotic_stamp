package metro.ExoticStamp.modules.auth.domain.exception;

/** Refresh cannot be evaluated safely (e.g. Redis unavailable for grace cache). */
public class RefreshUnavailableException extends RuntimeException {
    public RefreshUnavailableException() {
        super("Refresh temporarily unavailable");
    }
}
