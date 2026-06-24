package metro.ExoticStamp.modules.collection.presentation.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Result of a scan/collect operation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StampCollectResponse {
    @Schema(description = "Persisted user stamp id")
    private UUID stampId;
    @Schema(description = "Metro station id")
    private UUID stationId;
    @Schema(description = "Station display name")
    private String stationName;
    @Schema(description = "Line id")
    private UUID lineId;
    @Schema(description = "Campaign id")
    private UUID campaignId;
    @Schema(description = "Artwork URL for the stamp")
    private String stampDesignUrl;
    @Schema(description = "When the stamp was collected")
    private LocalDateTime collectedAt;
    @Schema(description = "False when replayed from idempotency cache")
    @JsonProperty("isNew")
    private boolean isNew;
    @Schema(description = "NFC or QR")
    private String collectMethod;
    @Schema(description = "Progress snapshot for the line")
    private ProgressResponse progress;
}

