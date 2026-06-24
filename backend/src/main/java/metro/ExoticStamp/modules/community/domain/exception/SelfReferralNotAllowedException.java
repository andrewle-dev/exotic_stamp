package metro.ExoticStamp.modules.community.domain.exception;

public class SelfReferralNotAllowedException extends RuntimeException {
    public SelfReferralNotAllowedException(String message) {
        super(message);
    }
}
