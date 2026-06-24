package metro.ExoticStamp.modules.metro.domain.exception;

import java.util.UUID;

public class InvalidStationStatusException extends RuntimeException {

    public InvalidStationStatusException(UUID lineId) {
        super("Station cannot be ACTIVE when parent line is not ACTIVE: lineId=" + lineId);
    }
}
