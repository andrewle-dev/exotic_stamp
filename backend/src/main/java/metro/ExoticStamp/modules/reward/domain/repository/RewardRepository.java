package metro.ExoticStamp.modules.reward.domain.repository;

import metro.ExoticStamp.modules.reward.domain.model.PagedSlice;
import metro.ExoticStamp.modules.reward.domain.model.Reward;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RewardRepository {

    Optional<Reward> findById(UUID id);

    Reward save(Reward reward);

    Optional<Reward> findActiveByMilestoneId(UUID milestoneId);

    /**
     * @return true if a row was updated (stock allowed)
     */
    boolean incrementIssuedCountIfStockAllows(UUID rewardId);

    /**
     * Compensates {@link #incrementIssuedCountIfStockAllows} when user_reward insert hits unique constraint.
     */
    void decrementIssuedCount(UUID rewardId);

    PagedSlice<Reward> findAllPaged(Boolean activeOnly, int page, int size);

    List<Reward> findAllByIds(Iterable<UUID> rewardIds);

    boolean existsById(UUID id);
}
