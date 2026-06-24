package metro.ExoticStamp.modules.community.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.community.application.mapper.CommunityAppMapper;
import metro.ExoticStamp.modules.community.application.view.ShareEventView;
import metro.ExoticStamp.modules.community.config.CommunityProperties;
import metro.ExoticStamp.modules.community.domain.model.PagedSlice;
import metro.ExoticStamp.modules.community.domain.model.ShareEvent;
import metro.ExoticStamp.modules.community.domain.repository.ShareEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShareEventQueryService {

    private final ShareEventRepository shareEventRepository;
    private final CommunityAppMapper communityAppMapper;
    private final CommunityProperties communityProperties;

    public PageResponse<ShareEventView> listMyShares(UUID userId, int page, int size) {
        int p = Math.max(0, page);
        int s = normalizeSize(size);
        PagedSlice<ShareEvent> slice = shareEventRepository.findByUserIdOrderBySharedAtDesc(userId, p, s);
        List<ShareEventView> content = slice.content().stream()
                .map(communityAppMapper::toShareEventView)
                .collect(Collectors.toList());
        return PageResponse.of(content, slice.totalElements(), slice.totalPages(), slice.page(), slice.size());
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return communityProperties.getDefaultPageSize();
        }
        return Math.min(size, communityProperties.getMaxPageSize());
    }
}
