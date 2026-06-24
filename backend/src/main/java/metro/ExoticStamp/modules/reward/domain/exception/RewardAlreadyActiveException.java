package metro.ExoticStamp.modules.reward.domain.exception;

public class RewardAlreadyActiveException extends RuntimeException {

    public RewardAlreadyActiveException(String message) {
        super(message);
    }
}
