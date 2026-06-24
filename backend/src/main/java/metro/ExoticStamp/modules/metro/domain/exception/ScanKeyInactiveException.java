package metro.ExoticStamp.modules.metro.domain.exception;

public class ScanKeyInactiveException extends RuntimeException {

    public ScanKeyInactiveException() {
        super("Scan key is inactive");
    }
}
