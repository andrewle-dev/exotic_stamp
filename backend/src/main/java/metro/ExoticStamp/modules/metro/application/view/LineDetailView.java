package metro.ExoticStamp.modules.metro.application.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineDetailView {
    private java.util.UUID id;
    private String code;
    private String name;
    private String displayName;
    private String description;
    private String colorHex;
    private Integer sortOrder;
    private Integer totalStations;
    private String status;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private List<StationView> stations;
}
