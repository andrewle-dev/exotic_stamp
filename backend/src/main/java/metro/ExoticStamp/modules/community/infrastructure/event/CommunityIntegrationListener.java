package metro.ExoticStamp.modules.community.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.collection.domain.event.StampCollectedEvent;
import metro.ExoticStamp.modules.user.domain.event.EmailVerifiedEvent;
import metro.ExoticStamp.modules.community.application.command.CreateNotificationCommand;
import metro.ExoticStamp.modules.community.application.service.NotificationCommandService;
import metro.ExoticStamp.modules.community.application.service.ReferralCommandService;
import metro.ExoticStamp.modules.community.domain.model.NotificationType;
import metro.ExoticStamp.modules.reward.domain.event.RewardIssuedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityIntegrationListener {

    private final NotificationCommandService notificationCommandService;
    private final ReferralCommandService referralCommandService;

    @Async
    @EventListener
    public void onRewardIssued(RewardIssuedEvent event) {
        try {
            notificationCommandService.create(new CreateNotificationCommand(
                    event.getUserId(),
                    NotificationType.REWARD,
                    "New reward earned",
                    "You unlocked a new reward. Open the app to view it.",
                    event.getUserRewardId().toString(),
                    "/rewards/" + event.getUserRewardId(),
                    Map.of("milestoneId", event.getMilestoneId().toString(),
                            "rewardType", event.getRewardType().name())
            ));
        } catch (Exception e) {
            log.error("[Community] reward notification failed userId={} userRewardId={}: {}",
                    event.getUserId(), event.getUserRewardId(), e.getMessage(), e);
        }
    }

    @Async
    @EventListener
    public void onEmailVerified(EmailVerifiedEvent event) {
        onUserEmailVerified(event.getUserId());
    }

    @Async
    @EventListener
    public void onStampCollected(StampCollectedEvent event) {
        try {
            referralCommandService.completePendingReferral(event.getUserId());
        } catch (Exception e) {
            log.error("[Community] referral completion on collect failed userId={}: {}",
                    event.getUserId(), e.getMessage(), e);
        }
    }

    /**
     * Hook for auth email verification.
     */
    public void onUserEmailVerified(java.util.UUID userId) {
        try {
            referralCommandService.completePendingReferral(userId);
        } catch (Exception e) {
            log.error("[Community] referral completion failed userId={}: {}", userId, e.getMessage(), e);
        }
    }
}
