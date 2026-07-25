package metro.ExoticStamp.infra.storage.asset;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.infra.storage.StorageMetrics;
import metro.ExoticStamp.infra.storage.StorageProperties;
import metro.ExoticStamp.infra.storage.StorageService;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Idempotent orphan / pending reconciliation. Bounded batch size and duration.
 * Never deletes ACTIVE referenced objects.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrphanCleanupJob {

    private final StoredAssetRepository storedAssetRepository;
    private final StorageService storageService;
    private final StorageProperties storageProperties;
    private final StorageMetrics storageMetrics;

    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Scheduled(cron = "${storage.cleanup.cron:0 15 3 * * *}")
    public void scheduledCleanup() {
        runCleanup(storageProperties.getCleanup().isDryRun());
    }

    public CleanupResult runCleanup(boolean dryRun) {
        if (shuttingDown.get()) {
            log.info("[OrphanCleanup] skipped — shutdown in progress");
            return CleanupResult.skipped("shutdown");
        }
        if (!running.compareAndSet(false, true)) {
            log.info("[OrphanCleanup] skipped — already running");
            return CleanupResult.skipped("already-running");
        }
        Instant deadline = Instant.now().plus(storageProperties.getCleanup().getMaxRunDuration());
        int batchSize = Math.max(1, storageProperties.getCleanup().getBatchSize());
        int deleted = 0;
        int failed = 0;
        int examined = 0;
        try {
            LocalDateTime now = LocalDateTime.now();
            List<StoredAsset> orphans = storedAssetRepository.findByStatusAndDeleteAfterBefore(
                    StoredAssetStatus.ORPHANED, now, PageRequest.of(0, batchSize));
            for (StoredAsset orphan : orphans) {
                if (shuttingDown.get() || Instant.now().isAfter(deadline)) {
                    break;
                }
                examined++;
                if (storedAssetRepository.existsByObjectKeyAndStatus(orphan.getObjectKey(), StoredAssetStatus.ACTIVE)) {
                    log.warn("[OrphanCleanup] refusing delete — ACTIVE reference exists keyPrefix={}",
                            prefix(orphan.getObjectKey()));
                    continue;
                }
                if (dryRun) {
                    log.info("[OrphanCleanup] dry-run would delete keyPrefix={}", prefix(orphan.getObjectKey()));
                    continue;
                }
                try {
                    storageService.delete(orphan.getObjectKey());
                    storedAssetRepository.delete(orphan);
                    storageMetrics.recordCleanupSuccess();
                    deleted++;
                } catch (Exception ex) {
                    storageMetrics.recordCleanupFailure();
                    failed++;
                    log.warn("[OrphanCleanup] delete failed keyPrefix={} type={}",
                            prefix(orphan.getObjectKey()), ex.getClass().getSimpleName());
                }
            }

            Duration pendingAge = storageProperties.getCleanup().getOrphanRetention();
            LocalDateTime pendingBefore = now.minus(pendingAge);
            List<StoredAsset> stalePending = storedAssetRepository.findStalePending(
                    pendingBefore, PageRequest.of(0, batchSize));
            for (StoredAsset pending : stalePending) {
                if (shuttingDown.get() || Instant.now().isAfter(deadline)) {
                    break;
                }
                examined++;
                pending.setStatus(StoredAssetStatus.ORPHANED);
                pending.setOrphanedAt(now);
                pending.setDeleteAfter(now.plus(Duration.ofDays(1)));
                storedAssetRepository.save(pending);
                storageMetrics.recordOrphanCreated();
            }
            return new CleanupResult(examined, deleted, failed, dryRun, false, null);
        } finally {
            running.set(false);
        }
    }

    @PreDestroy
    void onShutdown() {
        shuttingDown.set(true);
    }

    private static String prefix(String objectKey) {
        if (objectKey == null) {
            return "null";
        }
        int idx = objectKey.lastIndexOf('/');
        return idx > 0 ? objectKey.substring(0, idx) : objectKey;
    }

    public record CleanupResult(
            int examined,
            int deleted,
            int failed,
            boolean dryRun,
            boolean skipped,
            String skipReason
    ) {
        static CleanupResult skipped(String reason) {
            return new CleanupResult(0, 0, 0, false, true, reason);
        }
    }
}
