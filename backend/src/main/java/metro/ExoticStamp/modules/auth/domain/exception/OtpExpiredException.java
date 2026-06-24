package metro.ExoticStamp.modules.auth.domain.exception;

public class OtpExpiredException extends RuntimeException {
    public OtpExpiredException() {
        super("OTP expired or does not exist");
    }
}

