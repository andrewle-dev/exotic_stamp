package metro.ExoticStamp.modules.community.infrastructure.repository;

import metro.ExoticStamp.modules.community.domain.model.ReferralCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaReferralCodeRepository extends JpaRepository<ReferralCode, UUID> {

    Optional<ReferralCode> findByUserId(UUID userId);

    Optional<ReferralCode> findByCode(String code);

    boolean existsByCode(String code);
}
