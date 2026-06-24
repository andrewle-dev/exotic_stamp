package metro.ExoticStamp.modules.collection.application.view;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserStampView(
        UUID stampId,
        UUID stationId,
        UUID lineId,
        UUID campaignId,
        String stationName,
        String stampDesignUrl,
        LocalDateTime collectedAt,
        String collectMethod
) {
}
