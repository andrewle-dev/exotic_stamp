package metro.ExoticStamp.modules.metro.presentation.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationStatsResponse {

    private UUID stationId;
    private String stationName;
    private String lineName;
    private Integer collectorCount;
}



