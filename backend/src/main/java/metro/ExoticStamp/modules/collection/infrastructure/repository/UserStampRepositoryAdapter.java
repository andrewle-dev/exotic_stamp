package metro.ExoticStamp.modules.collection.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import metro.ExoticStamp.modules.collection.domain.model.UserStampSlice;
import metro.ExoticStamp.modules.collection.domain.repository.UserStampRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserStampRepositoryAdapter implements UserStampRepository {

    private final JpaUserStampRepository jpaUserStampRepository;

    @Override
    public Optional<UserStamp> findById(UUID id) {
        return jpaUserStampRepository.findById(id);
    }

    @Override
    public Optional<UserStamp> findFirstByIdempotencyKeyAndCollectedAtAfterOrderByCollectedAtDesc(
            String idempotencyKey, LocalDateTime since) {
        return jpaUserStampRepository.findFirstByIdempotencyKeyAndCollectedAtAfterOrderByCollectedAtDesc(idempotencyKey, since);
    }

    @Override
    public Optional<UserStamp> findFirstByUserIdAndIdempotencyKeyOrderByCollectedAtDesc(
            UUID userId, String idempotencyKey) {
        return jpaUserStampRepository.findFirstByUserIdAndIdempotencyKeyOrderByCollectedAtDesc(userId, idempotencyKey);
    }

    @Override
    public boolean existsByUserIdAndStationIdAndCampaignId(UUID userId, UUID stationId, UUID campaignId) {
        return jpaUserStampRepository.existsByUserIdAndStationIdAndCampaignId(userId, stationId, campaignId);
    }

    @Override
    public UserStamp save(UserStamp userStamp) {
        return jpaUserStampRepository.save(userStamp);
    }

    @Override
    public List<UserStamp> findRecentByUserId(UUID userId, int limit) {
        return jpaUserStampRepository.findByUserIdOrderByCollectedAtDesc(userId, PageRequest.of(0, limit)).getContent();
    }

    @Override
    public UserStampSlice findByUserIdAndCampaignIdPaged(UUID userId, UUID campaignId, int page, int size) {
        Page<UserStamp> p = jpaUserStampRepository.findByUserIdAndCampaignIdOrderByCollectedAtDesc(
                userId, campaignId, PageRequest.of(page, size));
        return toSlice(p);
    }

    @Override
    public UserStampSlice findByUserIdPaged(UUID userId, int page, int size) {
        Page<UserStamp> p = jpaUserStampRepository.findByUserIdOrderByCollectedAtDesc(userId, PageRequest.of(page, size));
        return toSlice(p);
    }

    private static UserStampSlice toSlice(Page<UserStamp> p) {
        return new UserStampSlice(
                p.getContent(),
                p.getTotalElements(),
                p.getTotalPages(),
                p.getNumber(),
                p.getSize()
        );
    }

    @Override
    public List<UserStamp> findByUserIdAndCampaignId(UUID userId, UUID campaignId) {
        return jpaUserStampRepository.findByUserIdAndCampaignIdOrderByCollectedAtDesc(userId, campaignId);
    }

    @Override
    public long countDistinctStationsByUserIdAndCampaignId(UUID userId, UUID campaignId) {
        return jpaUserStampRepository.countDistinctStationsByUserIdAndCampaignId(userId, campaignId);
    }

    @Override
    public long countAll() {
        return jpaUserStampRepository.count();
    }

    @Override
    public Map<UUID, Long> countStampsByCampaignId() {
        List<Object[]> rows = jpaUserStampRepository.countGroupedByCampaignId();
        Map<UUID, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            UUID campaignId = (UUID) row[0];
            long cnt = ((Number) row[1]).longValue();
            if (campaignId != null) {
                map.put(campaignId, cnt);
            }
        }
        return map;
    }
}

