package metro.ExoticStamp.modules.community.domain.repository;

import metro.ExoticStamp.modules.community.domain.model.PagedSlice;
import metro.ExoticStamp.modules.community.domain.model.Referral;
import metro.ExoticStamp.modules.community.domain.model.ReferralStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReferralRepository {

    Referral save(Referral referral);

    Optional<Referral> findById(UUID id);

    Optional<Referral> findByReferredUserId(UUID referredUserId);

    List<Referral> findByReferrerUserId(UUID referrerUserId);

    long countByReferrerUserIdAndStatus(UUID referrerUserId, ReferralStatus status);

    PagedSlice<Referral> findByReferrerUserIdPaged(UUID referrerUserId, int page, int size);
}
