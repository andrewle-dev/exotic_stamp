package metro.ExoticStamp.modules.reward.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

public class MilestoneCodeDuplicateException extends DomainException {

    public MilestoneCodeDuplicateException(String code) {
        super("Milestone code already exists for campaign: " + code);
    }
}
