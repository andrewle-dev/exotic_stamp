package metro.ExoticStamp.infra.security.ratelimit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import metro.ExoticStamp.common.exceptions.security.SecurityDependencyUnavailableException;
import metro.ExoticStamp.common.response.ErrorResponse;
import metro.ExoticStamp.common.security.AuthenticatedUser;
import metro.ExoticStamp.infra.security.ClientIpResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Endpoint-specific composite rate limits for auth, scan resolve, and collect POSTs.
 * Never logs bucket keys, emails, tokens, or raw scan payloads.
 * Registered only via {@link RateLimitAutoConfig} / SecurityFilterChain (not servlet filter scan).
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final String HEADER_DEVICE_ID = "X-Device-Id";
    private static final String HEADER_IDEMPOTENCY_KEY = "X-Idempotency-Key";

    private final RateLimitProperties properties;
    private final RateLimitService rateLimitService;
    private final RateLimitKeyHasher keyHasher;
    private final ClientIpResolver clientIpResolver;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(
            RateLimitProperties properties,
            RateLimitService rateLimitService,
            RateLimitKeyHasher keyHasher,
            ClientIpResolver clientIpResolver,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.rateLimitService = rateLimitService;
        this.keyHasher = keyHasher;
        this.clientIpResolver = clientIpResolver;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!properties.isEnabled() || !"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = normalizePath(request.getRequestURI());
        Optional<Route> route = matchRoute(path);
        if (route.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        CachedBodyHttpServletRequest cachedRequest = needsBody(route.get())
                ? new CachedBodyHttpServletRequest(request)
                : null;
        HttpServletRequest effective = cachedRequest != null ? cachedRequest : request;

        try {
            String ip = clientIpResolver.resolve(request);
            String bucketKey = buildBucketKey(route.get(), effective, cachedRequest, ip);
            rateLimitService.tryConsume(route.get().policy(), bucketKey);
        } catch (RateLimitExceededException ex) {
            writeError(response, request, 429, "RATE_LIMIT_EXCEEDED",
                    "Too many requests. Please try again later.", ex.getRetryAfterSeconds());
            return;
        } catch (SecurityDependencyUnavailableException ex) {
            writeError(response, request, 503, "SECURITY_DEPENDENCY_UNAVAILABLE",
                    ex.getMessage(), 5);
            return;
        } catch (IllegalStateException ex) {
            // Misconfiguration (e.g. missing pepper) — fail closed without leaking details
            log.warn("Rate limit configuration error: {}", ex.getClass().getSimpleName());
            writeError(response, request, 503, "SECURITY_DEPENDENCY_UNAVAILABLE",
                    "Security dependency temporarily unavailable", 5);
            return;
        }

        filterChain.doFilter(effective, response);
    }

    private boolean needsBody(Route route) {
        return switch (route.policy()) {
            case LOGIN, REGISTER, OTP_ISSUE, OTP_VERIFY, SCAN_RESOLVE, COLLECT -> true;
            case REFRESH -> false;
        };
    }

    private String buildBucketKey(
            Route route,
            HttpServletRequest request,
            CachedBodyHttpServletRequest cached,
            String ip
    ) {
        JsonNode body = cached != null ? parseBodyQuietly(cached.getCachedBody()) : null;

        return switch (route.policy()) {
            case LOGIN -> keyHasher.buildKey(
                    RateLimitPolicyName.LOGIN,
                    ip,
                    hashEmailLike(extractLoginIdentifier(body))
            );
            case REGISTER -> keyHasher.buildKey(
                    RateLimitPolicyName.REGISTER,
                    ip,
                    hashEmailLike(textField(body, "email"))
            );
            case OTP_ISSUE -> keyHasher.buildKey(
                    RateLimitPolicyName.OTP_ISSUE,
                    ip,
                    hashEmailLike(textField(body, "email"))
            );
            case OTP_VERIFY -> keyHasher.buildKey(
                    RateLimitPolicyName.OTP_VERIFY,
                    ip,
                    hashEmailLike(textField(body, "email"))
            );
            case REFRESH -> {
                String device = request.getHeader(HEADER_DEVICE_ID);
                String deviceHash = StringUtils.hasText(device)
                        ? keyHasher.hmacHex(device.trim())
                        : null;
                yield keyHasher.buildKey(RateLimitPolicyName.REFRESH, ip, deviceHash);
            }
            case SCAN_RESOLVE -> {
                String scanRaw = firstNonBlank(
                        textField(body, "payload"),
                        textField(body, "scanKey"),
                        textField(body, "token")
                );
                yield keyHasher.buildKey(
                        RateLimitPolicyName.SCAN_RESOLVE,
                        ip,
                        keyHasher.fingerprintScanKey(scanRaw != null ? scanRaw : "")
                );
            }
            case COLLECT -> {
                String userPart = resolveUserIdHash().orElse(null);
                String idemRaw = firstNonBlank(
                        request.getHeader(HEADER_IDEMPOTENCY_KEY),
                        textField(body, "idempotencyKey")
                );
                String idemHash = StringUtils.hasText(idemRaw)
                        ? keyHasher.hmacHex(idemRaw.trim())
                        : null;
                yield keyHasher.buildKey(RateLimitPolicyName.COLLECT, ip, userPart, idemHash);
            }
        };
    }

    private Optional<String> resolveUserIdHash() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof AuthenticatedUser user && user.getUserId() != null) {
            return Optional.of(keyHasher.hmacHex(user.getUserId().toString()));
        }
        if (principal != null) {
            String name = auth.getName();
            if (StringUtils.hasText(name)) {
                try {
                    UUID.fromString(name);
                    return Optional.of(keyHasher.hmacHex(name));
                } catch (IllegalArgumentException ignored) {
                    // not a UUID username
                }
            }
        }
        return Optional.empty();
    }

    /**
     * LoginRequest uses {@code identifier}; also accept {@code email} / email-like {@code username}.
     */
    private String extractLoginIdentifier(JsonNode body) {
        String email = textField(body, "email");
        if (StringUtils.hasText(email)) {
            return email;
        }
        String identifier = textField(body, "identifier");
        if (StringUtils.hasText(identifier) && looksLikeEmail(identifier)) {
            return identifier;
        }
        String username = textField(body, "username");
        if (StringUtils.hasText(username) && looksLikeEmail(username)) {
            return username;
        }
        // Still hash non-email identifier for composite key (username/phone login)
        if (StringUtils.hasText(identifier)) {
            return identifier;
        }
        return username;
    }

    private String hashEmailLike(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String trimmed = raw.trim();
        if (looksLikeEmail(trimmed)) {
            return keyHasher.hmacHex(keyHasher.normalizeEmail(trimmed));
        }
        // phone-ish or opaque identifier — normalize phone chars then HMAC
        String phone = keyHasher.normalizePhone(trimmed);
        return keyHasher.hmacHex(phone.isEmpty() ? trimmed.toLowerCase(Locale.ROOT) : phone);
    }

    private static boolean looksLikeEmail(String value) {
        int at = value.indexOf('@');
        return at > 0 && at < value.length() - 1 && value.indexOf('@', at + 1) < 0;
    }

    private JsonNode parseBodyQuietly(byte[] body) {
        if (body == null || body.length == 0) {
            return null;
        }
        try {
            return objectMapper.readTree(body);
        } catch (Exception e) {
            return null;
        }
    }

    private static String textField(JsonNode body, String field) {
        if (body == null || !body.has(field) || body.get(field).isNull()) {
            return null;
        }
        String v = body.get(field).asText(null);
        return StringUtils.hasText(v) ? v : null;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (StringUtils.hasText(v)) {
                return v;
            }
        }
        return null;
    }

    private static String normalizePath(String uri) {
        if (uri == null) {
            return "";
        }
        int q = uri.indexOf('?');
        String path = q >= 0 ? uri.substring(0, q) : uri;
        if (path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    private static Optional<Route> matchRoute(String path) {
        return switch (path) {
            case "/api/v1/auth/login" -> Optional.of(new Route(RateLimitPolicyName.LOGIN));
            case "/api/v1/auth/register" -> Optional.of(new Route(RateLimitPolicyName.REGISTER));
            case "/api/v1/auth/forgot-password",
                 "/api/v1/auth/resend-otp",
                 "/api/v1/auth/resend-verification-otp" -> Optional.of(new Route(RateLimitPolicyName.OTP_ISSUE));
            case "/api/v1/auth/verify-account",
                 "/api/v1/auth/reset-password" -> Optional.of(new Route(RateLimitPolicyName.OTP_VERIFY));
            case "/api/v1/auth/refresh" -> Optional.of(new Route(RateLimitPolicyName.REFRESH));
            case "/api/v1/metro/scan/resolve" -> Optional.of(new Route(RateLimitPolicyName.SCAN_RESOLVE));
            case "/api/v1/collection/collect",
                 "/api/v1/collections/scan" -> Optional.of(new Route(RateLimitPolicyName.COLLECT));
            default -> Optional.empty();
        };
    }

    private void writeError(
            HttpServletResponse response,
            HttpServletRequest request,
            int status,
            String code,
            String message,
            long retryAfterSeconds
    ) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        if (retryAfterSeconds > 0) {
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        }
        ErrorResponse err = ErrorResponse.of(code, message, status, request.getRequestURI());
        objectMapper.writeValue(response.getOutputStream(), err);
    }

    private record Route(RateLimitPolicyName policy) {
    }
}
