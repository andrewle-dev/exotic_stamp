package metro.ExoticStamp.modules.metro.domain.exception;

import java.util.UUID;

public class ScanKeyAlreadyActiveException extends RuntimeException {

    public ScanKeyAlreadyActiveException(UUID id) {
        super("Scan key is already active: " + id);
    }
}
