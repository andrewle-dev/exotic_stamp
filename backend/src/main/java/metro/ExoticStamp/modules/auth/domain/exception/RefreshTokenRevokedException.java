package metro.ExoticStamp.modules.auth.domain.exception;

/** Refresh session revoked (logout, password change, etc.). */
public class RefreshTokenRevokedException extends RuntimeException {
    public RefreshTokenRevokedException() {
        super("Refresh token revoked");
    }
}
