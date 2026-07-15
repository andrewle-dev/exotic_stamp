package metro.ExoticStamp.modules.metro.application.view;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class StationScanKeyCreatedView {
    UUID id;
    UUID stationId;
    String scanType;
    /** Returned only once at creation. Never persisted or listed. */
    String payloadToWrite;
    String keyPrefix;
    String status;
    String label;
    String placementNote;
}
