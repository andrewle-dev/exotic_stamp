package metro.ExoticStamp.modules.reward.application.mapper;

import metro.ExoticStamp.modules.reward.application.view.MilestoneView;
import metro.ExoticStamp.modules.reward.application.view.PartnerView;
import metro.ExoticStamp.modules.reward.application.view.PromotionalPartnerBannerView;
import metro.ExoticStamp.modules.reward.application.view.RewardView;
import metro.ExoticStamp.modules.reward.application.view.UserRewardView;
import metro.ExoticStamp.modules.reward.application.view.UserRewardVoucherView;
import metro.ExoticStamp.modules.reward.application.view.VoucherPoolView;
import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import metro.ExoticStamp.modules.reward.domain.model.Partner;
import metro.ExoticStamp.modules.reward.domain.model.Reward;
import metro.ExoticStamp.modules.reward.domain.model.UserReward;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPool;
import org.springframework.stereotype.Component;

@Component
public class RewardAppMapper {

    public PartnerView toPartnerView(Partner p) {
        if (p == null) {
            return null;
        }
        return PartnerView.builder()
                .id(p.getId())
                .name(p.getName())
                .logoUrl(p.getLogoUrl())
                .bannerImageUrl(p.getBannerImageUrl())
                .contactEmail(p.getContactEmail())
                .contractStartDate(p.getContractStartDate())
                .contractEndDate(p.getContractEndDate())
                .active(p.isActive())
                .build();
    }

    public PromotionalPartnerBannerView toPromotionalPartnerBannerView(Partner p) {
        if (p == null) {
            return null;
        }
        return PromotionalPartnerBannerView.builder()
                .partnerId(p.getId())
                .partnerName(p.getName())
                .logoUrl(p.getLogoUrl())
                .bannerImageUrl(p.getBannerImageUrl())
                .contractStart(p.getContractStartDate())
                .contractEnd(p.getContractEndDate())
                .build();
    }

    public MilestoneView toMilestoneView(Milestone m) {
        if (m == null) {
            return null;
        }
        return MilestoneView.builder()
                .id(m.getId())
                .campaignId(m.getCampaignId())
                .code(m.getCode())
                .requiredStampCount(m.getStampsRequired())
                .name(m.getName())
                .description(m.getDescription())
                .rewardType(m.getRewardType())
                .rewardTitle(m.getRewardTitle())
                .rewardDescription(m.getRewardDescription())
                .rewardImageUrl(m.getRewardImageUrl())
                .status(m.getStatus())
                .sortOrder(m.getSortOrder())
                .deletedAt(m.getDeletedAt())
                .build();
    }

    public RewardView toRewardView(Reward r) {
        if (r == null) {
            return null;
        }
        return RewardView.builder()
                .id(r.getId())
                .milestoneId(r.getMilestoneId())
                .partnerId(r.getPartnerId())
                .rewardType(r.getRewardType())
                .name(r.getName())
                .description(r.getDescription())
                .valueAmount(r.getValueAmount())
                .expiryDays(r.getExpiryDays())
                .totalStock(r.getTotalStock())
                .issuedCount(r.getIssuedCount() != null ? r.getIssuedCount() : 0)
                .active(r.isActive())
                .build();
    }

    public UserRewardView toUserRewardView(UserReward ur, Milestone milestone, UserRewardVoucherView voucher) {
        if (ur == null) {
            return null;
        }
        return UserRewardView.builder()
                .id(ur.getId())
                .userId(ur.getUserId())
                .campaignId(ur.getCampaignId() != null ? ur.getCampaignId()
                        : milestone != null ? milestone.getCampaignId() : null)
                .milestoneId(ur.getMilestoneId())
                .milestoneCode(milestone != null ? milestone.getCode() : null)
                .milestoneName(milestone != null ? milestone.getName() : null)
                .rewardType(milestone != null ? milestone.getRewardType() : null)
                .rewardTitle(milestone != null ? milestone.getRewardTitle() : null)
                .rewardDescription(milestone != null ? milestone.getRewardDescription() : null)
                .rewardImageUrl(milestone != null ? milestone.getRewardImageUrl() : null)
                .issuedAt(ur.getIssuedAt())
                .expiresAt(ur.getExpiresAt())
                .redeemedAt(ur.getRedeemedAt())
                .status(ur.getStatus())
                .voucher(voucher)
                .build();
    }

    public VoucherPoolView toVoucherPoolView(VoucherPool vp) {
        if (vp == null) {
            return null;
        }
        return VoucherPoolView.builder()
                .id(vp.getId())
                .milestoneId(vp.getMilestoneId())
                .code(vp.getCode())
                .status(vp.getStatus())
                .assignedUserId(vp.getAssignedUserId())
                .assignedUserRewardId(vp.getAssignedUserRewardId())
                .assignedAt(vp.getAssignedAt())
                .expiresAt(vp.getExpiresAt())
                .createdAt(vp.getCreatedAt())
                .build();
    }
}
