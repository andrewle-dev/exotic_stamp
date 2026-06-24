package metro.ExoticStamp.modules.reward.domain.event;

import metro.ExoticStamp.modules.reward.domain.model.RewardType;

import java.util.UUID;

/**
 * Published after successful user reward issuance (after transaction commit). Immutable POJO.
 * Milestone-centric issuance uses {@link #userRewardId} and {@link #milestoneId}; {@link #legacyRewardId} is null.
 */
public final class RewardIssuedEvent {

    private final UUID userId;
    private final UUID userRewardId;
    private final UUID milestoneId;
    private final RewardType rewardType;
    /** Legacy {@code rewards.id}; null for Stage 5 milestone-centric issuance. */
    private final UUID legacyRewardId;

    public RewardIssuedEvent(
            UUID userId,
            UUID userRewardId,
            UUID milestoneId,
            RewardType rewardType,
            UUID legacyRewardId
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (userRewardId == null) {
            throw new IllegalArgumentException("userRewardId must not be null");
        }
        if (milestoneId == null) {
            throw new IllegalArgumentException("milestoneId must not be null");
        }
        if (rewardType == null) {
            throw new IllegalArgumentException("rewardType must not be null");
        }
        this.userId = userId;
        this.userRewardId = userRewardId;
        this.milestoneId = milestoneId;
        this.rewardType = rewardType;
        this.legacyRewardId = legacyRewardId;
    }

    public static RewardIssuedEvent milestoneIssued(
            UUID userId,
            UUID userRewardId,
            UUID milestoneId,
            RewardType rewardType
    ) {
        return new RewardIssuedEvent(userId, userRewardId, milestoneId, rewardType, null);
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getUserRewardId() {
        return userRewardId;
    }

    public UUID getMilestoneId() {
        return milestoneId;
    }

    public RewardType getRewardType() {
        return rewardType;
    }

    public UUID getLegacyRewardId() {
        return legacyRewardId;
    }

    /**
     * @deprecated Use {@link #getUserRewardId()} and {@link #getMilestoneId()}. Null for milestone-centric issuance.
     */
    @Deprecated
    public UUID getRewardId() {
        return legacyRewardId;
    }

    /**
     * @deprecated No longer populated; always null for Stage 5+ events.
     */
    @Deprecated
    public UUID getLineId() {
        return null;
    }
}
