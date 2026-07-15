package metro.ExoticStamp.modules.collection.application.command;

import java.util.List;
import java.util.UUID;

public record ReorderStampDesignsCommand(UUID campaignId, List<UUID> orderedIds) {
}
