package metro.ExoticStamp.modules.reward.application.service;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.reward.application.port.RewardReconcileCandidatePort;
import metro.ExoticStamp.modules.reward.application.port.RewardReconcileLockPort;
import metro.ExoticStamp.modules.reward.config.RewardProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MVP Option B (R-P1-01): AFTER_COMMIT async listener + DB-locked reconcile for
 * (1) missing rewards and (2) PENDING_STOCK fulfillment. Idempotent via unique constraints.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RewardReconcileService {

    private final RewardReconcileCandidatePort candidatePort;
    private final RewardReconcileLockPort lockPort;
    private final RewardEvaluationService rewardEvaluationService;
    private final RewardProperties rewardProperties;
    private final MeterRegistry meterRegistry;

    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    @Scheduled(cron = "${reward.reconcile-cron:0 20 */1 * * *}")
    public void scheduledReconcile() {
        runReconcile(ReconcileRequest.scheduled());
    }

    public ReconcileResult runReconcile() {
        return runReconcile(ReconcileRequest.scheduled());
    }

    public ReconcileResult runReconcile(ReconcileRequest request) {
        if (shuttingDown.get()) {
            return ReconcileResult.skipped("shutdown");
        }
        int batchSize = clampBatch(request.batchSize());
        int maxBatches = Math.max(1, Math.min(request.maxBatches(), rewardProperties.getReconcileMaxBatches()));
        if (!lockPort.tryAcquire()) {
            meterRegistry.counter("reward.reconcile.busy").increment();
            return ReconcileResult.skipped("already-running");
        }
        Instant deadline = Instant.now().plus(rewardProperties.getReconcileMaxDuration());
        int missingExamined = 0;
        int missingRepaired = 0;
        int pendingExamined = 0;
        int pendingFulfilled = 0;
        int stillNoStock = 0;
        int failed = 0;
        try {
            if (request.dryRun()) {
                List<RewardReconcileCandidatePort.UserCampaignPair> missing =
                        candidatePort.findMissingRewardCandidates(rewardProperties.getReconcileLookback(), batchSize);
                List<UUID> pending = candidatePort.peekPendingStockRewardIds(
                        rewardProperties.getReconcileLookback(), batchSize);
                return new ReconcileResult(
                        missing.size(), 0, pending.size(), 0, 0, 0, true, "dry-run",
                        request.initiatedByAdminId());
            }
            for (int batch = 0; batch < maxBatches; batch++) {
                if (shuttingDown.get() || Instant.now().isAfter(deadline)) {
                    break;
                }
                List<RewardReconcileCandidatePort.UserCampaignPair> missing =
                        candidatePort.findMissingRewardCandidates(
                                rewardProperties.getReconcileLookback(), batchSize);
                for (var pair : missing) {
                    if (shuttingDown.get() || Instant.now().isAfter(deadline)) {
                        break;
                    }
                    missingExamined++;
                    try {
                        rewardEvaluationService.handleStampCollected(pair.userId(), pair.campaignId());
                        missingRepaired++;
                        meterRegistry.counter("reward.reconcile.missing_repaired").increment();
                    } catch (Exception ex) {
                        failed++;
                        meterRegistry.counter("reward.reconcile.failed").increment();
                        log.warn("[RewardReconcile] missing failed type={}", ex.getClass().getSimpleName());
                    }
                }
                if (shuttingDown.get() || Instant.now().isAfter(deadline)) {
                    break;
                }
                List<UUID> pendingIds = candidatePort.claimPendingStockRewardIds(
                        rewardProperties.getReconcileLookback(), batchSize);
                for (UUID rewardId : pendingIds) {
                    if (shuttingDown.get() || Instant.now().isAfter(deadline)) {
                        break;
                    }
                    pendingExamined++;
                    try {
                        var result = rewardEvaluationService.fulfillPendingStock(rewardId);
                        switch (result) {
                            case FULFILLED -> {
                                pendingFulfilled++;
                                meterRegistry.counter("reward.reconcile.pending_fulfilled").increment();
                            }
                            case STILL_NO_STOCK -> {
                                stillNoStock++;
                                meterRegistry.counter("reward.reconcile.still_no_stock").increment();
                            }
                            case SKIPPED -> meterRegistry.counter("reward.reconcile.pending_skipped").increment();
                        }
                    } catch (Exception ex) {
                        failed++;
                        meterRegistry.counter("reward.reconcile.failed").increment();
                        log.warn("[RewardReconcile] pending failed type={}", ex.getClass().getSimpleName());
                    }
                }
                if (missing.isEmpty() && pendingIds.isEmpty()) {
                    break;
                }
            }
            if (request.initiatedByAdminId() != null) {
                log.info("[RewardReconcile] adminRun adminId={} missingRepaired={} pendingFulfilled={} failed={}",
                        request.initiatedByAdminId(), missingRepaired, pendingFulfilled, failed);
            }
            return new ReconcileResult(
                    missingExamined, missingRepaired, pendingExamined, pendingFulfilled,
                    stillNoStock, failed, false, null, request.initiatedByAdminId());
        } finally {
            lockPort.release();
        }
    }

    private int clampBatch(Integer requested) {
        int configured = Math.max(1, rewardProperties.getReconcileBatchSize());
        int max = Math.max(1, rewardProperties.getReconcileMaxBatchSize());
        if (requested == null || requested <= 0) {
            return Math.min(configured, max);
        }
        return Math.min(requested, max);
    }

    @PreDestroy
    void onShutdown() {
        shuttingDown.set(true);
    }

    public record ReconcileRequest(
            Integer batchSize,
            int maxBatches,
            boolean dryRun,
            UUID initiatedByAdminId
    ) {
        public static ReconcileRequest scheduled() {
            return new ReconcileRequest(null, 3, false, null);
        }

        public static ReconcileRequest admin(Integer batchSize, boolean dryRun, UUID adminId) {
            return new ReconcileRequest(batchSize, 3, dryRun, adminId);
        }
    }

    public record ReconcileResult(
            int missingExamined,
            int missingRepaired,
            int pendingExamined,
            int pendingFulfilled,
            int stillNoStock,
            int failed,
            boolean skipped,
            String skipReason,
            UUID initiatedByAdminId
    ) {
        static ReconcileResult skipped(String reason) {
            return new ReconcileResult(0, 0, 0, 0, 0, 0, true, reason, null);
        }

        /** Backward-compatible counts for older response mapping. */
        public int examined() {
            return missingExamined + pendingExamined;
        }

        public int evaluated() {
            return missingRepaired + pendingFulfilled;
        }
    }
}
