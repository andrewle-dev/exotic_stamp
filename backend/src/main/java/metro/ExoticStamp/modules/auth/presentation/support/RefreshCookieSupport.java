package metro.ExoticStamp.modules.auth.presentation.support;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.auth.application.port.TokenTtlPort;
import metro.ExoticStamp.modules.auth.config.AuthCookieProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshCookieSupport {

    private final AuthCookieProperties cookieProperties;
    private final TokenTtlPort tokenTtlPort;

    /**
     * Sets the current-path cookie and expires the legacy Path=/api/v1/auth/refresh cookie.
     */
    public void setRefreshCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(refreshToken, request, false, cookieProperties.getPath()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("", request, true, AuthCookieProperties.LEGACY_PATH).toString());
    }

    /** Clears both current and legacy cookie paths with matching Domain/SameSite/Secure. */
    public void clearRefreshCookie(HttpServletRequest request, HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("", request, true, cookieProperties.getPath()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("", request, true, AuthCookieProperties.LEGACY_PATH).toString());
    }

    public Optional<String> readRefreshCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        String name = cookieProperties.getName();
        for (Cookie cookie : cookies) {
            if (Objects.equals(cookie.getName(), name)) {
                String value = cookie.getValue();
                if (value != null && !value.isBlank()) {
                    return Optional.of(value);
                }
            }
        }
        return Optional.empty();
    }

    public boolean hasRefreshCookie(HttpServletRequest request) {
        return readRefreshCookie(request).isPresent();
    }

    private ResponseCookie buildCookie(String value, HttpServletRequest request, boolean clear, String path) {
        boolean secure = resolveSecure(request);
        Duration maxAge = clear ? Duration.ZERO : tokenTtlPort.getRefreshTokenTtl();
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieProperties.getName(), value)
                .httpOnly(Boolean.TRUE.equals(cookieProperties.getHttpOnly()))
                .secure(secure)
                .path(path)
                .maxAge(maxAge)
                .sameSite(cookieProperties.getSameSite());

        String domain = cookieProperties.getDomain();
        if (domain != null && !domain.isBlank()) {
            builder.domain(domain);
        }
        return builder.build();
    }

    private boolean resolveSecure(HttpServletRequest request) {
        if (cookieProperties.isSecureAlways() || cookieProperties.isSecure()) {
            return true;
        }
        return request.isSecure();
    }
}
