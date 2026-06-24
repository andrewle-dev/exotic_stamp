package metro.ExoticStamp.modules.metro.application.view;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record MetroStationView(
        UUID id,
        UUID lineId,
        String name,
        Integer sequence,
        boolean active,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
