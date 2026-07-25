package metro.ExoticStamp.common.exceptions.storage;

/**
 * Raised when two admins replace the same image concurrently (lost update on pointer).
 */
public class ConcurrentAssetReplaceException extends RuntimeException {

    public ConcurrentAssetReplaceException(String message) {
        super(message);
    }
}
