package metro.ExoticStamp.modules.reward.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RewardDomainModelTest {

    @Test
    void milestone_isEvaluable_onlyWhenActiveAndNotDeleted() {
        Milestone active = Milestone.builder()
                .campaignId(UUID.randomUUID())
                .code("m1")
                .stampsRequired(3)
                .name("M")
                .rewardType(RewardType.VOUCHER)
                .rewardTitle("T")
                .status(MilestoneStatus.ACTIVE)
                .sortOrder(0)
                .build();
        assertTrue(active.isEvaluable());

        Milestone draft = Milestone.builder()
                .campaignId(active.getCampaignId())
                .code("m1")
                .stampsRequired(3)
                .name("M")
                .rewardType(RewardType.VOUCHER)
                .rewardTitle("T")
                .status(MilestoneStatus.DRAFT)
                .sortOrder(0)
                .build();
        assertFalse(draft.isEvaluable());

        Milestone deleted = Milestone.builder()
                .campaignId(active.getCampaignId())
                .code("m1")
                .stampsRequired(3)
                .name("M")
                .rewardType(RewardType.VOUCHER)
                .rewardTitle("T")
                .status(MilestoneStatus.ACTIVE)
                .sortOrder(0)
                .deletedAt(java.time.LocalDateTime.now())
                .build();
        assertFalse(deleted.isEvaluable());
    }

    @Test
    void milestone_isEvaluable_inactiveStatusEvenWithoutDeletedAt() {
        Milestone inactive = Milestone.builder()
                .campaignId(UUID.randomUUID())
                .code("M1")
                .stampsRequired(1)
                .name("M")
                .rewardType(RewardType.VOUCHER)
                .rewardTitle("T")
                .status(MilestoneStatus.INACTIVE)
                .sortOrder(0)
                .build();
        assertFalse(inactive.isEvaluable());
    }

    @Test
    void milestone_normalizeAndValidate_trimsAndUppercasesCode() {
        Milestone m = Milestone.builder()
                .campaignId(UUID.randomUUID())
                .code("  m1  ")
                .stampsRequired(1)
                .name("  Name  ")
                .rewardType(RewardType.VOUCHER)
                .rewardTitle("  Title  ")
                .status(MilestoneStatus.DRAFT)
                .sortOrder(0)
                .build();

        m.normalizeAndValidate();

        assertTrue(m.getCode().equals("M1"));
        assertTrue(m.getName().equals("Name"));
        assertTrue(m.getRewardTitle().equals("Title"));
        assertTrue(m.isActive() == false);
    }

    @Test
    void milestone_normalizeAndValidate_defaultsNullStatusToDraft() {
        Milestone m = Milestone.builder()
                .campaignId(UUID.randomUUID())
                .code("m1")
                .stampsRequired(1)
                .name("Name")
                .rewardType(RewardType.VOUCHER)
                .rewardTitle("Title")
                .sortOrder(null)
                .build();
        m.normalizeAndValidate();
        assertTrue(m.getStatus() == MilestoneStatus.DRAFT);
        assertTrue(m.getSortOrder() == 0);
        assertFalse(m.isActive());
    }

    @Test
    void milestone_normalizeAndValidate_rejectsBlankCodeAndRewardTitle() {
        Milestone blankCode = Milestone.builder()
                .campaignId(UUID.randomUUID())
                .code("   ")
                .stampsRequired(1)
                .name("M")
                .rewardType(RewardType.VOUCHER)
                .rewardTitle("T")
                .status(MilestoneStatus.DRAFT)
                .sortOrder(0)
                .build();
        assertThrows(IllegalArgumentException.class, blankCode::normalizeAndValidate);

        Milestone blankTitle = Milestone.builder()
                .campaignId(UUID.randomUUID())
                .code("M1")
                .stampsRequired(1)
                .name("M")
                .rewardType(RewardType.VOUCHER)
                .rewardTitle("   ")
                .status(MilestoneStatus.DRAFT)
                .sortOrder(0)
                .build();
        assertThrows(IllegalArgumentException.class, blankTitle::normalizeAndValidate);
    }

    @Test
    void milestone_normalizeAndValidate_rejectsNegativeSortOrder() {
        Milestone m = Milestone.builder()
                .campaignId(UUID.randomUUID())
                .code("M1")
                .stampsRequired(1)
                .name("M")
                .rewardType(RewardType.VOUCHER)
                .rewardTitle("T")
                .status(MilestoneStatus.DRAFT)
                .sortOrder(-1)
                .build();
        assertThrows(IllegalArgumentException.class, m::normalizeAndValidate);
    }

    @Test
    void milestone_normalizeAndValidate_rejectsInvalidStampsRequired() {
        Milestone m = Milestone.builder()
                .campaignId(UUID.randomUUID())
                .code("M1")
                .stampsRequired(0)
                .name("M")
                .rewardType(RewardType.VOUCHER)
                .rewardTitle("T")
                .status(MilestoneStatus.DRAFT)
                .sortOrder(0)
                .build();

        assertThrows(IllegalArgumentException.class, m::normalizeAndValidate);
    }

    @Test
    void milestone_isArchived_andIsDeleted() {
        Milestone archived = Milestone.builder()
                .campaignId(UUID.randomUUID())
                .code("M1")
                .stampsRequired(1)
                .name("M")
                .rewardType(RewardType.VOUCHER)
                .rewardTitle("T")
                .status(MilestoneStatus.ARCHIVED)
                .sortOrder(0)
                .build();
        assertTrue(archived.isArchived());
        assertFalse(archived.isDeleted());
    }

    @Test
    void voucherPool_statusSyncsRedeemedFlag() {
        VoucherPool claimed = VoucherPool.builder()
                .milestoneId(UUID.randomUUID())
                .code(" ABC ")
                .status(VoucherPoolStatus.CLAIMED)
                .build();
        claimed.normalizeAndValidate();

        assertTrue(claimed.getCode().equals("ABC"));
        assertTrue(claimed.isRedeemed());

        VoucherPool available = VoucherPool.builder()
                .milestoneId(claimed.getMilestoneId())
                .code("ABC")
                .status(VoucherPoolStatus.AVAILABLE)
                .build();
        available.normalizeAndValidate();
        assertFalse(available.isRedeemed());
    }

    @Test
    void voucherPool_requiresMilestoneOrRewardId() {
        VoucherPool vp = VoucherPool.builder()
                .code("X")
                .status(VoucherPoolStatus.AVAILABLE)
                .build();

        assertThrows(IllegalArgumentException.class, vp::normalizeAndValidate);
    }

    @Test
    void reward_validation_rejectsBlankName() {
        Reward r = Reward.builder()
                .milestoneId(UUID.randomUUID())
                .rewardType(RewardType.VOUCHER)
                .name("   ")
                .issuedCount(0)
                .active(true)
                .build();

        assertThrows(IllegalArgumentException.class, r::onPrePersist);
    }

    @Test
    void reward_onPrePersist_defaultsIssuedCount() {
        Reward r = Reward.builder()
                .milestoneId(UUID.randomUUID())
                .rewardType(RewardType.VOUCHER)
                .name("Reward")
                .active(true)
                .build();

        r.onPrePersist();

        assertTrue(r.getIssuedCount() == 0);
        assertTrue(r.getName().equals("Reward"));
    }

    @Test
    void reward_validation_rejectsNullMilestoneIdAndNegativeCounts() {
        Reward noMilestone = Reward.builder()
                .rewardType(RewardType.VOUCHER)
                .name("Reward")
                .issuedCount(0)
                .active(true)
                .build();
        assertThrows(IllegalArgumentException.class, noMilestone::onPrePersist);

        Reward negativeStock = Reward.builder()
                .milestoneId(UUID.randomUUID())
                .rewardType(RewardType.VOUCHER)
                .name("Reward")
                .issuedCount(0)
                .totalStock(-1)
                .active(true)
                .build();
        assertThrows(IllegalArgumentException.class, negativeStock::onPrePersist);
    }

    @Test
    void milestone_isEvaluable_archivedStatus_isFalse() {
        Milestone archived = Milestone.builder()
                .campaignId(UUID.randomUUID())
                .code("M1")
                .stampsRequired(1)
                .name("M")
                .rewardType(RewardType.VOUCHER)
                .rewardTitle("T")
                .status(MilestoneStatus.ARCHIVED)
                .sortOrder(0)
                .build();
        assertFalse(archived.isEvaluable());
    }

    @Test
    void milestone_normalizeAndValidate_rejectsNullRewardType() {
        Milestone m = Milestone.builder()
                .campaignId(UUID.randomUUID())
                .code("M1")
                .stampsRequired(1)
                .name("M")
                .rewardTitle("T")
                .status(MilestoneStatus.DRAFT)
                .sortOrder(0)
                .build();
        assertThrows(IllegalArgumentException.class, m::normalizeAndValidate);
    }
}