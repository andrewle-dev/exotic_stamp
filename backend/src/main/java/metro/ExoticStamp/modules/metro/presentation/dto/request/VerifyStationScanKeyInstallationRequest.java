package metro.ExoticStamp.modules.metro.presentation.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyStationScanKeyInstallationRequest {

    @NotBlank
    private String payloadReadBack;

    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double latitude;

    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double longitude;

    @DecimalMin("0.0")
    private Double accuracyMeters;

    @Size(max = 30)
    private String devicePlatform;

    @Size(max = 50)
    private String appVersion;
}
