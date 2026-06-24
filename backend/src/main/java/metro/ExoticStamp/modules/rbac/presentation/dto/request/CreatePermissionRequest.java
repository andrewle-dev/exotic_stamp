package metro.ExoticStamp.modules.rbac.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePermissionRequest {

    @NotBlank(message = "permissionCode is required")
    @Size(max = 80)
    private String permissionCode;

    @Size(max = 500)
    private String description;
}
