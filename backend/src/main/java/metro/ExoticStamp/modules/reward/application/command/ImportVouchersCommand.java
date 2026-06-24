package metro.ExoticStamp.modules.reward.application.command;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ImportVouchersCommand(
        UUID milestoneId,
        List<String> codes,
        LocalDateTime expiresAt
) {
}
