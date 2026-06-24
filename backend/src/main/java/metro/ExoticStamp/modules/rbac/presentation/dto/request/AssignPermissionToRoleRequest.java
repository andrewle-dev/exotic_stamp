package metro.ExoticStamp.modules.rbac.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignPermissionToRoleRequest {

    @NotBlank(message = "permissionCode is required")
    @Size(max = 80)
    private String permissionCode;
}
