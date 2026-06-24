package metro.ExoticStamp.modules.collection.application.view;

/**
 * Read-only outcome for {@code GET /api/v1/collection/collect/status}.
 */
public enum CollectOutcomeStatus {
    /** Collect completed; stamp persisted for this idempotency key. */
    SUCCESS,
    /** Idempotent replay within the idempotency window (safe retry). */
    DUPLICATE,
    /** Collect attempt failed (recorded outcome when available). */
    FAILED,
    /** No outcome for this user + idempotency key. */
    NOT_FOUND,
    /** Outcome not yet persisted (in-flight collect). */
    PENDING
}
