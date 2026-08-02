-- Batch E: integrity constraints for voucher linking and soft-delete-aware default campaigns.
-- Additive / forward-only. Does not edit prior migrations.
--
-- Dirty-data analysis (pre-apply):
-- 1) user_rewards.voucher_pool_id duplicates: none expected if app always set voucher via
--    VoucherAllocationService + SKIP LOCKED. Audit:
--      SELECT voucher_pool_id, COUNT(*) FROM user_rewards
--      WHERE voucher_pool_id IS NOT NULL GROUP BY 1 HAVING COUNT(*) > 1;
-- 2) Soft-deleted default campaigns holding uq_campaigns_default_per_line:
--      SELECT line_id, COUNT(*) FROM campaigns
--      WHERE is_default AND line_id IS NOT NULL AND deleted_at IS NOT NULL GROUP BY 1;
--    Safe: drop+recreate unique to exclude deleted rows; existing soft-deleted defaults no longer block.

-- ---------------------------------------------------------------------------
-- R-P1-02: one voucher_pool_id may link to at most one user_reward
-- ---------------------------------------------------------------------------
CREATE UNIQUE INDEX IF NOT EXISTS uq_user_rewards_voucher_pool_id
    ON user_rewards (voucher_pool_id)
    WHERE voucher_pool_id IS NOT NULL;

-- ---------------------------------------------------------------------------
-- R-P1-09: soft-delete-aware default campaign uniqueness
-- ---------------------------------------------------------------------------
DROP INDEX IF EXISTS uq_campaigns_default_per_line;

CREATE UNIQUE INDEX uq_campaigns_default_per_line
    ON campaigns (line_id)
    WHERE is_default = TRUE
      AND line_id IS NOT NULL
      AND deleted_at IS NULL;
