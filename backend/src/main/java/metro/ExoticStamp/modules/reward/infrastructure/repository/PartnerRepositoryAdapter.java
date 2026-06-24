package metro.ExoticStamp.modules.reward.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.reward.domain.model.PagedSlice;
import metro.ExoticStamp.modules.reward.domain.model.Partner;
import metro.ExoticStamp.modules.reward.domain.repository.PartnerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PartnerRepositoryAdapter implements PartnerRepository {

    private final JpaPartnerRepository jpaPartnerRepository;

    @Override
    public Optional<Partner> findById(UUID id) {
        return jpaPartnerRepository.findById(id);
    }

    @Override
    public Partner save(Partner partner) {
        return jpaPartnerRepository.save(partner);
    }

    @Override
    public PagedSlice<Partner> findAllPaged(Boolean activeOnly, int page, int size) {
        PageRequest pr = PageRequest.of(page, size);
        Page<Partner> p;
        if (activeOnly == null) {
            p = jpaPartnerRepository.findAll(pr);
        } else {
            p = jpaPartnerRepository.findByActive(activeOnly, pr);
        }
        return new PagedSlice<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaPartnerRepository.existsById(id);
    }
}
