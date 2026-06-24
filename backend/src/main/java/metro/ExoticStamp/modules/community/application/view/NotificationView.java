package metro.ExoticStamp.modules.community.application.view;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Builder
public record NotificationView(
        UUID id,
        String type,
        String title,
        String body,
        String referenceId,
        boolean read,
        LocalDateTime createdAt,
        LocalDateTime readAt,
        String deepLink,
        Map<String, Object> metadata
) {
}
