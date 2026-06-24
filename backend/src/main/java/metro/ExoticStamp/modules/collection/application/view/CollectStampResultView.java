package metro.ExoticStamp.modules.collection.application.view;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record CollectStampResultView(
        StampInfo stamp,
        ProgressView progress,
        ScanInfo scan,
        boolean isNew
) {
    @Builder
    public record StampInfo(
            UUID stampId,
            UUID stationId,
            String stationName,
            String lineName,
            UUID lineId,
            UUID campaignId,
            String stampDesignUrl,
            LocalDateTime collectedAt
    ) {}

    @Builder
    public record ScanInfo(
            String scanType,
            BigDecimal gpsDistanceMeters,
            BigDecimal gpsAccuracyMeters
    ) {}
}
