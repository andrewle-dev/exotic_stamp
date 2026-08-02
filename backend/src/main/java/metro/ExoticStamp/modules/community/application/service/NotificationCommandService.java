package metro.ExoticStamp.modules.community.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.community.application.command.CreateNotificationCommand;
import metro.ExoticStamp.modules.community.application.mapper.CommunityAppMapper;
import metro.ExoticStamp.modules.community.application.support.MetadataSanitizer;
import metro.ExoticStamp.modules.community.application.view.NotificationView;
import metro.ExoticStamp.modules.community.domain.event.NotificationCreatedEvent;
import metro.ExoticStamp.modules.community.domain.exception.NotificationNotFoundException;
import metro.ExoticStamp.modules.community.domain.model.Notification;
import metro.ExoticStamp.modules.community.domain.repository.NotificationRepository;
import metro.ExoticStamp.modules.rbac.application.support.RbacTransactionCallbacks;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationCommandService {

    private final NotificationRepository notificationRepository;
    private final MetadataSanitizer metadataSanitizer;
    private final CommunityAppMapper communityAppMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional
    public NotificationView create(CreateNotificationCommand command) {
        Notification notification = Notification.builder()
                .userId(command.userId())
                .type(command.type())
                .title(command.title())
                .body(command.body())
                .referenceId(command.referenceId())
                .deepLink(command.deepLink())
                .metadata(metadataSanitizer.sanitize(command.metadata()))
                .build();

        Notification saved;
        try {
            saved = notificationRepository.save(notification);
        } catch (DataIntegrityViolationException ex) {
            // uq_notifications_user_type_ref — idempotent side effect (Batch E.1)
            log.debug("[Community] duplicate notification suppressed type={} refPresent={}",
                    command.type(), command.referenceId() != null);
            return communityAppMapper.toNotificationView(notification);
        }

        RbacTransactionCallbacks.afterCommit(() -> {
            try {
                eventPublisher.publishEvent(new NotificationCreatedEvent(
                        saved.getId(), saved.getUserId(), saved.getType()));
            } catch (Exception e) {
                log.error("[Community] NotificationCreatedEvent publish failed notificationId={}: {}",
                        saved.getId(), e.getClass().getSimpleName());
            }
        });

        return communityAppMapper.toNotificationView(saved);
    }

    @Transactional
    public NotificationView markRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findByUserIdAndId(userId, notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found: " + notificationId));
        notification.markRead(LocalDateTime.now(clock));
        return communityAppMapper.toNotificationView(notificationRepository.save(notification));
    }

    @Transactional
    public int markAllRead(UUID userId) {
        return notificationRepository.markAllReadByUserId(userId);
    }
}
