package metro.ExoticStamp.modules.reward.domain.exception;

public class RewardAlreadyIssuedException extends RuntimeException {

    public RewardAlreadyIssuedException(String message) {
        super(message);
    }
}
