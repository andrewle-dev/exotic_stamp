package metro.ExoticStamp.modules.reward.application.port;

/**
 * Database-backed exclusive run lock for reconciliation (multi-instance safe).
 * Implementations must not rely solely on Redis.
 */
public interface RewardReconcileLockPort {

    /** @return true if this instance holds the lock */
    boolean tryAcquire();

    void release();
}
