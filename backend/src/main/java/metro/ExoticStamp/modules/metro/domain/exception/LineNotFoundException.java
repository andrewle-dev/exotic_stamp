package metro.ExoticStamp.modules.metro.domain.exception;

import java.util.UUID;

public class LineNotFoundException extends RuntimeException {

    public LineNotFoundException(UUID lineId) {
        super("Line not found with id: " + lineId);
    }
}




