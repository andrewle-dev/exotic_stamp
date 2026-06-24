package metro.ExoticStamp.modules.community.domain.repository;

import metro.ExoticStamp.modules.community.domain.model.ReferralCode;

import java.util.Optional;
import java.util.UUID;

public interface ReferralCodeRepository {

    ReferralCode save(ReferralCode referralCode);

    Optional<ReferralCode> findById(UUID id);

    Optional<ReferralCode> findByUserId(UUID userId);

    Optional<ReferralCode> findByCode(String code);

    boolean existsByCode(String code);
}
