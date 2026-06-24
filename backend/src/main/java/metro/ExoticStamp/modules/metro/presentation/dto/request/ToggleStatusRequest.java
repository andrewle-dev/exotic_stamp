package metro.ExoticStamp.modules.metro.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToggleStatusRequest {

    @NotNull
    private Boolean isActive;
}
