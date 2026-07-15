package metro.ExoticStamp.modules.mobile.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.mobile.application.view.MaintenancePolicyView;
import metro.ExoticStamp.modules.mobile.application.view.MobileAppConfigView;
import metro.ExoticStamp.modules.mobile.application.view.PlatformVersionPolicyView;
import metro.ExoticStamp.modules.mobile.config.MobileAppConfigProperties;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MobileAppConfigQueryService {

    private final MobileAppConfigProperties properties;

    public MobileAppConfigView getAppConfig() {
        return MobileAppConfigView.builder()
                .android(toPlatformView(properties.getAndroid()))
                .ios(toPlatformView(properties.getIos()))
                .maintenance(toMaintenanceView(properties.getMaintenance()))
                .build();
    }

    private static PlatformVersionPolicyView toPlatformView(MobileAppConfigProperties.PlatformPolicy policy) {
        return PlatformVersionPolicyView.builder()
                .minimumSupportedVersion(policy.getMinimumSupportedVersion())
                .latestVersion(policy.getLatestVersion())
                .forceUpdate(policy.isForceUpdate())
                .storeUrl(blankToNull(policy.getStoreUrl()))
                .build();
    }

    private static MaintenancePolicyView toMaintenanceView(MobileAppConfigProperties.MaintenancePolicy policy) {
        return MaintenancePolicyView.builder()
                .enabled(policy.isEnabled())
                .message(blankToNull(policy.getMessage()))
                .build();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
