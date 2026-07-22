package metro.ExoticStamp.modules.auth.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Environment-driven refresh cookie policy.
 * Deletion must use the same name, path, domain, sameSite, and secure as set.
 */
@ConfigurationProperties(prefix = "application.auth.cookie")
@Component
@Validated
@Getter
@Setter
public class AuthCookieProperties {

    public static final String LEGACY_PATH = "/api/v1/auth/refresh";

    @NotBlank
    private String name = "refresh_token";

    /** Path covering refresh, logout, logout-all, change-password. */
    @NotBlank
    private String path = "/api/v1/auth";

    /** Optional; omit (null/blank) for host-only cookies. */
    private String domain;

    /**
     * Lax | Strict | None. Case B (same-site cross-origin) should use Lax.
     * None requires Secure=true.
     */
    @NotBlank
    private String sameSite = "Lax";

    /**
     * When true, Secure flag is always set.
     * When false, Secure follows the request ({@code request.isSecure()}).
     */
    private boolean secure = false;

    /** Force Secure=true regardless of request (required in production). */
    private boolean secureAlways = false;

    @NotNull
    private Boolean httpOnly = true;

    @Value("${spring.profiles.active:}")
    private String activeProfiles;

    @PostConstruct
    void requireSecureAlwaysInProduction() {
        if (activeProfiles == null) {
            return;
        }
        for (String profile : activeProfiles.split(",")) {
            if ("prod".equalsIgnoreCase(profile.trim()) && !secureAlways) {
                throw new IllegalStateException(
                        "application.auth.cookie.secure-always must be true when spring.profiles.active includes prod"
                );
            }
        }
    }
}
