package metro.ExoticStamp.modules.reward.infrastructure.repository;

import metro.ExoticStamp.modules.reward.domain.model.UserReward;
import metro.ExoticStamp.modules.reward.domain.model.RewardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface JpaUserRewardRepository extends JpaRepository<UserReward, UUID> {

    Optional<UserReward> findByUserIdAndId(UUID userId, UUID id);

    boolean existsByUserIdAndMilestoneId(UUID userId, UUID milestoneId);

    @Query("SELECT DISTINCT ur.milestoneId FROM UserReward ur WHERE ur.userId = :userId")
    Set<UUID> findDistinctMilestoneIdsByUserId(@Param("userId") UUID userId);

    Page<UserReward> findByUserIdOrderByIssuedAtDesc(UUID userId, Pageable pageable);

    Page<UserReward> findByUserIdAndStatusOrderByIssuedAtDesc(UUID userId, RewardStatus status, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE user_rewards SET status = 'EXPIRED'
            WHERE status = 'ISSUED' AND expires_at IS NOT NULL AND expires_at < :now
            """, nativeQuery = true)
    int expireIssuedBefore(@Param("now") LocalDateTime now);
}
