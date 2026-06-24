package metro.ExoticStamp.modules.collection.application.view;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record StampCollectView(
        UUID stampId,
        UUID stationId,
        String stationName,
        UUID lineId,
        UUID campaignId,
        String stampDesignUrl,
        LocalDateTime collectedAt,
        boolean isNew,
        String collectMethod,
        ProgressView progress
) {
}
