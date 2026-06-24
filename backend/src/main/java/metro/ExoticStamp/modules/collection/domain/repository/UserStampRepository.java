package metro.ExoticStamp.modules.collection.domain.repository;

import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import metro.ExoticStamp.modules.collection.domain.model.UserStampSlice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface UserStampRepository {

    Optional<UserStamp> findById(UUID id);

    /**
     * Latest stamp with this idempotency key where {@code collectedAt} is after {@code since} (1h window query).
     */
    Optional<UserStamp> findFirstByIdempotencyKeyAndCollectedAtAfterOrderByCollectedAtDesc(
            String idempotencyKey, LocalDateTime since);

    /**
     * Latest stamp for the authenticated user and idempotency key (read-only status lookup).
     */
    Optional<UserStamp> findFirstByUserIdAndIdempotencyKeyOrderByCollectedAtDesc(
            UUID userId, String idempotencyKey);

    boolean existsByUserIdAndStationIdAndCampaignId(UUID userId, UUID stationId, UUID campaignId);

    UserStamp save(UserStamp userStamp);

    List<UserStamp> findRecentByUserId(UUID userId, int limit);

    List<UserStamp> findByUserIdAndCampaignId(UUID userId, UUID campaignId);

    UserStampSlice findByUserIdAndCampaignIdPaged(UUID userId, UUID campaignId, int page, int size);

    UserStampSlice findByUserIdPaged(UUID userId, int page, int size);

    long countDistinctStationsByUserIdAndCampaignId(UUID userId, UUID campaignId);

    long countAll();

    /**
     * Stamp counts per campaign id (rows with null campaign id are included when present).
     */
    Map<UUID, Long> countStampsByCampaignId();
}

