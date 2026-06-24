package metro.ExoticStamp.modules.collection.application.view;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record ResolvedStationView(
        UUID id,
        String code,
        String name,
        String displayName,
        UUID lineId,
        String lineCode,
        String lineName,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer zoneRadiusMeters,
        String imageUrl,
        String stampPreviewUrl,
        String scanType
) {}
