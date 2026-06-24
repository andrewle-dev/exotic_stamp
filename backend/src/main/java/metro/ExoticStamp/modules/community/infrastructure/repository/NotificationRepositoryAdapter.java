package metro.ExoticStamp.modules.community.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.community.domain.model.Notification;
import metro.ExoticStamp.modules.community.domain.model.PagedSlice;
import metro.ExoticStamp.modules.community.domain.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final JpaNotificationRepository jpaNotificationRepository;

    @Override
    public Notification save(Notification notification) {
        return jpaNotificationRepository.save(notification);
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return jpaNotificationRepository.findById(id);
    }

    @Override
    public Optional<Notification> findByUserIdAndId(UUID userId, UUID id) {
        return jpaNotificationRepository.findByUserIdAndId(userId, id);
    }

    @Override
    public PagedSlice<Notification> findByUserId(UUID userId, Boolean unreadOnly, int page, int size) {
        boolean unread = Boolean.TRUE.equals(unreadOnly);
        Page<Notification> p = jpaNotificationRepository.findFiltered(userId, unread, PageRequest.of(page, size));
        return new PagedSlice<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
    }

    @Override
    public int markAllReadByUserId(UUID userId) {
        return jpaNotificationRepository.markAllReadByUserId(userId, LocalDateTime.now());
    }
}
