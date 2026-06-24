package metro.ExoticStamp.modules.community.domain.repository;

import metro.ExoticStamp.modules.community.domain.model.Notification;
import metro.ExoticStamp.modules.community.domain.model.PagedSlice;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(UUID id);

    Optional<Notification> findByUserIdAndId(UUID userId, UUID id);

    PagedSlice<Notification> findByUserId(UUID userId, Boolean unreadOnly, int page, int size);

    int markAllReadByUserId(UUID userId);
}
