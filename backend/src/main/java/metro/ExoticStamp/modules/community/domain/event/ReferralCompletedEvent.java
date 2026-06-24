package metro.ExoticStamp.modules.community.domain.event;

import java.util.UUID;

public final class ReferralCompletedEvent {

    private final UUID referralId;
    private final UUID referrerUserId;
    private final UUID referredUserId;

    public ReferralCompletedEvent(UUID referralId, UUID referrerUserId, UUID referredUserId) {
        this.referralId = referralId;
        this.referrerUserId = referrerUserId;
        this.referredUserId = referredUserId;
    }

    public UUID getReferralId() {
        return referralId;
    }

    public UUID getReferrerUserId() {
        return referrerUserId;
    }

    public UUID getReferredUserId() {
        return referredUserId;
    }
}
