package metro.ExoticStamp.modules.reward.infrastructure.event;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.collection.domain.event.StampCollectedEvent;
import metro.ExoticStamp.modules.reward.application.port.RewardStampCollectedDedupPort;
import metro.ExoticStamp.modules.reward.application.service.RewardEvaluationService;
import metro.ExoticStamp.modules.reward.config.RewardProperties;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Slf4j
@Component("rewardStampCollectedEventListener")
@RequiredArgsConstructor
public class StampCollectedEventListener {

    private final RewardStampCollectedDedupPort rewardStampCollectedDedupPort;
    private final RewardEvaluationService rewardEvaluationService;
    private final MeterRegistry meterRegistry;
    private final RewardProperties rewardProperties;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStampCollected(StampCollectedEvent event) {
        UUID eventId = event.getEventId();
        try {
            if (rewardStampCollectedDedupPort.isProcessed(eventId)) {
                log.debug("[Reward] duplicate StampCollectedEvent skipped eventId={} userId={}",
                        eventId, event.getUserId());
                return;
            }
            if (!rewardStampCollectedDedupPort.acquireProcessingLock(eventId)) {
                log.debug("[Reward] processing in progress, skip eventId={} userId={}", eventId, event.getUserId());
                return;
            }
            try {
                processWithRetry(event);
            } finally {
                rewardStampCollectedDedupPort.releaseProcessingLock(eventId);
            }
        } catch (Exception e) {
            log.error("[Reward] StampCollectedEvent handling failed eventId={} userId={}: {}",
                    eventId, event.getUserId(), e.getMessage(), e);
            // Stamp already committed; no outbox redelivery. Ops: alert on this counter and re-eval manually.
            meterRegistry.counter("reward.stamp_collected.orphan", "reason", "listener_exhausted").increment();
        }
    }

    private void processWithRetry(StampCollectedEvent event) {
        int maxAttempts = Math.max(1, rewardProperties.getStampCollectedEventMaxAttempts());
        long backoffMillis = Math.max(0L, rewardProperties.getStampCollectedEventRetryBackoff().toMillis());
        UUID lineId = event.getLineId();
        String lineTag = lineId != null ? lineId.toString() : "unknown";

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                meterRegistry.counter("reward.milestone.checked", "lineId", lineTag).increment();
                rewardEvaluationService.handleStampCollected(event.getUserId(), event.getCampaignId());
                rewardStampCollectedDedupPort.markProcessed(event.getEventId());
                return;
            } catch (Exception ex) {
                if (attempt >= maxAttempts) {
                    throw ex;
                }
                log.warn("[Reward] attempt {}/{} failed eventId={} userId={}: {}",
                        attempt, maxAttempts, event.getEventId(), event.getUserId(), ex.getMessage());
                sleepQuietly(backoffMillis);
            }
        }
    }

    private static void sleepQuietly(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
