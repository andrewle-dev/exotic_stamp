package metro.ExoticStamp.modules.auth.presentation.support;

import jakarta.servlet.http.HttpServletRequest;
import metro.ExoticStamp.modules.auth.domain.exception.ConflictingRefreshCredentialsException;
import metro.ExoticStamp.modules.auth.domain.model.AuthTransport;

import java.util.Objects;
import java.util.Optional;

/**
 * Resolves refresh credential transport without trusting client type for authorization.
 */
public final class AuthTransportResolver {

    public static final String TRANSPORT_HEADER = "X-Client-Transport";

    private AuthTransportResolver() {
    }

    public static AuthTransport transportFromRequest(HttpServletRequest request) {
        return AuthTransport.fromHeader(request.getHeader(TRANSPORT_HEADER));
    }

    /**
     * Cookie vs body selection:
     * - both present and unequal → conflict
     * - body present → BODY transport credential
     * - else cookie → COOKIE
     */
    public static ResolvedRefresh resolveRefreshCredential(
            Optional<String> cookieToken,
            Optional<String> bodyToken
    ) {
        boolean hasCookie = cookieToken.filter(s -> !s.isBlank()).isPresent();
        boolean hasBody = bodyToken.filter(s -> !s.isBlank()).isPresent();

        if (hasCookie && hasBody) {
            if (!Objects.equals(cookieToken.get(), bodyToken.get())) {
                throw new ConflictingRefreshCredentialsException();
            }
            return new ResolvedRefresh(bodyToken.get(), AuthTransport.BODY);
        }
        if (hasBody) {
            return new ResolvedRefresh(bodyToken.get(), AuthTransport.BODY);
        }
        if (hasCookie) {
            return new ResolvedRefresh(cookieToken.get(), AuthTransport.COOKIE);
        }
        return new ResolvedRefresh(null, AuthTransport.COOKIE);
    }

    public record ResolvedRefresh(String refreshToken, AuthTransport transport) {}
}
