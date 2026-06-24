package metro.ExoticStamp.modules.community.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.community.domain.model.ReferralCode;
import metro.ExoticStamp.modules.community.domain.repository.ReferralCodeRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReferralCodeRepositoryAdapter implements ReferralCodeRepository {

    private final JpaReferralCodeRepository jpaReferralCodeRepository;

    @Override
    public ReferralCode save(ReferralCode referralCode) {
        return jpaReferralCodeRepository.save(referralCode);
    }

    @Override
    public Optional<ReferralCode> findById(UUID id) {
        return jpaReferralCodeRepository.findById(id);
    }

    @Override
    public Optional<ReferralCode> findByUserId(UUID userId) {
        return jpaReferralCodeRepository.findByUserId(userId);
    }

    @Override
    public Optional<ReferralCode> findByCode(String code) {
        return jpaReferralCodeRepository.findByCode(code);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaReferralCodeRepository.existsByCode(code);
    }
}
