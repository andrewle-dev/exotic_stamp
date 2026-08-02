package metro.ExoticStamp.modules.collection.application.support;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.domain.exception.IdempotencyConflictException;
import metro.ExoticStamp.modules.collection.domain.exception.IdempotencyKeyConflictException;
import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import metro.ExoticStamp.modules.collection.domain.policy.CollectionDuplicatePolicy;
import metro.ExoticStamp.modules.collection.domain.repository.UserStampRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Application orchestration for collection anti-cheat lookups.
 * Domain decisions live in {@link CollectionDuplicatePolicy}.
 *
 * <p><b>Idempotency (R-P1-03 / Batch E.1):</b>
 * Permanent DB unique {@code uq_user_stamps_user_idempotency (user_id, idempotency_key)} is the
 * concurrency backstop. Logical identity is {@link CollectIdempotencyFingerprint}.
 * Same key + same fingerprint → replay; same key + different fingerprint →
 * {@link IdempotencyConflictException}. Legacy null fingerprint rows compare station/campaign only
 * and never silently accept a different logical payload.
 */
@Component
@RequiredArgsConstructor
public class CollectionPolicyService {

    private final UserStampRepository userStampRepository;
    private final CollectionRuntimeAuditHelper auditHelper;

    /**
     * Resolve a prior collect for this user+key against the logical fingerprint.
     *
     * @return empty when no prior row; present when replay is allowed
     * @throws IdempotencyConflictException when key exists for a different logical operation
     * @throws IdempotencyKeyConflictException when key belongs to another user (should not happen
     *         under per-user unique; retained for defense-in-depth)
     */
    public Optional<UserStamp> resolveIdempotentReplay(
            String idempotencyKey,
            UUID userId,
            String fingerprint,
            UUID stationId,
            UUID campaignId) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }
        Optional<UserStamp> existing = userStampRepository
                .findFirstByUserIdAndIdempotencyKeyOrderByCollectedAtDesc(userId, idempotencyKey);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        UserStamp stamp = existing.get();
        CollectionDuplicatePolicy.assertReplayBelongsToUser(stamp.getUserId(), userId);
        assertLogicalMatch(stamp, fingerprint, stationId, campaignId);
        return Optional.of(stamp);
    }

    /**
     * Soft window lookup retained for status APIs; collect uses permanent per-user key + fingerprint.
     */
    public Optional<UserStamp> resolveIdempotentReplay(String idempotencyKey, UUID userId) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }
        return userStampRepository
                .findFirstByUserIdAndIdempotencyKeyOrderByCollectedAtDesc(userId, idempotencyKey)
                .map(stamp -> {
                    CollectionDuplicatePolicy.assertReplayBelongsToUser(stamp.getUserId(), userId);
                    return stamp;
                });
    }

    public void assertLogicalMatch(
            UserStamp stamp, String fingerprint, UUID stationId, UUID campaignId) {
        String storedFp = stamp.getIdempotencyFingerprint();
        if (storedFp != null && !storedFp.isBlank()) {
            if (!CollectIdempotencyFingerprint.matches(storedFp, fingerprint)) {
                throw new IdempotencyConflictException();
            }
            return;
        }
        // Legacy null fingerprint: allow only when station + campaign match stored row.
        if (!Objects.equals(stamp.getStationId(), stationId)
                || !Objects.equals(stamp.getCampaignId(), campaignId)) {
            throw new IdempotencyConflictException();
        }
    }

    public void assertCollectAllowed(UUID userId, UUID stationId, UUID campaignId) {
        boolean exists = userStampRepository.existsByUserIdAndStationIdAndCampaignId(userId, stationId, campaignId);
        if (exists) {
            auditHelper.scheduleDuplicateAttempt(userId, stationId, campaignId);
        }
        CollectionDuplicatePolicy.assertNotAlreadyCollected(exists, stationId);
    }
}
