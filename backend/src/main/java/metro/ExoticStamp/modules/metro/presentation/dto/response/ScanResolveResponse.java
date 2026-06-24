package metro.ExoticStamp.modules.metro.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResolveResponse {
    private ScanResolveStationResponse station;
    private ScanResolveMetaResponse scan;
}
