package metro.ExoticStamp.modules.auth.domain.exception;

public class OtpInvalidException extends RuntimeException {
    public OtpInvalidException() {
        super("Invalid OTP code");
    }
}

