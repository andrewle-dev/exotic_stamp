package metro.ExoticStamp.modules.auth.infrastructure.security;

public enum AccessTokenRevocationStatus {
    OK,
    REVOKED,
    /** DB unavailable; request allowed per operations policy (monitor via metrics/logs). */
    FAIL_OPEN
}
