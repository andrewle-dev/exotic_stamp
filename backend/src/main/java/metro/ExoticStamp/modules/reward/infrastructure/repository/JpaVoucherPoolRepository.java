package metro.ExoticStamp.modules.reward.infrastructure.repository;

import metro.ExoticStamp.modules.reward.domain.model.VoucherPool;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaVoucherPoolRepository extends JpaRepository<VoucherPool, UUID> {

    Optional<VoucherPool> findByCode(String code);

    @Query(value = """
            SELECT vp.* FROM voucher_pool vp
            WHERE vp.milestone_id = :milestoneId
              AND vp.status = 'AVAILABLE'
              AND (vp.expires_at IS NULL OR vp.expires_at > NOW())
              AND NOT EXISTS (SELECT 1 FROM user_rewards ur WHERE ur.voucher_pool_id = vp.id)
            ORDER BY vp.created_at ASC
            LIMIT 1
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    Optional<VoucherPool> lockNextAvailableForMilestone(@Param("milestoneId") UUID milestoneId);

    @Query(value = """
            SELECT vp.* FROM voucher_pool vp
            WHERE vp.reward_id = :rewardId AND vp.is_redeemed = FALSE
            AND NOT EXISTS (SELECT 1 FROM user_rewards ur WHERE ur.voucher_pool_id = vp.id)
            ORDER BY vp.created_at ASC
            LIMIT 1
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    Optional<VoucherPool> lockNextAvailable(@Param("rewardId") UUID rewardId);

    @Query(value = """
            SELECT COUNT(*) FROM voucher_pool vp
            WHERE vp.milestone_id = :milestoneId
              AND vp.status = 'AVAILABLE'
              AND (vp.expires_at IS NULL OR vp.expires_at > NOW())
              AND NOT EXISTS (SELECT 1 FROM user_rewards ur WHERE ur.voucher_pool_id = vp.id)
            """, nativeQuery = true)
    long countUnissuedAvailableByMilestoneId(@Param("milestoneId") UUID milestoneId);

    @Query(value = """
            SELECT COUNT(*) FROM voucher_pool vp
            WHERE vp.reward_id = :rewardId AND vp.is_redeemed = FALSE
              AND NOT EXISTS (SELECT 1 FROM user_rewards ur WHERE ur.voucher_pool_id = vp.id)
            """, nativeQuery = true)
    long countUnissuedAvailableByRewardId(@Param("rewardId") UUID rewardId);

    @Query(value = """
            SELECT COUNT(*) FROM voucher_pool vp
            WHERE vp.reward_id = :rewardId AND vp.is_redeemed = TRUE
            """, nativeQuery = true)
    long countRedeemedByRewardId(@Param("rewardId") UUID rewardId);

    @Query("""
            SELECT vp FROM VoucherPool vp
            WHERE (:milestoneId IS NULL OR vp.milestoneId = :milestoneId)
              AND (:status IS NULL OR vp.status = :status)
            ORDER BY vp.createdAt DESC
            """)
    Page<VoucherPool> findFiltered(
            @Param("milestoneId") UUID milestoneId,
            @Param("status") metro.ExoticStamp.modules.reward.domain.model.VoucherPoolStatus status,
            Pageable pageable
    );
}
