package metro.ExoticStamp.modules.community.presentation.mapper;

import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.community.application.command.ApplyReferralCommand;
import metro.ExoticStamp.modules.community.application.command.RecordShareEventCommand;
import metro.ExoticStamp.modules.community.application.view.MyReferralsView;
import metro.ExoticStamp.modules.community.application.view.NotificationView;
import metro.ExoticStamp.modules.community.application.view.ReferralCodeView;
import metro.ExoticStamp.modules.community.application.view.ReferralView;
import metro.ExoticStamp.modules.community.application.view.ShareEventView;
import metro.ExoticStamp.modules.community.presentation.request.ApplyReferralRequest;
import metro.ExoticStamp.modules.community.presentation.request.RecordShareEventRequest;
import metro.ExoticStamp.modules.community.presentation.response.MyReferralsResponse;
import metro.ExoticStamp.modules.community.presentation.response.NotificationResponse;
import metro.ExoticStamp.modules.community.presentation.response.ReferralCodeResponse;
import metro.ExoticStamp.modules.community.presentation.response.ReferralResponse;
import metro.ExoticStamp.modules.community.presentation.response.ShareEventResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommunityPresentationMapper {

    public ApplyReferralCommand toApplyReferralCommand(ApplyReferralRequest request) {
        return new ApplyReferralCommand(request.getCode());
    }

    public RecordShareEventCommand toRecordShareEventCommand(RecordShareEventRequest request) {
        return new RecordShareEventCommand(
                request.getPlatform(),
                request.getShareType(),
                request.getTargetId(),
                request.getMetadata()
        );
    }

    public ReferralCodeResponse toReferralCodeResponse(ReferralCodeView view) {
        return ReferralCodeResponse.builder()
                .id(view.id())
                .code(view.code())
                .status(view.status())
                .totalReferrals(view.totalReferrals())
                .createdAt(view.createdAt())
                .build();
    }

    public ReferralResponse toReferralResponse(ReferralView view) {
        return ReferralResponse.builder()
                .id(view.id())
                .referrerUserId(view.referrerUserId())
                .referredUserId(view.referredUserId())
                .referralCodeId(view.referralCodeId())
                .status(view.status())
                .appliedAt(view.appliedAt())
                .completedAt(view.completedAt())
                .rewardedAt(view.rewardedAt())
                .build();
    }

    public MyReferralsResponse toMyReferralsResponse(MyReferralsView view) {
        List<ReferralResponse> referred = view.referredUsers().stream()
                .map(this::toReferralResponse)
                .collect(Collectors.toList());
        return MyReferralsResponse.builder()
                .referredBy(view.referredBy() == null ? null : toReferralResponse(view.referredBy()))
                .referredUsers(referred)
                .pendingCount(view.pendingCount())
                .completedCount(view.completedCount())
                .rewardedCount(view.rewardedCount())
                .build();
    }

    public ShareEventResponse toShareEventResponse(ShareEventView view) {
        return ShareEventResponse.builder()
                .id(view.id())
                .platform(view.platform())
                .shareType(view.shareType())
                .targetId(view.targetId())
                .metadata(view.metadata())
                .sharedAt(view.sharedAt())
                .build();
    }

    public PageResponse<ShareEventResponse> toShareEventPage(PageResponse<ShareEventView> page) {
        List<ShareEventResponse> content = page.content().stream()
                .map(this::toShareEventResponse)
                .collect(Collectors.toList());
        return PageResponse.of(content, page.totalElements(), page.totalPages(), page.page(), page.size());
    }

    public NotificationResponse toNotificationResponse(NotificationView view) {
        return NotificationResponse.builder()
                .id(view.id())
                .type(view.type())
                .title(view.title())
                .body(view.body())
                .referenceId(view.referenceId())
                .read(view.read())
                .createdAt(view.createdAt())
                .readAt(view.readAt())
                .deepLink(view.deepLink())
                .metadata(view.metadata())
                .build();
    }

    public PageResponse<NotificationResponse> toNotificationPage(PageResponse<NotificationView> page) {
        List<NotificationResponse> content = page.content().stream()
                .map(this::toNotificationResponse)
                .collect(Collectors.toList());
        return PageResponse.of(content, page.totalElements(), page.totalPages(), page.page(), page.size());
    }
}
