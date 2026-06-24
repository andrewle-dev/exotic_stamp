package metro.ExoticStamp.modules.reward.domain.model;

public enum RewardType {
    DIGITAL_BADGE,
    DIGITAL_STICKER,
    VOUCHER,
    PHYSICAL_GIFT_PLACEHOLDER,
    /** @deprecated Legacy V4 value; mapped to DIGITAL_BADGE in migrations. */
    @Deprecated
    BONUS_STAMP
}
