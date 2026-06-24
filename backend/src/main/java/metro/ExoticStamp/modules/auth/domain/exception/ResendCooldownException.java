package metro.ExoticStamp.modules.auth.domain.exception;

import lombok.Getter;

@Getter
public class ResendCooldownException extends RuntimeException {

    private final long secondsRemaining;

    public ResendCooldownException(long secondsRemaining) {
        super("Please wait " + secondsRemaining + " seconds before requesting another OTP.");
        this.secondsRemaining = secondsRemaining;
    }
}
