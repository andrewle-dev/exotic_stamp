package metro.ExoticStamp.modules.metro.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import metro.ExoticStamp.modules.metro.presentation.dto.ScanTypeApi;

@Data
public class ScanResolveRequest {
    @NotNull
    private ScanTypeApi scanType;
    @NotBlank
    private String payload;
    private String devicePlatform;
    private String appVersion;
}
