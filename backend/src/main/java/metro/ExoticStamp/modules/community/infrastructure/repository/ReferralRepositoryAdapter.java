package metro.ExoticStamp.modules.community.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.community.domain.model.PagedSlice;
import metro.ExoticStamp.modules.community.domain.model.Referral;
import metro.ExoticStamp.modules.community.domain.model.ReferralStatus;
import metro.ExoticStamp.modules.community.domain.repository.ReferralRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReferralRepositoryAdapter implements ReferralRepository {

    private final JpaReferralRepository jpaReferralRepository;

    @Override
    public Referral save(Referral referral) {
        return jpaReferralRepository.save(referral);
    }

    @Override
    public Optional<Referral> findById(UUID id) {
        return jpaReferralRepository.findById(id);
    }

    @Override
    public Optional<Referral> findByReferredUserId(UUID referredUserId) {
        return jpaReferralRepository.findByReferredUserId(referredUserId);
    }

    @Override
    public List<Referral> findByReferrerUserId(UUID referrerUserId) {
        return jpaReferralRepository.findByReferrerUserIdOrderByReferredAtDesc(referrerUserId);
    }

    @Override
    public long countByReferrerUserIdAndStatus(UUID referrerUserId, ReferralStatus status) {
        return jpaReferralRepository.countByReferrerUserIdAndStatus(referrerUserId, status);
    }

    @Override
    public PagedSlice<Referral> findByReferrerUserIdPaged(UUID referrerUserId, int page, int size) {
        Page<Referral> p = jpaReferralRepository.findByReferrerUserIdOrderByReferredAtDesc(
                referrerUserId, PageRequest.of(page, size));
        return new PagedSlice<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
    }
}
