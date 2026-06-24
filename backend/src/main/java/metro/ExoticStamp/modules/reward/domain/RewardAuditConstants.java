package metro.ExoticStamp.modules.reward.domain;

public final class RewardAuditConstants {

    private RewardAuditConstants() {}

    public static final String TABLE_MILESTONES = "milestones";
    public static final String TABLE_VOUCHER_POOL = "voucher_pool";
    public static final String TABLE_USER_REWARDS = "user_rewards";

    public static final String REWARD_ISSUED = "REWARD_ISSUED";
    public static final String REWARD_PENDING_STOCK = "REWARD_PENDING_STOCK";
    public static final String VOUCHER_ALLOCATED = "VOUCHER_ALLOCATED";
    public static final String VOUCHER_STOCK_EMPTY = "VOUCHER_STOCK_EMPTY";
    public static final String MILESTONE_CREATED = "MILESTONE_CREATED";
    public static final String MILESTONE_UPDATED = "MILESTONE_UPDATED";
    public static final String MILESTONE_DISABLED = "MILESTONE_DISABLED";
    public static final String VOUCHER_IMPORTED = "VOUCHER_IMPORTED";
    public static final String VOUCHER_DISABLED = "VOUCHER_DISABLED";
}
