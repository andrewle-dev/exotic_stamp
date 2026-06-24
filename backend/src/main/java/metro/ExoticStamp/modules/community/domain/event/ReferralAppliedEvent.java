package metro.ExoticStamp.modules.community.domain.event;

import java.util.UUID;

public final class ReferralAppliedEvent {

    private final UUID referralId;
    private final UUID referrerUserId;
    private final UUID referredUserId;
    private final UUID referralCodeId;

    public ReferralAppliedEvent(UUID referralId, UUID referrerUserId, UUID referredUserId, UUID referralCodeId) {
        this.referralId = referralId;
        this.referrerUserId = referrerUserId;
        this.referredUserId = referredUserId;
        this.referralCodeId = referralCodeId;
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

    public UUID getReferralCodeId() {
        return referralCodeId;
    }
}
