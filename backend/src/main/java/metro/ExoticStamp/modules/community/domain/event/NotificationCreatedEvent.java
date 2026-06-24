package metro.ExoticStamp.modules.community.domain.event;

import metro.ExoticStamp.modules.community.domain.model.NotificationType;

import java.util.UUID;

public final class NotificationCreatedEvent {

    private final UUID notificationId;
    private final UUID userId;
    private final NotificationType type;

    public NotificationCreatedEvent(UUID notificationId, UUID userId, NotificationType type) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.type = type;
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public NotificationType getType() {
        return type;
    }
}
