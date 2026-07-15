package metro.ExoticStamp.modules.mobile.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Mobile update policy and maintenance policy. Does not reflect the installed binary version.")
public record MobileAppConfigResponse(
        @Schema(description = "Android version / store policy")
        PlatformVersionPolicyResponse android,
        @Schema(description = "iOS version / store policy")
        PlatformVersionPolicyResponse ios,
        @Schema(description = "Maintenance mode policy")
        MaintenancePolicyResponse maintenance
) {
}
