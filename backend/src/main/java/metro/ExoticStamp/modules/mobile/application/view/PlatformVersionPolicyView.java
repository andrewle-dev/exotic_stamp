package metro.ExoticStamp.modules.mobile.application.view;

import lombok.Builder;

@Builder
public record PlatformVersionPolicyView(
        String minimumSupportedVersion,
        String latestVersion,
        boolean forceUpdate,
        String storeUrl
) {
}
