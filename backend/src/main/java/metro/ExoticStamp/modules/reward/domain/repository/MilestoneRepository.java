package metro.ExoticStamp.modules.reward.domain.repository;

import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import metro.ExoticStamp.modules.reward.domain.model.MilestoneStatus;
import metro.ExoticStamp.modules.reward.domain.model.PagedSlice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MilestoneRepository {

    Optional<Milestone> findById(UUID id);

    Optional<Milestone> findByIdNotDeleted(UUID id);

    Milestone save(Milestone milestone);

    List<Milestone> findActiveByCampaignId(UUID campaignId);

    /** @deprecated Use {@link #findActiveByCampaignId(UUID)}. */
    @Deprecated
    List<Milestone> findActiveApplicableToLineAndCampaign(UUID lineId, UUID campaignId);

    PagedSlice<Milestone> findAllNotDeletedPaged(UUID campaignId, MilestoneStatus status, int page, int size);

    /** All non-deleted milestones for a campaign, ordered by sortOrder then stampsRequired. */
    List<Milestone> findAllByCampaignIdOrderBySortOrderAsc(UUID campaignId);

    boolean existsById(UUID id);

    boolean existsByCampaignIdAndCodeAndIdNot(UUID campaignId, String code, UUID excludeId);

    /** @deprecated Legacy admin list; use {@link #findAllNotDeletedPaged(UUID, MilestoneStatus, int, int)}. */
    @Deprecated
    PagedSlice<Milestone> findAllPaged(Boolean activeOnly, int page, int size);
}
