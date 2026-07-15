package metro.ExoticStamp.modules.collection.domain.policy;

import metro.ExoticStamp.modules.collection.domain.exception.StampAlreadyCollectedException;
import metro.ExoticStamp.modules.collection.domain.exception.IdempotencyKeyConflictException;

import java.util.UUID;

/**
 * Cross-aggregate anti-cheat / uniqueness decisions.
 * Application loads existence / stamp ownership; this policy decides.
 * DB unique constraint {@code uq_user_stamps_collect} remains the concurrency backstop.
 */
public final class CollectionDuplicatePolicy {

    private CollectionDuplicatePolicy() {
    }

    public static void assertNotAlreadyCollected(boolean alreadyExists, UUID stationId) {
        if (alreadyExists) {
            throw new StampAlreadyCollectedException(stationId);
        }
    }

    /**
     * Idempotent replay must belong to the same user as the requesting collect.
     */
    public static void assertReplayBelongsToUser(UUID stampOwnerUserId, UUID requestingUserId) {
        if (stampOwnerUserId == null || requestingUserId == null
                || !requestingUserId.equals(stampOwnerUserId)) {
            throw new IdempotencyKeyConflictException();
        }
    }
}
