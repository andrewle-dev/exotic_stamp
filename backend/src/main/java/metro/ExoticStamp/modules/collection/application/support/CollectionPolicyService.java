package metro.ExoticStamp.modules.collection.application.support;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.config.CollectionProperties;
import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import metro.ExoticStamp.modules.collection.domain.policy.CollectionDuplicatePolicy;
import metro.ExoticStamp.modules.collection.domain.repository.UserStampRepository;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Application orchestration for collection anti-cheat lookups.
 * Domain decisions live in {@link CollectionDuplicatePolicy}.
 */
@Component
@RequiredArgsConstructor
public class CollectionPolicyService {

    private final UserStampRepository userStampRepository;
    private final CollectionProperties collectionProperties;
    private final Clock clock;
    private final CollectionRuntimeAuditHelper auditHelper;

    public Optional<UserStamp> resolveIdempotentReplay(String idempotencyKey, UUID userId) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }
        Duration window = collectionProperties.getIdempotencyWindow();
        LocalDateTime since = LocalDateTime.now(clock).minus(window);
        Optional<UserStamp> existing = userStampRepository
                .findFirstByIdempotencyKeyAndCollectedAtAfterOrderByCollectedAtDesc(idempotencyKey, since);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        UserStamp stamp = existing.get();
        CollectionDuplicatePolicy.assertReplayBelongsToUser(stamp.getUserId(), userId);
        return Optional.of(stamp);
    }

    public void assertCollectAllowed(UUID userId, UUID stationId, UUID campaignId) {
        boolean exists = userStampRepository.existsByUserIdAndStationIdAndCampaignId(userId, stationId, campaignId);
        if (exists) {
            auditHelper.scheduleDuplicateAttempt(userId, stationId, campaignId);
        }
        CollectionDuplicatePolicy.assertNotAlreadyCollected(exists, stationId);
    }
}
