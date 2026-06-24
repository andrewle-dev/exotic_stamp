package metro.ExoticStamp.modules.metro.application.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineView {
    private UUID id;
    private String code;
    private String name;
    private String displayName;
    private String description;
    private String colorHex;
    private Integer sortOrder;
    private Integer totalStations;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
