package metro.ExoticStamp.modules.reward.domain.exception;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RewardDomainExceptionsTest {

    @Test
    void milestoneNotFound_carriesMessage() {
        var ex = new MilestoneNotFoundException("Milestone not found: abc");
        assertTrue(ex.getMessage().contains("Milestone not found"));
    }

    @Test
    void milestoneArchived_includesId() {
        UUID id = UUID.randomUUID();
        var ex = new MilestoneArchivedException(id);
        assertTrue(ex.getMessage().contains(id.toString()));
        assertTrue(ex.getMessage().contains("archived"));
    }

    @Test
    void partnerStateExceptions_carryContext() {
        assertTrue(new PartnerAlreadyActiveException("Partner already active: x").getMessage().contains("active"));
        assertTrue(new PartnerAlreadyInactiveException("Partner already inactive: x").getMessage().contains("inactive"));
        assertTrue(new PartnerNotFoundException("Partner not found: x").getMessage().contains("Partner not found"));
    }

    @Test
    void rewardStateExceptions_carryContext() {
        assertTrue(new RewardNotFoundException("Reward not found: x").getMessage().contains("Reward not found"));
        assertTrue(new RewardAlreadyActiveException("Reward already active: x").getMessage().contains("active"));
        assertTrue(new RewardAlreadyInactiveException("Reward already inactive: x").getMessage().contains("inactive"));
        assertTrue(new RewardNotRedeemableException("not redeemable").getMessage().contains("redeemable"));
        assertTrue(new RewardAlreadyIssuedException("already issued").getMessage().contains("issued"));
    }

    @Test
    void voucherCodeExceptions_formatMessages() {
        assertEquals("Voucher code already exists: ABC", new VoucherCodeDuplicateException("ABC").getMessage());
        assertEquals("Milestone code already exists for campaign: M1",
                new MilestoneCodeDuplicateException("M1").getMessage());
        assertTrue(new VoucherCodeExhaustedException("pool empty").getMessage().contains("pool empty"));
    }

    @Test
    void invalidMilestoneState_andRedeemNotSupported() {
        assertEquals("bad state", new InvalidMilestoneStateException("bad state").getMessage());
        assertTrue(new RedeemNotSupportedException("MVP").getMessage().contains("MVP"));
    }
}
