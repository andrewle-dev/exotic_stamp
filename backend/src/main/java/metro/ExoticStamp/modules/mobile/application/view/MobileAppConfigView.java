package metro.ExoticStamp.modules.mobile.application.view;

import lombok.Builder;

@Builder
public record MobileAppConfigView(
        PlatformVersionPolicyView android,
        PlatformVersionPolicyView ios,
        MaintenancePolicyView maintenance
) {
}
