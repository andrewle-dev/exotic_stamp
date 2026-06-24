package metro.ExoticStamp.modules.reward.presentation.mapper;

import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.reward.application.command.CreateMilestoneCommand;
import metro.ExoticStamp.modules.reward.application.command.CreatePartnerCommand;
import metro.ExoticStamp.modules.reward.application.command.CreateRewardCommand;
import metro.ExoticStamp.modules.reward.application.command.ImportVouchersCommand;
import metro.ExoticStamp.modules.reward.application.command.UpdateMilestoneCommand;
import metro.ExoticStamp.modules.reward.application.command.UpdatePartnerCommand;
import metro.ExoticStamp.modules.reward.application.command.UpdateRewardCommand;
import metro.ExoticStamp.modules.reward.application.view.MilestoneView;
import metro.ExoticStamp.modules.reward.application.view.PartnerView;
import metro.ExoticStamp.modules.reward.application.view.RewardView;
import metro.ExoticStamp.modules.reward.application.view.UserRewardView;
import metro.ExoticStamp.modules.reward.application.view.VoucherPoolStatsView;
import metro.ExoticStamp.modules.reward.application.view.VoucherPoolView;
import metro.ExoticStamp.modules.reward.presentation.dto.RewardStatusApi;
import metro.ExoticStamp.modules.reward.presentation.request.CreateMilestoneRequest;
import metro.ExoticStamp.modules.reward.presentation.request.CreatePartnerRequest;
import metro.ExoticStamp.modules.reward.presentation.request.CreateRewardRequest;
import metro.ExoticStamp.modules.reward.presentation.request.ImportVouchersRequest;
import metro.ExoticStamp.modules.reward.presentation.request.UpdateMilestoneRequest;
import metro.ExoticStamp.modules.reward.presentation.request.UpdatePartnerRequest;
import metro.ExoticStamp.modules.reward.presentation.request.UpdateRewardRequest;
import metro.ExoticStamp.modules.reward.presentation.response.MilestoneResponse;
import metro.ExoticStamp.modules.reward.presentation.response.PartnerResponse;
import metro.ExoticStamp.modules.reward.presentation.response.RewardResponse;
import metro.ExoticStamp.modules.reward.presentation.response.UserRewardResponse;
import metro.ExoticStamp.modules.reward.presentation.response.UserRewardVoucherResponse;
import metro.ExoticStamp.modules.reward.presentation.response.VoucherPoolResponse;
import metro.ExoticStamp.modules.reward.presentation.response.VoucherPoolStatsResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RewardPresentationMapper {

    public CreatePartnerCommand toCreatePartnerCommand(CreatePartnerRequest r) {
        return new CreatePartnerCommand(r.getName(), r.getLogoUrl(), r.getContactEmail(),
                r.getContractStartDate(), r.getContractEndDate());
    }

    public UpdatePartnerCommand toUpdatePartnerCommand(UUID id, UpdatePartnerRequest r) {
        return new UpdatePartnerCommand(id, r.getName(), r.getLogoUrl(), r.getContactEmail(),
                r.getContractStartDate(), r.getContractEndDate());
    }

    public PartnerResponse toPartnerResponse(PartnerView v) {
        return PartnerResponse.builder()
                .id(v.id())
                .name(v.name())
                .logoUrl(v.logoUrl())
                .contactEmail(v.contactEmail())
                .contractStartDate(v.contractStartDate())
                .contractEndDate(v.contractEndDate())
                .active(v.active())
                .build();
    }

    public CreateMilestoneCommand toCreateMilestoneCommand(CreateMilestoneRequest r) {
        return new CreateMilestoneCommand(
                r.getCampaignId(),
                r.getCode(),
                r.getRequiredStampCount(),
                r.getName(),
                r.getDescription(),
                r.getRewardType().name(),
                r.getRewardTitle(),
                r.getRewardDescription(),
                r.getRewardImageUrl(),
                r.getStatus() == null ? null : r.getStatus().name(),
                r.getSortOrder() == null ? 0 : r.getSortOrder()
        );
    }

    public UpdateMilestoneCommand toUpdateMilestoneCommand(UUID id, UpdateMilestoneRequest r) {
        return new UpdateMilestoneCommand(
                id,
                r.getCode(),
                r.getRequiredStampCount(),
                r.getName(),
                r.getDescription(),
                r.getRewardType() == null ? null : r.getRewardType().name(),
                r.getRewardTitle(),
                r.getRewardDescription(),
                r.getRewardImageUrl(),
                r.getStatus() == null ? null : r.getStatus().name(),
                r.getSortOrder()
        );
    }

    public MilestoneResponse toMilestoneResponse(MilestoneView v) {
        return MilestoneResponse.builder()
                .id(v.id())
                .campaignId(v.campaignId())
                .code(v.code())
                .requiredStampCount(v.requiredStampCount())
                .name(v.name())
                .description(v.description())
                .rewardType(enumName(v.rewardType()))
                .rewardTitle(v.rewardTitle())
                .rewardDescription(v.rewardDescription())
                .rewardImageUrl(v.rewardImageUrl())
                .status(enumName(v.status()))
                .sortOrder(v.sortOrder())
                .deletedAt(v.deletedAt())
                .build();
    }

    public ImportVouchersCommand toImportVouchersCommand(ImportVouchersRequest r) {
        return new ImportVouchersCommand(r.getMilestoneId(), r.getCodes(), r.getExpiresAt());
    }

    public VoucherPoolResponse toVoucherPoolResponse(VoucherPoolView v) {
        return VoucherPoolResponse.builder()
                .id(v.id())
                .milestoneId(v.milestoneId())
                .code(v.code())
                .status(enumName(v.status()))
                .assignedUserId(v.assignedUserId())
                .assignedUserRewardId(v.assignedUserRewardId())
                .assignedAt(v.assignedAt())
                .expiresAt(v.expiresAt())
                .createdAt(v.createdAt())
                .build();
    }

    public String parseRewardStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        RewardStatusApi.valueOf(status);
        return status;
    }

    public CreateRewardCommand toCreateRewardCommand(CreateRewardRequest r) {
        return new CreateRewardCommand(r.getMilestoneId(), r.getPartnerId(), r.getRewardType().name(),
                r.getName(), r.getDescription(), r.getValueAmount(), r.getExpiryDays(), r.getTotalStock());
    }

    public UpdateRewardCommand toUpdateRewardCommand(UUID id, UpdateRewardRequest r) {
        return new UpdateRewardCommand(id, r.getMilestoneId(), r.getPartnerId(),
                r.getRewardType() == null ? null : r.getRewardType().name(),
                r.getName(), r.getDescription(), r.getValueAmount(), r.getExpiryDays(), r.getTotalStock());
    }

    public RewardResponse toRewardResponse(RewardView v) {
        return RewardResponse.builder()
                .id(v.id())
                .milestoneId(v.milestoneId())
                .partnerId(v.partnerId())
                .rewardType(enumName(v.rewardType()))
                .name(v.name())
                .description(v.description())
                .valueAmount(v.valueAmount())
                .expiryDays(v.expiryDays())
                .totalStock(v.totalStock())
                .issuedCount(v.issuedCount())
                .active(v.active())
                .build();
    }

    public PageResponse<UserRewardResponse> toUserRewardListPage(PageResponse<UserRewardView> page) {
        List<UserRewardResponse> content = page.content().stream()
                .map(this::toUserRewardListItem)
                .collect(Collectors.toList());
        return PageResponse.of(content, page.totalElements(), page.totalPages(), page.page(), page.size());
    }

    public UserRewardResponse toUserRewardListItem(UserRewardView v) {
        return toUserRewardResponse(v, false);
    }

    public UserRewardResponse toUserRewardDetail(UserRewardView v) {
        return toUserRewardResponse(v, true);
    }

    private UserRewardResponse toUserRewardResponse(UserRewardView v, boolean includeVoucher) {
        return UserRewardResponse.builder()
                .id(v.id())
                .campaignId(v.campaignId())
                .milestoneId(v.milestoneId())
                .milestoneCode(v.milestoneCode())
                .milestoneName(v.milestoneName())
                .rewardType(enumName(v.rewardType()))
                .rewardTitle(v.rewardTitle())
                .rewardDescription(v.rewardDescription())
                .rewardImageUrl(v.rewardImageUrl())
                .issuedAt(v.issuedAt())
                .expiresAt(v.expiresAt())
                .redeemedAt(v.redeemedAt())
                .status(enumName(v.status()))
                .voucher(includeVoucher && v.voucher() != null
                        ? UserRewardVoucherResponse.builder()
                        .id(v.voucher().id())
                        .code(v.voucher().code())
                        .build()
                        : null)
                .build();
    }

    private static String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }

    public VoucherPoolStatsResponse toVoucherStatsResponse(VoucherPoolStatsView v) {
        return VoucherPoolStatsResponse.builder()
                .availableCount(v.availableCount())
                .redeemedCount(v.redeemedCount())
                .build();
    }

    public PageResponse<PartnerResponse> toPartnerPage(PageResponse<PartnerView> page) {
        List<PartnerResponse> content = page.content().stream()
                .map(this::toPartnerResponse)
                .collect(Collectors.toList());
        return PageResponse.of(content, page.totalElements(), page.totalPages(), page.page(), page.size());
    }

    public PageResponse<MilestoneResponse> toMilestonePage(PageResponse<MilestoneView> page) {
        List<MilestoneResponse> content = page.content().stream()
                .map(this::toMilestoneResponse)
                .collect(Collectors.toList());
        return PageResponse.of(content, page.totalElements(), page.totalPages(), page.page(), page.size());
    }

    public PageResponse<VoucherPoolResponse> toVoucherPoolPage(PageResponse<VoucherPoolView> page) {
        List<VoucherPoolResponse> content = page.content().stream()
                .map(this::toVoucherPoolResponse)
                .collect(Collectors.toList());
        return PageResponse.of(content, page.totalElements(), page.totalPages(), page.page(), page.size());
    }

    public PageResponse<RewardResponse> toRewardPage(PageResponse<RewardView> page) {
        List<RewardResponse> content = page.content().stream()
                .map(this::toRewardResponse)
                .collect(Collectors.toList());
        return PageResponse.of(content, page.totalElements(), page.totalPages(), page.page(), page.size());
    }
}
