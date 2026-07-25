package metro.ExoticStamp.modules.reward.application.mapper;

import metro.ExoticStamp.modules.reward.application.view.UserRewardVoucherView;
import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import metro.ExoticStamp.modules.reward.domain.model.MilestoneStatus;
import metro.ExoticStamp.modules.reward.domain.model.Partner;
import metro.ExoticStamp.modules.reward.domain.model.Reward;
import metro.ExoticStamp.modules.reward.domain.model.RewardStatus;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import metro.ExoticStamp.modules.reward.domain.model.UserReward;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPool;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPoolStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RewardAppMapperTest {

    private final RewardAppMapper mapper = new RewardAppMapper();

    @Test
    void toPartnerView_mapsContractDatesAndActiveFlag() {
        UUID id = UUID.randomUUID();
        Partner partner = Partner.builder()
                .id(id)
                .name("Highland")
                .logoUrl("https://cdn/logo.png")
                .bannerImageUrl("https://cdn/banner.png")
                .contactEmail("partner@highland.com")
                .contractStartDate(LocalDate.of(2026, 1, 1))
                .contractEndDate(LocalDate.of(2026, 12, 31))
                .active(true)
                .build();

        var view = mapper.toPartnerView(partner);

        assertEquals(id, view.id());
        assertEquals("Highland", view.name());
        assertEquals("https://cdn/banner.png", view.bannerImageUrl());
        assertTrue(view.active());
    }

    @Test
    void toMilestoneView_mapsStampRequirementAndRewardMetadata() {
        UUID id = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        Milestone milestone = Milestone.builder()
                .id(id)
                .campaignId(campaignId)
                .code("M3")
                .stampsRequired(3)
                .name("Three stamps")
                .description("Collect three")
                .rewardType(RewardType.VOUCHER)
                .rewardTitle("Free drink")
                .rewardDescription("One drink")
                .rewardImageUrl("https://cdn/reward.png")
                .status(MilestoneStatus.ACTIVE)
                .sortOrder(2)
                .build();

        var view = mapper.toMilestoneView(milestone);

        assertEquals(3, view.requiredStampCount());
        assertEquals("Free drink", view.rewardTitle());
        assertEquals(RewardType.VOUCHER, view.rewardType());
    }

    @Test
    void toRewardView_defaultsNullIssuedCountToZero() {
        Reward reward = Reward.builder()
                .id(UUID.randomUUID())
                .milestoneId(UUID.randomUUID())
                .rewardType(RewardType.VOUCHER)
                .name("Voucher")
                .valueAmount(new BigDecimal("15.50"))
                .issuedCount(null)
                .active(false)
                .build();

        var view = mapper.toRewardView(reward);

        assertEquals(0, view.issuedCount());
        assertEquals(new BigDecimal("15.50"), view.valueAmount());
        assertEquals(false, view.active());
    }

    @Test
    void toUserRewardView_usesMilestoneFallbacksAndVoucher() {
        UUID userRewardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        LocalDateTime issuedAt = LocalDateTime.of(2026, 4, 12, 10, 0);

        UserReward userReward = UserReward.builder()
                .id(userRewardId)
                .userId(userId)
                .campaignId(campaignId)
                .milestoneId(milestoneId)
                .issuedAt(issuedAt)
                .status(RewardStatus.ISSUED)
                .build();
        Milestone milestone = Milestone.builder()
                .id(milestoneId)
                .campaignId(campaignId)
                .code("M1")
                .stampsRequired(1)
                .name("First stamp")
                .rewardType(RewardType.VOUCHER)
                .rewardTitle("Coffee")
                .rewardDescription("Small coffee")
                .rewardImageUrl("https://cdn/coffee.png")
                .status(MilestoneStatus.ACTIVE)
                .sortOrder(0)
                .build();
        UserRewardVoucherView voucher = UserRewardVoucherView.builder()
                .id(UUID.randomUUID())
                .code("ABC123")
                .build();

        var view = mapper.toUserRewardView(userReward, milestone, voucher);

        assertEquals("M1", view.milestoneCode());
        assertEquals("Coffee", view.rewardTitle());
        assertEquals("ABC123", view.voucher().code());
        assertEquals(RewardStatus.ISSUED, view.status());
    }

    @Test
    void toUserRewardView_nullMilestoneKeepsCampaignFromUserReward() {
        UUID campaignId = UUID.randomUUID();
        UserReward userReward = UserReward.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .campaignId(campaignId)
                .milestoneId(UUID.randomUUID())
                .issuedAt(LocalDateTime.now())
                .status(RewardStatus.PENDING_STOCK)
                .build();

        var view = mapper.toUserRewardView(userReward, null, null);

        assertEquals(campaignId, view.campaignId());
        assertNull(view.milestoneCode());
        assertNull(view.voucher());
    }

    @Test
    void toVoucherPoolView_mapsAssignmentFields() {
        UUID poolId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID userRewardId = UUID.randomUUID();
        LocalDateTime assignedAt = LocalDateTime.of(2026, 4, 12, 11, 0);

        VoucherPool pool = VoucherPool.builder()
                .id(poolId)
                .milestoneId(milestoneId)
                .code("VOUCHER-1")
                .status(VoucherPoolStatus.CLAIMED)
                .assignedUserId(userId)
                .assignedUserRewardId(userRewardId)
                .assignedAt(assignedAt)
                .build();

        var view = mapper.toVoucherPoolView(pool);

        assertEquals("VOUCHER-1", view.code());
        assertEquals(VoucherPoolStatus.CLAIMED, view.status());
        assertEquals(userRewardId, view.assignedUserRewardId());
    }
}
