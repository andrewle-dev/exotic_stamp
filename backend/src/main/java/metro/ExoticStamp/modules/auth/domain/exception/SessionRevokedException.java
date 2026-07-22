package metro.ExoticStamp.modules.auth.domain.exception;

/** Session was revoked globally (logout-all / password / reuse) during or before refresh. */
public class SessionRevokedException extends RuntimeException {
    public SessionRevokedException() {
        super("Session revoked");
    }
}
