package metro.ExoticStamp.modules.metro.application.view;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class StationScanKeyVerifyView {
    boolean verified;
    UUID id;
    UUID stationId;
    LocalDateTime lastInstallVerifiedAt;
}
