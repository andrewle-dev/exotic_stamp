package metro.ExoticStamp.modules.collection.infrastructure.event;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.collection.application.port.StampCollectedDedupPort;
import metro.ExoticStamp.modules.collection.domain.event.StampCollectedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Async hook after stamp collection. Failures here do not affect the collect transaction.
 * Downstream modules may add their own {@code @EventListener} for {@link StampCollectedEvent} when integrations exist.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StampCollectedEventListener {

    private final StampCollectedDedupPort stampCollectedDedupPort;
    private final MeterRegistry meterRegistry;

    @Async
    @EventListener
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
