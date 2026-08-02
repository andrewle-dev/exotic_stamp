package metro.ExoticStamp.common.exceptions.security;

/**
 * Fail-closed signal when a security-critical dependency (e.g. Redis denylist/OTP) is unavailable.
 * Message must not include hostnames, connection strings, or other infrastructure details.
 */
public class SecurityDependencyUnavailableException extends RuntimeException {

    public static final String SAFE_MESSAGE = "Security dependency temporarily unavailable";

    public SecurityDependencyUnavailableException() {
        super(SAFE_MESSAGE);
    }

    public SecurityDependencyUnavailableException(Throwable cause) {
        super(SAFE_MESSAGE, cause);
    }

    public SecurityDependencyUnavailableException(String message) {
        super(message != null && !message.isBlank() ? message : SAFE_MESSAGE);
    }

    public SecurityDependencyUnavailableException(String message, Throwable cause) {
        super(message != null && !message.isBlank() ? message : SAFE_MESSAGE, cause);
    }
}
