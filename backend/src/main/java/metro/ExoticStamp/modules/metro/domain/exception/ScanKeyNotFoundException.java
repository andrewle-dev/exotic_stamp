package metro.ExoticStamp.modules.metro.domain.exception;

public class ScanKeyNotFoundException extends RuntimeException {

    public ScanKeyNotFoundException() {
        super("Unknown scan key");
    }
}
