package metro.ExoticStamp.modules.collection.domain.service;

import metro.ExoticStamp.modules.collection.application.support.CollectionPolicyService;
import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * @deprecated Prefer {@link CollectionPolicyService} directly. Kept for backward-compatible wiring in tests.
 */
@Service
@Deprecated
public class CollectionDomainService {

    private final CollectionPolicyService policyService;

    public CollectionDomainService(CollectionPolicyService policyService) {
        this.policyService = policyService;
    }

    public void assertNotAlreadyCollected(UUID userId, UUID stationId, UUID campaignId) {
        policyService.assertCollectAllowed(userId, stationId, campaignId);
    }

    public Optional<UserStamp> resolveIdempotentStamp(String idempotencyKey, UUID userId, LocalDateTime since) {
        return policyService.resolveIdempotentReplay(idempotencyKey, userId);
    }
}
