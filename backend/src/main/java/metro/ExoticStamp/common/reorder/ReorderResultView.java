package metro.ExoticStamp.common.reorder;

import java.util.List;
import java.util.UUID;

public record ReorderResultView(UUID scopeId, int updatedCount, List<ReorderItemView> items) {
}
