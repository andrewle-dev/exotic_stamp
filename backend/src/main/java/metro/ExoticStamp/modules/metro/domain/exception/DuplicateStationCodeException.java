package metro.ExoticStamp.modules.metro.domain.exception;

import java.util.UUID;

public class DuplicateStationCodeException extends RuntimeException {

    public DuplicateStationCodeException(String code, UUID lineId) {
        super("Station code already exists on this line: " + code + " (lineId=" + lineId + ")");
    }
}



