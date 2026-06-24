package metro.ExoticStamp.modules.community.application.service;

import metro.ExoticStamp.modules.community.application.mapper.CommunityAppMapper;
import metro.ExoticStamp.modules.community.application.view.NotificationView;
import metro.ExoticStamp.modules.community.domain.exception.NotificationNotFoundException;
import metro.ExoticStamp.modules.community.domain.model.Notification;
import metro.ExoticStamp.modules.community.domain.model.NotificationType;
import metro.ExoticStamp.modules.community.domain.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationCommandServiceTest {

    @Mock private NotificationRepository notificationRepository;

    private NotificationCommandService service;
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-22T12:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        service = new NotificationCommandService(
                notificationRepository,
                null,
                new CommunityAppMapper(),
                null,
                clock
        );
    }

    @Test
    void markRead_owner_success() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        Notification n = Notification.builder()
                .id(notificationId).userId(userId).title("T").body("B")
                .type(NotificationType.SYSTEM).read(false).build();
        when(notificationRepository.findByUserIdAndId(userId, notificationId)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        NotificationView view = service.markRead(userId, notificationId);
        assertTrue(view.read());
        assertEquals(LocalDateTime.now(clock), view.readAt());
    }

    @Test
    void markRead_idempotent() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        LocalDateTime readAt = LocalDateTime.now(clock).minusHours(1);
        Notification n = Notification.builder()
                .id(notificationId).userId(userId).title("T").body("B")
                .type(NotificationType.SYSTEM).read(true).readAt(readAt).build();
        when(notificationRepository.findByUserIdAndId(userId, notificationId)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        NotificationView view = service.markRead(userId, notificationId);
        assertEquals(readAt, view.readAt());
    }

    @Test
    void markRead_otherUser_notFound() {
        UUID ownerId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        when(notificationRepository.findByUserIdAndId(otherId, notificationId)).thenReturn(Optional.empty());
        assertThrows(NotificationNotFoundException.class, () -> service.markRead(otherId, notificationId));
    }

    @Test
    void markAllRead_delegates() {
        UUID userId = UUID.randomUUID();
        when(notificationRepository.markAllReadByUserId(userId)).thenReturn(3);
        assertEquals(3, service.markAllRead(userId));
        verify(notificationRepository).markAllReadByUserId(userId);
    }
}
