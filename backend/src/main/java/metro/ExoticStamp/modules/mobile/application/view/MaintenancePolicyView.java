package metro.ExoticStamp.modules.mobile.application.view;

import lombok.Builder;

@Builder
public record MaintenancePolicyView(
        boolean enabled,
        String message
) {
}
