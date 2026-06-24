package metro.ExoticStamp.modules.metro.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import metro.ExoticStamp.modules.metro.presentation.dto.MetroStatusApi;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineDetailResponse {
    private UUID id;
    private String code;
    private String name;
    private String displayName;
    private String description;
    private String colorHex;
    private Integer sortOrder;
    private Integer totalStations;
    private MetroStatusApi status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<StationResponse> stations;
}
