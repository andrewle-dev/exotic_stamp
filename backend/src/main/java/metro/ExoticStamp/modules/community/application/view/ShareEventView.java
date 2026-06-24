package metro.ExoticStamp.modules.community.application.view;

import lombok.Builder;
import metro.ExoticStamp.modules.community.domain.model.SharePlatform;
import metro.ExoticStamp.modules.community.domain.model.ShareType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Builder
public record ShareEventView(
        UUID id,
        String platform,
        String shareType,
        UUID targetId,
        Map<String, Object> metadata,
        LocalDateTime sharedAt
) {
}
