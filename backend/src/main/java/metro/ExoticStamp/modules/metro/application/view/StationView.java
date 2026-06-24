package metro.ExoticStamp.modules.metro.application.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationView {
    private UUID id;
    private UUID lineId;
    private String lineCode;
    private String lineName;
    private String code;
    private String name;
    private String displayName;
    private String description;
    private String address;
    private String imageUrl;
    private String stampPreviewUrl;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer zoneRadiusMeters;
    private Integer sortOrder;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
