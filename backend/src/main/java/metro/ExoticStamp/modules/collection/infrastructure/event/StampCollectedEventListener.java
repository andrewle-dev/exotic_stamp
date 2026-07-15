package metro.ExoticStamp.modules.collection.infrastructure.event;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.collection.application.port.StampCollectedDedupPort;
import metro.ExoticStamp.modules.collection.domain.event.StampCollectedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Async hook after stamp collection commit. Failures here do not affect the collect transaction.
 * Downstream modules may add their own AFTER_COMMIT listeners for {@link StampCollectedEvent}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StampCollectedEventListener {

    private final StampCollectedDedupPort stampCollectedDedupPort;
    private final MeterRegistry meterRegistry;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStampCollected(StampCollectedEvent event) {
        try {
            if (!stampCollectedDedupPort.claimFirstProcessing(event.getEventId())) {
                log.debug("[Collection] duplicate StampCollectedEvent skipped eventId={} userId={} stationId={}",
                        event.getEventId(), event.getUserId(), event.getStationId());
                return;
            }
            log.info("[Collection] StampCollectedEvent eventId={} userId={} stationId={} lineId={} campaignId={} collectedAt={} collectMethod={}",
                    event.getEventId(),
                    event.getUserId(),
                    event.getStationId(),
                    event.getLineId(),
                    event.getCampaignId(),
                    event.getCollectedAt(),
                    event.getCollectMethod());
            meterRegistry.counter("collection.stamp_collected").increment();
        } catch (Exception e) {
            log.error("[Collection] StampCollectedEvent handling failed eventId={} userId={} stationId={}: {}",
                    event.getEventId(), event.getUserId(), event.getStationId(), e.getMessage(), e);
        }
    }
}
