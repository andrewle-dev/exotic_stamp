package metro.ExoticStamp.modules.collection.application.view;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record StampBookView(
        UUID lineId,
        String lineName,
        UUID campaignId,
        String campaignName,
        List<StationCellView> stations,
        ProgressView progress
) {
    @Builder
    public record StationCellView(
            UUID stationId,
            String stationName,
            Integer sequence,
            boolean collected,
            String stampDesignUrl,
            String stampDesignName,
            String stampDesignDescription,
            String rarity,
            java.time.LocalDateTime collectedAt
    ) {
    }
}
