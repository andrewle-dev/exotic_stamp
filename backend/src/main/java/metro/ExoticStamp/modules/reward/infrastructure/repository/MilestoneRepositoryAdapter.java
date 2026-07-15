package metro.ExoticStamp.modules.reward.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import metro.ExoticStamp.modules.reward.domain.model.MilestoneStatus;
import metro.ExoticStamp.modules.reward.domain.model.PagedSlice;
import metro.ExoticStamp.modules.reward.domain.repository.MilestoneRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MilestoneRepositoryAdapter implements MilestoneRepository {

    private final JpaMilestoneRepository jpaMilestoneRepository;

    @Override
    public Optional<Milestone> findById(UUID id) {
        return jpaMilestoneRepository.findById(id);
    }

    @Override
    public Optional<Milestone> findByIdNotDeleted(UUID id) {
        return jpaMilestoneRepository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public Milestone save(Milestone milestone) {
        return jpaMilestoneRepository.save(milestone);
    }

    @Override
    public List<Milestone> findActiveByCampaignId(UUID campaignId) {
        return jpaMilestoneRepository.findActiveByCampaignId(campaignId);
    }

    @Override
    public List<Milestone> findActiveApplicableToLineAndCampaign(UUID lineId, UUID campaignId) {
        return jpaMilestoneRepository.findActiveApplicableToLineAndCampaign(lineId, campaignId);
    }

    @Override
    public PagedSlice<Milestone> findAllNotDeletedPaged(UUID campaignId, MilestoneStatus status, int page, int size) {
        Page<Milestone> p = jpaMilestoneRepository.findAllNotDeleted(
                campaignId, status, PageRequest.of(page, size));
        return new PagedSlice<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
    }

    @Override
    public List<Milestone> findAllByCampaignIdOrderBySortOrderAsc(UUID campaignId) {
        return jpaMilestoneRepository.findAllByCampaignIdOrderBySortOrderAsc(campaignId);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaMilestoneRepository.existsById(id);
    }

    @Override
    public boolean existsByCampaignIdAndCodeAndIdNot(UUID campaignId, String code, UUID excludeId) {
        return jpaMilestoneRepository.existsByCampaignIdAndCodeAndIdNotAndDeletedAtIsNull(
                campaignId, code, excludeId);
    }

    @Override
    @Deprecated
    public PagedSlice<Milestone> findAllPaged(Boolean activeOnly, int page, int size) {
        MilestoneStatus status = null;
        if (activeOnly != null) {
            status = activeOnly ? MilestoneStatus.ACTIVE : MilestoneStatus.INACTIVE;
        }
        return findAllNotDeletedPaged(null, status, page, size);
    }
}
