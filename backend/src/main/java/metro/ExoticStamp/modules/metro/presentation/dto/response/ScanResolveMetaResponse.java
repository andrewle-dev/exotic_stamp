package metro.ExoticStamp.modules.metro.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import metro.ExoticStamp.modules.metro.presentation.dto.ScanTypeApi;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResolveMetaResponse {
    private ScanTypeApi scanType;
    private boolean resolved;
}
