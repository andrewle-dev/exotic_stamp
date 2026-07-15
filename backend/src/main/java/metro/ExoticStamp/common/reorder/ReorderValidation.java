package metro.ExoticStamp.common.reorder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ReorderValidation {

    private ReorderValidation() {
    }

    public static List<UUID> requireOrderedIds(List<UUID> orderedIds) {
        if (orderedIds == null) {
            throw new InvalidReorderException("orderedIds is required");
        }
        for (UUID id : orderedIds) {
            if (id == null) {
                throw new InvalidReorderException("orderedIds must not contain null");
            }
        }
        Set<UUID> unique = new HashSet<>();
        for (UUID id : orderedIds) {
            if (!unique.add(id)) {
                throw new InvalidReorderException("orderedIds must not contain duplicates");
            }
        }
        return orderedIds;
    }

    /**
     * Ensures the request is an exact permutation of the current scope membership.
     * Mismatched sets indicate a stale client (concurrent create/delete) → conflict.
     */
    public static void requireExactScope(List<UUID> orderedIds, Set<UUID> scopeIds, String scopeLabel) {
        if (orderedIds.size() != scopeIds.size() || !scopeIds.containsAll(orderedIds)) {
            throw new ReorderConflictException(
                    "orderedIds must match the current set of " + scopeLabel
                            + " (size expected " + scopeIds.size() + ", got " + orderedIds.size() + ")");
        }
    }
}
