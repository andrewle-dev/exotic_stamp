package metro.ExoticStamp.modules.rbac.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevokeRoleRequest {

    @NotNull(message = "userId is required")
    private UUID userId;

    @NotBlank(message = "roleName is required")
    private String roleName;
}
