package metro.ExoticStamp.modules.metro.domain.exception;

public class DuplicateNfcTagException extends RuntimeException {

    public DuplicateNfcTagException(String nfcTagId) {
        super("NFC tag already in use: " + nfcTagId);
    }
}
