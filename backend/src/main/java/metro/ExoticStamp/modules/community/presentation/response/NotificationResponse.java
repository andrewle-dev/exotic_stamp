package metro.ExoticStamp.modules.community.presentation.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Builder
public record NotificationResponse(
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
