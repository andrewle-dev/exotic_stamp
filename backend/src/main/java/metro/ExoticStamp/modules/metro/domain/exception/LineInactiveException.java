package metro.ExoticStamp.modules.metro.domain.exception;

import java.util.UUID;

public class LineInactiveException extends RuntimeException {

    public LineInactiveException(UUID lineId) {
        super("Metro line is inactive: " + lineId);
    }
}
