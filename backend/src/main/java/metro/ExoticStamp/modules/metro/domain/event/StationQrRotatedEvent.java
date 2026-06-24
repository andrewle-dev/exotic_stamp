package metro.ExoticStamp.modules.metro.domain.event;

import java.util.UUID;

public record StationQrRotatedEvent(UUID stationId, String oldQrToken, String newQrToken) {}



