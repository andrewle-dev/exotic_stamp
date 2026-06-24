package metro.ExoticStamp.modules.metro.presentation.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import metro.ExoticStamp.modules.metro.presentation.dto.MetroStatusApi;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateStationRequest {
    @NotNull
    private UUID lineId;
    @NotBlank
    @Size(max = 20)
    private String code;
    @NotBlank
    @Size(max = 100)
    private String name;
    @Size(max = 100)
    private String displayName;
    @Size(max = 500)
    private String description;
    @Size(max = 255)
    private String address;
    @Min(0)
    private Integer sortOrder;
    @DecimalMin("-90")
    @DecimalMax("90")
    private BigDecimal latitude;
    @DecimalMin("-180")
    @DecimalMax("180")
    private BigDecimal longitude;
    @Min(20)
    @Max(1000)
    private Integer zoneRadiusMeters;
    private String imageUrl;
    private String stampPreviewUrl;
    private String nfcTagId;
    private String qrCodeValue;
    private MetroStatusApi status;
}
