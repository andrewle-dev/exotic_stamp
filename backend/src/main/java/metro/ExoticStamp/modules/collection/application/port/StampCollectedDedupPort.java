package metro.ExoticStamp.modules.collection.application.port;

import java.util.UUID;

/**
 * Idempotent processing marker for {@link metro.ExoticStamp.modules.collection.domain.event.StampCollectedEvent}.
 */
public interface StampCollectedDedupPort {

    /**
     * @return true if this event should be processed (first time); false if duplicate.
     */
    boolean claimFirstProcessing(UUID eventId);
}
