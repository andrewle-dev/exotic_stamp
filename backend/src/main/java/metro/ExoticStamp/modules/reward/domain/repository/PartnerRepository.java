package metro.ExoticStamp.modules.reward.domain.repository;

import metro.ExoticStamp.modules.reward.domain.model.PagedSlice;
import metro.ExoticStamp.modules.reward.domain.model.Partner;

import java.util.Optional;
import java.util.UUID;

public interface PartnerRepository {

    Optional<Partner> findById(UUID id);

    Partner save(Partner partner);

    PagedSlice<Partner> findAllPaged(Boolean activeOnly, int page, int size);

    boolean existsById(UUID id);
}
