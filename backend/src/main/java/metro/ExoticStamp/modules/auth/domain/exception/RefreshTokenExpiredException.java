package metro.ExoticStamp.modules.auth.domain.exception;

/** Refresh credential expired. */
public class RefreshTokenExpiredException extends RuntimeException {
    public RefreshTokenExpiredException() {
        super("Refresh token expired");
    }
}
