package metro.ExoticStamp.modules.community.infrastructure.event;

import metro.ExoticStamp.modules.community.application.command.CreateNotificationCommand;
import metro.ExoticStamp.modules.community.application.service.NotificationCommandService;
import metro.ExoticStamp.modules.community.application.service.ReferralCommandService;
import metro.ExoticStamp.modules.community.domain.model.NotificationType;
import metro.ExoticStamp.modules.reward.domain.event.RewardIssuedEvent;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommunityIntegrationListenerTest {

    @Mock private NotificationCommandService notificationCommandService;
    @Mock private ReferralCommandService referralCommandService;
    @InjectMocks private CommunityIntegrationListener listener;

    @Test
    void onRewardIssued_alwaysSetsNonNullReferenceIdToUserRewardId() {
        UUID userId = UUID.randomUUID();
        UUID userRewardId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        listener.onRewardIssued(RewardIssuedEvent.milestoneIssued(
                userId, userRewardId, milestoneId, RewardType.VOUCHER));

        ArgumentCaptor<CreateNotificationCommand> captor = ArgumentCaptor.forClass(CreateNotificationCommand.class);
        verify(notificationCommandService).create(captor.capture());
        CreateNotificationCommand cmd = captor.getValue();
        assertEquals(NotificationType.REWARD, cmd.type());
        assertEquals(userRewardId.toString(), cmd.referenceId());
        assertNotNull(cmd.referenceId());
        assertFalse(cmd.referenceId().isBlank());
    }

    @Test
    void onRewardIssued_duplicateNotificationDoesNotPropagate() {
        doThrow(new DataIntegrityViolationException("uq_notifications_user_type_ref"))
                .when(notificationCommandService).create(any());
        assertDoesNotThrow(() -> listener.onRewardIssued(RewardIssuedEvent.milestoneIssued(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), RewardType.DIGITAL_STICKER)));
    }
}
