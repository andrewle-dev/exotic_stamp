package metro.ExoticStamp.modules.collection.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import metro.ExoticStamp.modules.collection.presentation.response.ProgressResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Schema(description = "Stamp book grid for a line/campaign")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StampBookResponse {
    private UUID lineId;
    private String lineName;
    private UUID campaignId;
    private String campaignName;
    private ProgressResponse progress;
    private List<StampBookStationResponse> stations;

    @Schema(description = "One cell in the stamp book grid")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StampBookStationResponse {
        private UUID stationId;
        private String stationName;
        private Integer sequence;
        private boolean collected;
        private String stampDesignUrl;
        private java.time.LocalDateTime collectedAt;
    }
}

