package metro.ExoticStamp.modules.metro.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResolveStationResponse {
    private UUID id;
    private String code;
    private String name;
    private String displayName;
    private UUID lineId;
    private String lineCode;
    private String lineName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer zoneRadiusMeters;
    private String imageUrl;
    private String stampPreviewUrl;
}
