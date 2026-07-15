package metro.ExoticStamp.modules.reward.domain.repository;

import metro.ExoticStamp.modules.reward.domain.model.PagedSlice;
import metro.ExoticStamp.modules.reward.domain.model.Partner;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PartnerRepository {

    Optional<Partner> findById(UUID id);

    Partner save(Partner partner);

    PagedSlice<Partner> findAllPaged(Boolean activeOnly, int page, int size);

    /**
     * Active partners that have a non-null banner URL, newest updates first.
     * Contract-window filtering is applied in the query service.
     */
    List<Partner> findActiveWithBannerOrderedByUpdatedAtDesc();

    boolean existsById(UUID id);
}
