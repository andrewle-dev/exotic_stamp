package metro.ExoticStamp.modules.auth.infrastructure.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.config.CorsProperties;
import metro.ExoticStamp.modules.auth.config.AuthCookieProperties;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * CSRF mitigation for cookie-authenticated auth POSTs.
 * Uses resolved credential source (cookie vs body), not {@code X-Client-Transport} alone.
 * Bean is created by {@code SecurityConfig} (not component-scanned) for WebMvcTest isolation.
 */
@RequiredArgsConstructor
public class CookieAuthOriginFilter extends OncePerRequestFilter {

    private static final Set<String> PROTECTED_PATHS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
            "/api/v1/auth/logout-all",
            "/api/v1/auth/change-password"
    );

    private final CorsProperties corsProperties;
    private final AuthCookieProperties cookieProperties;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return path == null || PROTECTED_PATHS.stream().noneMatch(path::equals);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        CachedBodyRequest cached = new CachedBodyRequest(request);
        boolean cookieCredential = hasRefreshCookie(cached);
        boolean bodyCredential = hasBodyRefreshToken(cached);

        // Native body-only refresh/logout: no cookie CSRF surface.
        if (bodyCredential && !cookieCredential) {
            filterChain.doFilter(cached, response);
            return;
        }

        String origin = blankToNull(cached.getHeader("Origin"));
        String referer = blankToNull(cached.getHeader("Referer"));
        String candidate = origin != null ? origin : extractOriginFromReferer(referer);

        if (cookieCredential) {
            // Cookie refresh/logout: Origin or Referer required and must be allowlisted.
            if (candidate == null) {
                writeForbidden(response, "ORIGIN_REQUIRED", "Origin or Referer required for cookie authentication");
                return;
            }
            if (!isAllowedOrigin(candidate)) {
                writeForbidden(response, "ORIGIN_FORBIDDEN", "Origin not allowed");
                return;
            }
            filterChain.doFilter(cached, response);
            return;
        }

        // No cookie credential (native login / Bearer logout-all / change-password).
        // If a browser still sends Origin/Referer, validate it; missing is allowed for native clients.
        if (candidate != null && !isAllowedOrigin(candidate)) {
            writeForbidden(response, "ORIGIN_FORBIDDEN", "Origin not allowed");
            return;
        }

        filterChain.doFilter(cached, response);
    }

    private boolean isAllowedOrigin(String candidate) {
        return corsProperties.allowedOriginsList().stream()
                .anyMatch(allowedOrigin -> allowedOrigin.equalsIgnoreCase(candidate));
    }

    private boolean hasRefreshCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return false;
        }
        String name = cookieProperties.getName();
        for (Cookie cookie : cookies) {
            if (Objects.equals(cookie.getName(), name)
                    && cookie.getValue() != null
                    && !cookie.getValue().isBlank()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasBodyRefreshToken(CachedBodyRequest request) {
        byte[] body = request.getCachedBody();
        if (body.length == 0) {
            return false;
        }
        String contentType = request.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).contains("json")) {
            return false;
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            JsonNode token = node.get("refreshToken");
            return token != null && token.isTextual() && !token.asText().isBlank();
        } catch (Exception e) {
            return false;
        }
    }

    private static String extractOriginFromReferer(String referer) {
        if (referer == null) {
            return null;
        }
        try {
            java.net.URI uri = java.net.URI.create(referer);
            if (uri.getScheme() == null || uri.getHost() == null) {
                return null;
            }
            StringBuilder origin = new StringBuilder();
            origin.append(uri.getScheme()).append("://").append(uri.getHost());
            if (uri.getPort() > 0) {
                origin.append(':').append(uri.getPort());
            }
            return origin.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static void writeForbidden(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"status\":403,\"code\":\"" + code + "\",\"message\":\"" + message + "\"}"
        );
    }

    static final class CachedBodyRequest extends HttpServletRequestWrapper {
        private final byte[] cachedBody;

        CachedBodyRequest(HttpServletRequest request) throws IOException {
            super(request);
            cachedBody = request.getInputStream().readAllBytes();
        }

        byte[] getCachedBody() {
            return cachedBody;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(cachedBody);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return inputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    // no-op
                }

                @Override
                public int read() {
                    return inputStream.read();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}
