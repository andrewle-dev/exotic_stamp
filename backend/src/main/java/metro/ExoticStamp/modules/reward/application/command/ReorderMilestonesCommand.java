package metro.ExoticStamp.modules.reward.application.command;

import java.util.List;
import java.util.UUID;

public record ReorderMilestonesCommand(UUID campaignId, List<UUID> orderedIds) {
}
