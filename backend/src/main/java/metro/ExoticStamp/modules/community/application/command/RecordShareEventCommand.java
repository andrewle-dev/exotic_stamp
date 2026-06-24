package metro.ExoticStamp.modules.community.application.command;

import java.util.Map;
import java.util.UUID;

public record RecordShareEventCommand(
        String platform,
        String shareType,
        UUID targetId,
        Map<String, Object> metadata
) {
}
