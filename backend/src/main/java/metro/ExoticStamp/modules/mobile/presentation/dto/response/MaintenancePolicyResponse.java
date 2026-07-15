package metro.ExoticStamp.modules.mobile.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Global maintenance mode flag for mobile clients")
public record MaintenancePolicyResponse(
        @Schema(description = "When true, clients should show maintenance UI and block normal flows", example = "false")
        boolean enabled,
        @Schema(description = "Optional maintenance message for the client UI; null when unset", example = "Scheduled maintenance")
        String message
) {
}
