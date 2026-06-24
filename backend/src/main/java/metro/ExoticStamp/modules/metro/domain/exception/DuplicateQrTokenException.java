package metro.ExoticStamp.modules.metro.domain.exception;

public class DuplicateQrTokenException extends RuntimeException {

    public DuplicateQrTokenException(String qrCodeToken) {
        super("QR code token already in use: " + qrCodeToken);
    }
}
