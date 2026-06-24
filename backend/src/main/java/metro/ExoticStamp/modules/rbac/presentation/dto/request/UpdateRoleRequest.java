package metro.ExoticStamp.modules.rbac.presentation.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {

    @Size(max = 64)
    private String roleCode;

    @Size(max = 500)
    private String description;

    @Size(max = 20)
    private String status;
}
