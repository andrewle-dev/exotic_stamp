package metro.ExoticStamp.infra.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Resolves the client IP from {@link HttpServletRequest#getRemoteAddr()} only.
 * <p>
 * Spoofed {@code X-Forwarded-For} / {@code Forwarded} headers are intentionally ignored here.
 * In production, Spring's {@code server.forward-headers-strategy=framework} (or an equivalent
 * trusted-proxy setup) rewrites {@code remoteAddr} from the last trusted hop; this resolver
 * then reads that trusted value. Parsing XFF blindly would allow clients to forge identities
 * for rate-limit keys.
 */
@Component
public class ClientIpResolver {

    public static final String UNKNOWN = "unknown";

    public String resolve(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        return normalize(request.getRemoteAddr());
    }

    /**
     * Normalizes a remote address for use in composite rate-limit keys.
     * <ul>
     *   <li>Blank → {@code unknown}</li>
     *   <li>IPv6 literal brackets stripped ({@code [2001:db8::1]} → {@code 2001:db8::1})</li>
     *   <li>IPv4 with trailing port stripped carefully ({@code 1.2.3.4:5678} → {@code 1.2.3.4});
     *       IPv6 colons are preserved</li>
     * </ul>
     */
    public String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return UNKNOWN;
        }
        String value = raw.trim();

        if (value.startsWith("[") && value.contains("]")) {
            int end = value.indexOf(']');
            String inside = value.substring(1, end);
            String rest = value.substring(end + 1);
            // Drop optional :port after bracketed IPv6
            value = inside;
            if (rest.startsWith(":") && rest.length() > 1 && rest.substring(1).chars().allMatch(Character::isDigit)) {
                // port discarded; address already extracted
            }
        } else if (looksLikeIpv4WithPort(value)) {
            value = value.substring(0, value.lastIndexOf(':'));
        }

        if (value.isBlank() || !isLikelyIp(value)) {
            // Keep non-blank remoteAddr strings that might still be hostnames in some containers,
            // but reject obvious garbage / blank after strip.
            if (value.isBlank()) {
                return UNKNOWN;
            }
        }
        return value;
    }

    /**
     * Lightweight check used by tests and callers — not a full RFC validation.
     */
    public boolean isLikelyIp(String value) {
        if (value == null || value.isBlank() || UNKNOWN.equalsIgnoreCase(value)) {
            return false;
        }
        String v = value.trim();
        if (v.startsWith("[") && v.endsWith("]")) {
            v = v.substring(1, v.length() - 1);
        }
        if (v.contains(":")) {
            // IPv6: at least one colon, hex/colon chars only (allow compressed ::)
            return v.chars().allMatch(c ->
                    (c >= '0' && c <= '9')
                            || (c >= 'a' && c <= 'f')
                            || (c >= 'A' && c <= 'F')
                            || c == ':');
        }
        // IPv4 dotted quad (loose)
        String[] parts = v.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            try {
                int n = Integer.parseInt(part);
                if (n < 0 || n > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private static boolean looksLikeIpv4WithPort(String value) {
        int lastColon = value.lastIndexOf(':');
        if (lastColon <= 0 || lastColon == value.length() - 1) {
            return false;
        }
        // IPv6 has multiple colons; only treat single-colon host:port as IPv4+port
        if (value.indexOf(':') != lastColon) {
            return false;
        }
        String host = value.substring(0, lastColon);
        String port = value.substring(lastColon + 1);
        if (!port.chars().allMatch(Character::isDigit)) {
            return false;
        }
        String[] parts = host.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            try {
                int n = Integer.parseInt(part);
                if (n < 0 || n > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }
}
