package metro.ExoticStamp.common.exceptions.storage;

public class StorageWriteFailedException extends RuntimeException {

    public StorageWriteFailedException(String message) {
        super(message);
    }

    public StorageWriteFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
