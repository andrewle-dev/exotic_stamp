package metro.ExoticStamp.modules.mobile.presentation.mapper;

import metro.ExoticStamp.modules.mobile.application.view.MaintenancePolicyView;
import metro.ExoticStamp.modules.mobile.application.view.MobileAppConfigView;
import metro.ExoticStamp.modules.mobile.application.view.PlatformVersionPolicyView;
import metro.ExoticStamp.modules.mobile.presentation.dto.response.MaintenancePolicyResponse;
import metro.ExoticStamp.modules.mobile.presentation.dto.response.MobileAppConfigResponse;
import metro.ExoticStamp.modules.mobile.presentation.dto.response.PlatformVersionPolicyResponse;
import org.springframework.stereotype.Component;

@Component
public class MobileAppConfigPresentationMapper {

    public MobileAppConfigResponse toResponse(MobileAppConfigView view) {
        return MobileAppConfigResponse.builder()
                .android(toPlatformResponse(view.android()))
                .ios(toPlatformResponse(view.ios()))
                .maintenance(toMaintenanceResponse(view.maintenance()))
                .build();
    }

    private static PlatformVersionPolicyResponse toPlatformResponse(PlatformVersionPolicyView view) {
        return PlatformVersionPolicyResponse.builder()
                .minimumSupportedVersion(view.minimumSupportedVersion())
                .latestVersion(view.latestVersion())
                .forceUpdate(view.forceUpdate())
                .storeUrl(view.storeUrl())
                .build();
    }

    private static MaintenancePolicyResponse toMaintenanceResponse(MaintenancePolicyView view) {
        return MaintenancePolicyResponse.builder()
                .enabled(view.enabled())
                .message(view.message())
                .build();
    }
}
