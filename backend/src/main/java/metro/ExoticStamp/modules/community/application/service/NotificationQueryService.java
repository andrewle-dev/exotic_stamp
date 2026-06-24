package metro.ExoticStamp.modules.community.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.community.application.mapper.CommunityAppMapper;
import metro.ExoticStamp.modules.community.application.view.NotificationView;
import metro.ExoticStamp.modules.community.config.CommunityProperties;
import metro.ExoticStamp.modules.community.domain.model.Notification;
import metro.ExoticStamp.modules.community.domain.model.PagedSlice;
import metro.ExoticStamp.modules.community.domain.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;
    private final CommunityAppMapper communityAppMapper;
    private final CommunityProperties communityProperties;

    public PageResponse<NotificationView> listMyNotifications(UUID userId, Boolean unreadOnly, int page, int size) {
        int p = Math.max(0, page);
        int s = normalizeSize(size);
        PagedSlice<Notification> slice = notificationRepository.findByUserId(userId, unreadOnly, p, s);
        List<NotificationView> content = slice.content().stream()
                .map(communityAppMapper::toNotificationView)
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
