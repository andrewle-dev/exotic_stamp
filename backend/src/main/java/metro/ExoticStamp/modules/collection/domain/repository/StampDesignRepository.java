package metro.ExoticStamp.modules.collection.domain.repository;

import metro.ExoticStamp.common.model.PageResult;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.model.StampDesignStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StampDesignRepository {

    Optional<StampDesign> findById(UUID id);

    Optional<StampDesign> findByIdNotDeleted(UUID id);

    List<StampDesign> findAllByIdIn(Collection<UUID> ids);

    Optional<StampDesign> findActiveByCampaignIdAndStationId(UUID campaignId, UUID stationId);

    boolean existsActiveByCampaignIdAndStationId(UUID campaignId, UUID stationId);

    boolean existsActiveByCampaignIdAndStationIdAndIdNot(UUID campaignId, UUID stationId, UUID excludeId);

    List<StampDesign> findActiveByCampaignIdAndStationIdIn(UUID campaignId, Collection<UUID> stationIds);

    StampDesign save(StampDesign stampDesign);

    List<StampDesign> findByCampaignIdOrderBySortOrderAsc(UUID campaignId);

    PageResult<StampDesign> findAllNotDeletedPaged(int page, int size);
}
