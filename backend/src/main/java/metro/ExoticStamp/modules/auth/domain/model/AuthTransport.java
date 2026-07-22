package metro.ExoticStamp.modules.auth.domain.model;

/**
 * Selects credential transport / response shaping only.
 * Must never grant authorization or bypass token validation.
 */
public enum AuthTransport {
    /** Web: refresh via HttpOnly cookie; refresh omitted from JSON. */
    COOKIE,
    /** Native: refresh via request/response body; no cookie dependency. */
    BODY;

    public static AuthTransport fromHeader(String raw) {
        if (raw == null || raw.isBlank()) {
            return COOKIE;
        }
        String normalized = raw.trim().toLowerCase();
        return switch (normalized) {
            case "body", "native", "mobile" -> BODY;
            case "cookie", "web" -> COOKIE;
            default -> COOKIE;
        };
    }
}
