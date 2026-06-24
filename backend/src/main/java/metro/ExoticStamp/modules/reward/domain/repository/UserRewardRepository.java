package metro.ExoticStamp.modules.reward.domain.repository;

import metro.ExoticStamp.modules.reward.domain.model.PagedSlice;
import metro.ExoticStamp.modules.reward.domain.model.RewardStatus;
import metro.ExoticStamp.modules.reward.domain.model.UserReward;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserRewardRepository {

    UserReward save(UserReward userReward);

    Optional<UserReward> findByUserIdAndId(UUID userId, UUID id);

    Set<UUID> findMilestoneIdsRewardedForUser(UUID userId);

    boolean existsByUserIdAndMilestoneId(UUID userId, UUID milestoneId);

    PagedSlice<UserReward> findByUserIdOrderByIssuedAtDesc(UUID userId, int page, int size);

    PagedSlice<UserReward> findByUserIdAndStatusOrderByIssuedAtDesc(UUID userId, RewardStatus status, int page, int size);

    /**
     * @return number of rows updated
     */
    int expireIssuedBefore(LocalDateTime now);
}
