package metro.ExoticStamp.modules.user.domain.event;

import java.util.UUID;

public final class EmailVerifiedEvent {

    private final UUID userId;

    public EmailVerifiedEvent(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }
}
