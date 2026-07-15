package metro.ExoticStamp.modules.metro.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import metro.ExoticStamp.modules.metro.presentation.dto.ScanTypeApi;

@Data
public class ScanResolveRequest {
    @NotNull
    private ScanTypeApi scanType;

    @NotBlank
    @Schema(description = "Raw key (nfc_…) or metrostamp URI (metrostamp://scan?k=nfc_…)")
    private String payload;

    private String devicePlatform;
    private String appVersion;
}
