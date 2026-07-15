package metro.ExoticStamp.modules.metro.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import metro.ExoticStamp.modules.metro.presentation.dto.ScanTypeApi;
import metro.ExoticStamp.modules.metro.presentation.dto.StationScanKeyStatusApi;

import java.util.UUID;

@Value
@Builder
public class StationScanKeyCreatedResponse {
    UUID id;
    UUID stationId;
    ScanTypeApi scanType;

    @Schema(description = "Full NDEF URI payload to write to the physical tag. Returned only once at creation. "
            + "Mobile should write this as an NDEF URI record. Never stored or listed again.")
    String payloadToWrite;

    String keyPrefix;
    StationScanKeyStatusApi status;
    String label;
    String placementNote;
}
