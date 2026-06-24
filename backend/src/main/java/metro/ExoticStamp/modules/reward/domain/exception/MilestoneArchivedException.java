package metro.ExoticStamp.modules.reward.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

import java.util.UUID;

public class MilestoneArchivedException extends DomainException {

    public MilestoneArchivedException(UUID milestoneId) {
        super("Milestone is archived and immutable: " + milestoneId);
    }
}
