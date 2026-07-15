package metro.ExoticStamp.modules.rbac.application.support;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Registers a callback to run after the current transaction commits.
 * <p>
 * When transaction synchronization is active (normal {@code @Transactional} path),
 * the runnable runs in {@link TransactionSynchronization#afterCommit()} — safe for
 * publishing domain events that listeners must only see after durable commit.
 * <p>
 * When synchronization is <strong>not</strong> active (unit tests without a TX, or
 * misconfigured call sites), the runnable runs <strong>immediately</strong>. Prefer
 * keeping production publish sites inside an active transaction.
 */
public final class RbacTransactionCallbacks {

    private RbacTransactionCallbacks() {}

    public static void afterCommit(Runnable runnable) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    runnable.run();
                }
            });
        } else {
            runnable.run();
        }
    }
}
