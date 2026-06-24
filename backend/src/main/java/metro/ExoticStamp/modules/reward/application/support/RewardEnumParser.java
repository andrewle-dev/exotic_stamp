package metro.ExoticStamp.modules.reward.application.support;

import metro.ExoticStamp.modules.reward.domain.model.MilestoneStatus;
import metro.ExoticStamp.modules.reward.domain.model.RewardStatus;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPoolStatus;

public final class RewardEnumParser {

    private RewardEnumParser() {
    }

    public static MilestoneStatus parseMilestoneStatus(String value) {
        return value == null || value.isBlank() ? null : MilestoneStatus.valueOf(value);
    }

    public static RewardType parseRewardType(String value) {
        return value == null || value.isBlank() ? null : RewardType.valueOf(value);
    }

    public static RewardStatus parseRewardStatus(String value) {
        return value == null || value.isBlank() ? null : RewardStatus.valueOf(value);
    }

    public static VoucherPoolStatus parseVoucherPoolStatus(String value) {
        return value == null || value.isBlank() ? null : VoucherPoolStatus.valueOf(value);
    }
}
