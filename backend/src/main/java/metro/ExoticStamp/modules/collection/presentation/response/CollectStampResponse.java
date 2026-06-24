package metro.ExoticStamp.modules.collection.presentation.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Stage 4 collect result")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectStampResponse {
    private StampInfoResponse stamp;
    private ProgressResponse progress;
    private ScanInfoResponse scan;

    @JsonProperty("isNew")
    private boolean isNew;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StampInfoResponse {
        private UUID stampId;
        private UUID stationId;
        private String stationName;
        private String lineName;
        private UUID lineId;
        private UUID campaignId;
        private String stampDesignUrl;
        private LocalDateTime collectedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScanInfoResponse {
        private String scanType;
        private BigDecimal gpsDistanceMeters;
        private BigDecimal gpsAccuracyMeters;
    }
}
