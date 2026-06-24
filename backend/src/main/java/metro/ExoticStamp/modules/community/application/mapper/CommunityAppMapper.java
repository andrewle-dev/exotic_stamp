package metro.ExoticStamp.modules.community.application.mapper;

import metro.ExoticStamp.modules.community.application.view.NotificationView;
import metro.ExoticStamp.modules.community.application.view.ReferralCodeView;
import metro.ExoticStamp.modules.community.application.view.ReferralView;
import metro.ExoticStamp.modules.community.application.view.ShareEventView;
import metro.ExoticStamp.modules.community.domain.model.Notification;
import metro.ExoticStamp.modules.community.domain.model.Referral;
import metro.ExoticStamp.modules.community.domain.model.ReferralCode;
import metro.ExoticStamp.modules.community.domain.model.ShareEvent;
import org.springframework.stereotype.Component;

@Component
public class CommunityAppMapper {

    public ReferralCodeView toReferralCodeView(ReferralCode code) {
        return ReferralCodeView.builder()
                .id(code.getId())
                .code(code.getCode())
                .status(code.getStatus().name())
                .totalReferrals(code.getTotalReferrals())
                .createdAt(code.getCreatedAt())
                .build();
    }

    public ReferralView toReferralView(Referral referral) {
        return ReferralView.builder()
                .id(referral.getId())
                .referrerUserId(referral.getReferrerUserId())
                .referredUserId(referral.getReferredUserId())
                .referralCodeId(referral.getReferralCodeId())
                .status(referral.getStatus().name())
                .appliedAt(referral.getReferredAt())
                .completedAt(referral.getCompletedAt())
                .rewardedAt(referral.getRewardIssuedAt())
                .build();
    }

    public ShareEventView toShareEventView(ShareEvent event) {
        return ShareEventView.builder()
                .id(event.getId())
                .platform(event.getPlatform().name())
                .shareType(event.getShareType().name())
                .targetId(event.getTargetId())
                .metadata(event.getMetadata())
                .sharedAt(event.getSharedAt())
                .build();
    }

    public NotificationView toNotificationView(Notification notification) {
        return NotificationView.builder()
                .id(notification.getId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .body(notification.getBody())
                .referenceId(notification.getReferenceId())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .deepLink(notification.getDeepLink())
                .metadata(notification.getMetadata())
                .build();
    }
}
