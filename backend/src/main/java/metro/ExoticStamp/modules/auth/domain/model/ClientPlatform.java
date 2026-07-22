package metro.ExoticStamp.modules.auth.domain.model;

/**
 * Client platform metadata for refresh sessions.
 * Not an authorization factor.
 */
public enum ClientPlatform {
    WEB,
    MOBILE,
    UNKNOWN;

    public static ClientPlatform fromTransport(AuthTransport transport) {
        if (transport == null) {
            return UNKNOWN;
        }
        return switch (transport) {
            case COOKIE -> WEB;
            case BODY -> MOBILE;
        };
    }
}
