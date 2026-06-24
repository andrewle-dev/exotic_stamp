package metro.ExoticStamp.modules.reward.application.port;

import java.util.UUID;

public interface RewardStampCollectedDedupPort {

    boolean isProcessed(UUID eventId);

    boolean acquireProcessingLock(UUID eventId);

    void markProcessed(UUID eventId);

    void releaseProcessingLock(UUID eventId);
}
