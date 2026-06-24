package metro.ExoticStamp.modules.community.presentation.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Builder
public record ShareEventResponse(
        UUID id,
        String platform,
        String shareType,
        UUID targetId,
        Map<String, Object> metadata,
        LocalDateTime sharedAt
) {
}
