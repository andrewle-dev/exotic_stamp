package metro.ExoticStamp.modules.reward.application.support;

import metro.ExoticStamp.modules.reward.domain.model.MilestoneStatus;
import metro.ExoticStamp.modules.reward.domain.model.RewardStatus;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPoolStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RewardEnumParserTest {

    @Test
    void parseMilestoneStatus_nullOrBlank_returnsNull() {
        assertNull(RewardEnumParser.parseMilestoneStatus(null));
        assertNull(RewardEnumParser.parseMilestoneStatus(" "));
    }

    @Test
    void parseMilestoneStatus_validValue() {
        assertEquals(MilestoneStatus.ACTIVE, RewardEnumParser.parseMilestoneStatus("ACTIVE"));
    }

    @Test
    void parseMilestoneStatus_invalid_throws() {
        assertThrows(IllegalArgumentException.class, () -> RewardEnumParser.parseMilestoneStatus("GONE"));
    }

    @Test
    void parseRewardType_nullOrValid() {
        assertNull(RewardEnumParser.parseRewardType(null));
        assertEquals(RewardType.VOUCHER, RewardEnumParser.parseRewardType("VOUCHER"));
    }

    @Test
    void parseRewardStatus_andVoucherPoolStatus() {
        assertNull(RewardEnumParser.parseRewardStatus(""));
        assertEquals(RewardStatus.REDEEMED, RewardEnumParser.parseRewardStatus("REDEEMED"));
        assertEquals(VoucherPoolStatus.AVAILABLE, RewardEnumParser.parseVoucherPoolStatus("AVAILABLE"));
    }

    @Test
    void parseRewardType_invalid_throws() {
        assertThrows(IllegalArgumentException.class, () -> RewardEnumParser.parseRewardType("NOT_A_TYPE"));
    }

    @Test
    void parseRewardStatus_invalid_throws() {
        assertThrows(IllegalArgumentException.class, () -> RewardEnumParser.parseRewardStatus("GONE"));
    }

    @Test
    void parseVoucherPoolStatus_invalid_throws() {
        assertThrows(IllegalArgumentException.class, () -> RewardEnumParser.parseVoucherPoolStatus("BAD"));
    }
}