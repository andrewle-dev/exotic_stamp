package metro.ExoticStamp.modules.mobile.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Mobile client update / maintenance policy.
 * Driven by {@code mobile.app-config.*} and env overrides — not the installed app version UI.
 */
@Data
@Validated
@ConfigurationProperties(prefix = "mobile.app-config")
public class MobileAppConfigProperties {

    @Valid
    @NotNull
    private PlatformPolicy android = new PlatformPolicy();

    @Valid
    @NotNull
    private PlatformPolicy ios = new PlatformPolicy();

    @Valid
    @NotNull
    private MaintenancePolicy maintenance = new MaintenancePolicy();

    @Data
    public static class PlatformPolicy {
        @NotBlank
        private String minimumSupportedVersion = "0.1.0";

        @NotBlank
        private String latestVersion = "0.1.0";

        private boolean forceUpdate;

        /** Optional store listing URL; empty means unset. */
        private String storeUrl = "";
    }

    @Data
    public static class MaintenancePolicy {
        private boolean enabled;

        /** Optional client-facing maintenance message; empty means unset. */
        private String message = "";
    }
}
