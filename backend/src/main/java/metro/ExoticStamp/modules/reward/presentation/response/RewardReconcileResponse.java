package metro.ExoticStamp.modules.reward.presentation.response;

import java.util.UUID;

public record RewardReconcileResponse(
        int missingExamined,
        int missingRepaired,
        int pendingExamined,
        int pendingFulfilled,
        int stillNoStock,
        int failed,
        boolean skipped,
        String skipReason,
        boolean dryRun,
        UUID initiatedByAdminId
) {
}
