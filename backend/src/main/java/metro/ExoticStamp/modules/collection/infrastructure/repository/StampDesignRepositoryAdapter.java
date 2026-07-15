package metro.ExoticStamp.modules.collection.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.model.PageResult;
import metro.ExoticStamp.modules.collection.domain.exception.DuplicateActiveStampDesignException;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StampDesignRepositoryAdapter implements StampDesignRepository {

    private final JpaStampDesignRepository jpaStampDesignRepository;

    @Override
    public Optional<StampDesign> findById(UUID id) {
        return jpaStampDesignRepository.findById(id);
    }

    @Override
    public Optional<StampDesign> findByIdNotDeleted(UUID id) {
        return jpaStampDesignRepository.findById(id).filter(sd -> !sd.isDeleted());
    }

    @Override
    public List<StampDesign> findAllByIdIn(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return jpaStampDesignRepository.findAllById(ids);
    }

    @Override
    public boolean existsActiveByCampaignIdAndStationId(UUID campaignId, UUID stationId) {
        return jpaStampDesignRepository.existsActiveByCampaignIdAndStationId(campaignId, stationId);
    }

    @Override
    public boolean existsActiveByCampaignIdAndStationIdAndIdNot(UUID campaignId, UUID stationId, UUID excludeId) {
        return jpaStampDesignRepository.existsActiveByCampaignIdAndStationIdAndIdNot(campaignId, stationId, excludeId);
    }

    @Override
    public Optional<StampDesign> findActiveByCampaignIdAndStationId(UUID campaignId, UUID stationId) {
        return jpaStampDesignRepository.findActiveByCampaignIdAndStationId(campaignId, stationId);
    }

    @Override
    public List<StampDesign> findActiveByCampaignIdAndStationIdIn(UUID campaignId, Collection<UUID> stationIds) {
        if (stationIds == null || stationIds.isEmpty()) {
            return List.of();
        }
        return jpaStampDesignRepository.findActiveByCampaignIdAndStationIdIn(campaignId, stationIds);
    }

    @Override
    public long countActiveByCampaignId(UUID campaignId) {
        return jpaStampDesignRepository.countActiveByCampaignId(campaignId);
    }

    @Override
    public StampDesign save(StampDesign stampDesign) {
        try {
            return jpaStampDesignRepository.save(stampDesign);
        } catch (DataIntegrityViolationException ex) {
            if (isActiveDesignViolation(ex)) {
                throw new DuplicateActiveStampDesignException();
            }
            throw ex;
        }
    }

    @Override
    public List<StampDesign> findByCampaignIdOrderBySortOrderAsc(UUID campaignId) {
        return jpaStampDesignRepository.findByCampaignIdOrderBySortOrderAsc(campaignId);
    }

    @Override
    public PageResult<StampDesign> findAllNotDeletedPaged(int page, int size) {
        Page<StampDesign> p = jpaStampDesignRepository.findAllNotDeleted(PageRequest.of(page, size));
        return PageResult.of(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber());
    }

    private static boolean isActiveDesignViolation(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        return msg != null && msg.contains("uq_stamp_design_active_per_campaign_station");
    }
}
