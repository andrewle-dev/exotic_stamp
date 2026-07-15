package metro.ExoticStamp.modules.metro.presentation.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RevokeStationScanKeyRequest {

    @Size(max = 255)
    private String reason;
}
