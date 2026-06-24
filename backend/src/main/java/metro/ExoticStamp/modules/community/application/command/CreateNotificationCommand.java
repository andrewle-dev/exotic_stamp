package metro.ExoticStamp.modules.community.application.command;

import metro.ExoticStamp.modules.community.domain.model.NotificationType;

import java.util.Map;

public record CreateNotificationCommand(
        java.util.UUID userId,
        NotificationType type,
        String title,
        String body,
        String referenceId,
        String deepLink,
        Map<String, Object> metadata
) {
}
