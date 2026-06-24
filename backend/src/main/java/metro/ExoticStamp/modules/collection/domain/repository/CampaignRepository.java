package metro.ExoticStamp.modules.collection.domain.repository;

import metro.ExoticStamp.common.model.PageResult;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.CampaignStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampaignRepository {

    Optional<Campaign> findById(UUID id);

    Optional<Campaign> findByIdNotDeleted(UUID id);

    Campaign save(Campaign campaign);

    boolean existsDefaultByLineId(UUID lineId);

    Optional<Campaign> findDefaultByLineId(UUID lineId);

    List<Campaign> findAllActiveDefaults();

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, UUID id);

    PageResult<Campaign> findAllPaged(int page, int size);

    PageResult<Campaign> findAllNotDeletedPaged(int page, int size);

    List<Campaign> findByLineIdOrderByCodeAsc(UUID lineId);

    List<Campaign> findActiveInWindow(LocalDateTime now);

    List<Campaign> findActiveByStationId(UUID stationId, LocalDateTime now);
}
