package metro.ExoticStamp.modules.reward.application.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

public class RewardReconcileBusyException extends DomainException {

    public RewardReconcileBusyException() {
        super("Reward reconciliation is already running");
    }
}
