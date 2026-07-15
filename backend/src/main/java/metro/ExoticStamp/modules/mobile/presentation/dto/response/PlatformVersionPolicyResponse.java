package metro.ExoticStamp.modules.mobile.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Platform-specific app version and store update policy")
public record PlatformVersionPolicyResponse(
        @Schema(description = "Minimum app version the client must run; below this => force update path", example = "0.1.0")
        String minimumSupportedVersion,
        @Schema(description = "Latest published app version", example = "0.1.0")
        String latestVersion,
        @Schema(description = "When true, clients below latest/minimum policy must update before continuing", example = "false")
        boolean forceUpdate,
        @Schema(description = "Store listing URL for this platform; null when not configured", example = "https://play.google.com/store/apps/details?id=com.example")
        String storeUrl
) {
}
