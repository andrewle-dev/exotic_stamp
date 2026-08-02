package metro.ExoticStamp.modules.reward.presentation.mapper;

import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.reward.application.view.MilestoneView;
import metro.ExoticStamp.modules.reward.application.view.PartnerView;
import metro.ExoticStamp.modules.reward.application.view.RewardView;
import metro.ExoticStamp.modules.reward.application.view.UserRewardView;
import metro.ExoticStamp.modules.reward.application.view.UserRewardVoucherView;
import metro.ExoticStamp.modules.reward.application.view.VoucherPoolStatsView;
import metro.ExoticStamp.modules.reward.application.view.VoucherPoolView;
import metro.ExoticStamp.modules.reward.domain.model.MilestoneStatus;
import metro.ExoticStamp.modules.reward.domain.model.RewardStatus;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPoolStatus;
import metro.ExoticStamp.modules.reward.presentation.dto.RewardTypeApi;
import metro.ExoticStamp.modules.reward.presentation.request.CreateMilestoneRequest;
import metro.ExoticStamp.modules.reward.presentation.request.CreateRewardRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RewardPresentationMapperTest {

    private final RewardPresentationMapper mapper = new RewardPresentationMapper();

    @Test
    void toCreateRewardCommand_mapsRewardTypeName() {
        UUID milestoneId = UUID.randomUUID();
        CreateRewardRequest request = new CreateRewardRequest();
        request.setMilestoneId(milestoneId);
        request.setRewardType(RewardTypeApi.VOUCHER);
        request.setName("Coffee voucher");
        request.setDescription("One free coffee");
        request.setValueAmount(new BigDecimal("5.00"));
        request.setExpiryDays(30);
        request.setTotalStock(100);

        var command = mapper.toCreateRewardCommand(request);

        assertEquals(milestoneId, command.milestoneId());
        assertEquals("VOUCHER", command.rewardType());
        assertEquals("Coffee voucher", command.name());
        assertEquals(new BigDecimal("5.00"), command.valueAmount());
    }

    @Test
    void toCreateMilestoneCommand_defaultsSortOrderAndStatus() {
        UUID campaignId = UUID.randomUUID();
        CreateMilestoneRequest request = new CreateMilestoneRequest();
        request.setCampaignId(campaignId);
        request.setCode("M1");
        request.setRequiredStampCount(3);
        request.setName("Three stamps");
        request.setRewardType(RewardTypeApi.DIGITAL_STICKER);
        request.setRewardTitle("Sticker");

        var command = mapper.toCreateMilestoneCommand(request);

        assertEquals(campaignId, command.campaignId());
        assertEquals("DIGITAL_STICKER", command.rewardType());
        assertEquals(0, command.sortOrder());
        assertNull(command.status());
    }

    @Test
    void toRewardResponse_mapsEnumNames() {
        UUID id = UUID.randomUUID();
        RewardView view = RewardView.builder()
                .id(id)
                .milestoneId(UUID.randomUUID())
                .rewardType(RewardType.VOUCHER)
                .name("Voucher")
                .issuedCount(4)
                .active(true)
                .build();

        var response = mapper.toRewardResponse(view);

        assertEquals(id, response.id());
        assertEquals("VOUCHER", response.rewardType());
        assertEquals(4, response.issuedCount());
    }

    @Test
    void toUserRewardDetail_includesVoucherCode() {
        UserRewardView view = UserRewardView.builder()
                .id(UUID.randomUUID())
                .campaignId(UUID.randomUUID())
                .milestoneId(UUID.randomUUID())
                .milestoneCode("M1")
                .milestoneName("First")
                .rewardType(RewardType.VOUCHER)
                .rewardTitle("Coffee")
                .issuedAt(LocalDateTime.of(2026, 4, 12, 10, 0))
                .status(RewardStatus.ISSUED)
                .voucher(UserRewardVoucherView.builder()
                        .id(UUID.randomUUID())
                        .code("SECRET123")
                        .build())
                .build();

        var detail = mapper.toUserRewardDetail(view);

        assertEquals("SECRET123", detail.voucher().code());
        assertEquals("ISSUED", detail.status());
    }

    @Test
    void toUserRewardListItem_omitsVoucherCode() {
        UserRewardView view = UserRewardView.builder()
                .id(UUID.randomUUID())
                .campaignId(UUID.randomUUID())
                .milestoneId(UUID.randomUUID())
                .rewardType(RewardType.VOUCHER)
                .status(RewardStatus.ISSUED)
                .voucher(UserRewardVoucherView.builder()
                        .id(UUID.randomUUID())
                        .code("SECRET123")
                        .build())
                .build();

        var listItem = mapper.toUserRewardListItem(view);

        assertNull(listItem.voucher());
    }

    @Test
    void toRewardPage_preservesPaginationMetadata() {
        RewardView reward = RewardView.builder()
                .id(UUID.randomUUID())
                .milestoneId(UUID.randomUUID())
                .rewardType(RewardType.DIGITAL_STICKER)
                .name("Sticker")
                .issuedCount(0)
                .active(true)
                .build();
        PageResponse<RewardView> page = PageResponse.of(List.of(reward), 11, 2, 1, 10);

        PageResponse<?> mapped = mapper.toRewardPage(page);

        assertEquals(11, mapped.totalElements());
        assertEquals(2, mapped.totalPages());
        assertEquals(1, mapped.page());
        assertEquals(10, mapped.size());
    }

    @Test
    void toPartnerResponse_mapsContractDates() {
        UUID id = UUID.randomUUID();
        PartnerView view = PartnerView.builder()
                .id(id)
                .name("Partner")
                .contactEmail("a@b.test")
                .contractStartDate(LocalDate.of(2026, 1, 1))
                .contractEndDate(LocalDate.of(2026, 6, 1))
                .active(true)
                .build();

        var response = mapper.toPartnerResponse(view);

        assertEquals(id, response.id());
        assertEquals(LocalDate.of(2026, 1, 1), response.contractStartDate());
    }

    @Test
    void toMilestoneResponse_mapsStatusAndRewardType() {
        MilestoneView view = MilestoneView.builder()
                .id(UUID.randomUUID())
                .campaignId(UUID.randomUUID())
                .code("M2")
                .requiredStampCount(2)
                .name("Two stamps")
                .rewardType(RewardType.DIGITAL_BADGE)
                .rewardTitle("Badge")
                .status(MilestoneStatus.ACTIVE)
                .sortOrder(1)
                .build();

        var response = mapper.toMilestoneResponse(view);

        assertEquals("M2", response.code());
        assertEquals("DIGITAL_BADGE", response.rewardType());
        assertEquals("ACTIVE", response.status());
    }

    @Test
    void toVoucherStatsResponse_mapsCounts() {
        var response = mapper.toVoucherStatsResponse(VoucherPoolStatsView.builder()
                .availableCount(8)
                .redeemedCount(2)
                .build());

        assertEquals(8, response.availableCount());
        assertEquals(2, response.redeemedCount());
    }

    @Test
    void toVoucherPoolResponse_mapsAssignmentMetadata() {
        VoucherPoolView view = VoucherPoolView.builder()
                .id(UUID.randomUUID())
                .milestoneId(UUID.randomUUID())
                .code("POOL-1")
                .status(VoucherPoolStatus.AVAILABLE)
                .createdAt(LocalDateTime.of(2026, 4, 12, 9, 0))
                .build();

        var response = mapper.toVoucherPoolResponse(view);

        assertEquals("POOL-1", response.code());
        assertEquals("AVAILABLE", response.status());
    }
}
