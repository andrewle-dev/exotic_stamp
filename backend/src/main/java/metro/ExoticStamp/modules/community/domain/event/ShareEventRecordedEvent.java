package metro.ExoticStamp.modules.community.domain.event;

import java.util.UUID;

public final class ShareEventRecordedEvent {

    private final UUID shareEventId;
    private final UUID userId;

    public ShareEventRecordedEvent(UUID shareEventId, UUID userId) {
        this.shareEventId = shareEventId;
        this.userId = userId;
    }

    public UUID getShareEventId() {
        return shareEventId;
    }

    public UUID getUserId() {
        return userId;
    }
}
