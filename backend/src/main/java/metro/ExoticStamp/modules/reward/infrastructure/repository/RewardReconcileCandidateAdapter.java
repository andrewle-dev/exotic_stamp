package metro.ExoticStamp.modules.reward.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.reward.application.port.RewardReconcileCandidatePort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RewardReconcileCandidateAdapter implements RewardReconcileCandidatePort {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<UserCampaignPair> findMissingRewardCandidates(Duration lookback, int limit) {
        LocalDateTime since = LocalDateTime.now().minus(lookback);
        int safeLimit = Math.max(1, Math.min(limit, 500));
        String sql = """
                SELECT user_id, campaign_id
                FROM (
                    SELECT us.user_id, us.campaign_id
                    FROM user_stamps us
                    INNER JOIN milestones m
                        ON m.campaign_id = us.campaign_id
                       AND m.status = 'ACTIVE'
                       AND m.deleted_at IS NULL
                    WHERE us.collected_at >= ?
                      AND us.campaign_id IS NOT NULL
                    GROUP BY us.user_id, us.campaign_id, m.id, m.stamps_required
                    HAVING COUNT(DISTINCT us.station_id) >= m.stamps_required
                       AND NOT EXISTS (
                            SELECT 1 FROM user_rewards ur
                            WHERE ur.user_id = us.user_id
                              AND ur.milestone_id = m.id
                       )
                ) missing
                GROUP BY user_id, campaign_id
                ORDER BY user_id, campaign_id
                LIMIT ?
                """;
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new UserCampaignPair(
                        (UUID) rs.getObject("user_id"),
                        (UUID) rs.getObject("campaign_id")),
                since,
                safeLimit);
    }

    @Override
    @Transactional
    public List<UUID> claimPendingStockRewardIds(Duration lookback, int limit) {
        return queryPending(lookback, limit, true);
    }

    @Override
    public List<UUID> peekPendingStockRewardIds(Duration lookback, int limit) {
        return queryPending(lookback, limit, false);
    }

    private List<UUID> queryPending(Duration lookback, int limit, boolean forUpdate) {
        LocalDateTime since = LocalDateTime.now().minus(lookback);
        int safeLimit = Math.max(1, Math.min(limit, 500));
        String sql;
        if (forUpdate) {
            sql = """
                    SELECT ur.id
                    FROM user_rewards ur
                    INNER JOIN milestones m ON m.id = ur.milestone_id
                    WHERE ur.status = 'PENDING_STOCK'
                      AND ur.voucher_pool_id IS NULL
                      AND ur.issued_at >= ?
                      AND m.status = 'ACTIVE'
                      AND m.deleted_at IS NULL
                      AND m.reward_type = 'VOUCHER'
                    ORDER BY ur.issued_at
                    FOR UPDATE OF ur SKIP LOCKED
                    LIMIT ?
                    """;
        } else {
            sql = """
                    SELECT ur.id
                    FROM user_rewards ur
                    INNER JOIN milestones m ON m.id = ur.milestone_id
                    WHERE ur.status = 'PENDING_STOCK'
                      AND ur.voucher_pool_id IS NULL
                      AND ur.issued_at >= ?
                      AND m.status = 'ACTIVE'
                      AND m.deleted_at IS NULL
                      AND m.reward_type = 'VOUCHER'
                    ORDER BY ur.issued_at
                    LIMIT ?
                    """;
        }
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> (UUID) rs.getObject("id"),
                since,
                safeLimit);
    }
}
