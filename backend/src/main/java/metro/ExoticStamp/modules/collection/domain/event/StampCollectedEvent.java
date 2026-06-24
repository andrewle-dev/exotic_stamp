package metro.ExoticStamp.modules.collection.domain.event;

import metro.ExoticStamp.modules.collection.domain.model.CollectMethod;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Published after a successful stamp collect (after transaction commit). Payload is immutable.
 */
public final class StampCollectedEvent extends ApplicationEvent {

    private final UUID eventId;
    private final UUID stampId;
    private final UUID userId;
    private final UUID stationId;
    private final UUID lineId;
    private final UUID campaignId;
    private final LocalDateTime collectedAt;
    private final CollectMethod collectMethod;

    public StampCollectedEvent(
            Object source,
            UUID eventId,
            UUID stampId,
            UUID userId,
            UUID stationId,
            UUID lineId,
            UUID campaignId,
            LocalDateTime collectedAt,
            CollectMethod collectMethod
    ) {
        super(source);
        this.eventId = eventId;
        this.stampId = stampId;
        this.userId = userId;
        this.stationId = stationId;
        this.lineId = lineId;
        this.campaignId = campaignId;
        this.collectedAt = collectedAt;
        this.collectMethod = collectMethod;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getStampId() {
        return stampId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getStationId() {
        return stationId;
    }

    public UUID getLineId() {
        return lineId;
    }

    public UUID getCampaignId() {
        return campaignId;
    }

    public LocalDateTime getCollectedAt() {
        return collectedAt;
    }

    public CollectMethod getCollectMethod() {
        return collectMethod;
    }
}
