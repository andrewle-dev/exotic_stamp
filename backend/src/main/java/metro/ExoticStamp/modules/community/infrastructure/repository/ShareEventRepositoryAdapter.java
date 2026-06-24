package metro.ExoticStamp.modules.community.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.community.domain.model.PagedSlice;
import metro.ExoticStamp.modules.community.domain.model.ShareEvent;
import metro.ExoticStamp.modules.community.domain.repository.ShareEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ShareEventRepositoryAdapter implements ShareEventRepository {

    private final JpaShareEventRepository jpaShareEventRepository;

    @Override
    public ShareEvent save(ShareEvent shareEvent) {
        return jpaShareEventRepository.save(shareEvent);
    }

    @Override
    public PagedSlice<ShareEvent> findByUserIdOrderBySharedAtDesc(UUID userId, int page, int size) {
        Page<ShareEvent> p = jpaShareEventRepository.findByUserIdOrderBySharedAtDesc(
                userId, PageRequest.of(page, size));
        return new PagedSlice<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
    }
}
