package metro.ExoticStamp.modules.community.infrastructure.repository;

import metro.ExoticStamp.modules.community.domain.model.Referral;
import metro.ExoticStamp.modules.community.domain.model.ReferralStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaReferralRepository extends JpaRepository<Referral, UUID> {

    Optional<Referral> findByReferredUserId(UUID referredUserId);

    List<Referral> findByReferrerUserIdOrderByReferredAtDesc(UUID referrerUserId);

    long countByReferrerUserIdAndStatus(UUID referrerUserId, ReferralStatus status);

    Page<Referral> findByReferrerUserIdOrderByReferredAtDesc(UUID referrerUserId, Pageable pageable);
}
