package metro.ExoticStamp.modules.metro.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import metro.ExoticStamp.modules.metro.presentation.dto.ScanTypeApi;

@Data
public class CreateStationScanKeyRequest {

    @NotNull
    private ScanTypeApi scanType;

    @Size(max = 100)
    private String label;

    @Size(max = 255)
    private String placementNote;
}
