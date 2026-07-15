package metro.ExoticStamp.modules.metro.presentation.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class StationScanKeyVerifyResponse {
    boolean verified;
    UUID id;
    UUID stationId;
    LocalDateTime lastInstallVerifiedAt;
}
