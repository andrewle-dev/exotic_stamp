package metro.ExoticStamp.modules.auth.domain.exception;

/** Confirmed refresh-token reuse outside grace policy. */
public class RefreshTokenReusedException extends RuntimeException {
    public RefreshTokenReusedException() {
        super("Refresh token reuse detected");
    }
}
