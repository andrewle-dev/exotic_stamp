package metro.ExoticStamp.modules.community.domain.repository;

import metro.ExoticStamp.modules.community.domain.model.PagedSlice;
import metro.ExoticStamp.modules.community.domain.model.ShareEvent;

import java.util.UUID;

public interface ShareEventRepository {

    ShareEvent save(ShareEvent shareEvent);

    PagedSlice<ShareEvent> findByUserIdOrderBySharedAtDesc(UUID userId, int page, int size);
}
