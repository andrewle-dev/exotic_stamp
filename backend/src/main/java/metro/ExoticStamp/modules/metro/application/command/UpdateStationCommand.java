package metro.ExoticStamp.modules.metro.application.command;

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
public class UpdateStationCommand {
    private UUID stationId;
    private String code;
    private String name;
    private String displayName;
    private Integer sortOrder;
    private String description;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer zoneRadiusMeters;
    private String imageUrl;
    private String stampPreviewUrl;
    private String status;
}
