package metro.ExoticStamp.modules.auth.domain.exception;

public class OtpMaxAttemptsExceededException extends RuntimeException {

    private final int maxAttempts;

    public OtpMaxAttemptsExceededException(int maxAttempts) {
        super("Maximum OTP resend attempts (" + maxAttempts + ") exceeded. Please try again in 1 hour.");
        this.maxAttempts = maxAttempts;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }
}

