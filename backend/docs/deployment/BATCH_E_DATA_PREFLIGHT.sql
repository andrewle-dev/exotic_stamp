-- Batch E / E.1 data preflight (READ-ONLY)
-- Run against shared/staging/prod BEFORE applying V22/V23.
-- Do NOT auto-delete, reassign, or UPDATE production data from this script.
--
-- Operator notes:
-- 1) Any non-empty result for sections 1–6 / 10 must be resolved manually before unique indexes.
-- 2) V22 adds uq_user_rewards_voucher_pool_id and soft-delete-aware default campaign unique.
-- 3) V23 adds nullable idempotency_fingerprint and uq_notifications_user_type_ref.
-- 4) Section 7 lists legacy rows that need fingerprint backfill policy (app compares station/campaign).
-- 5) Sections 8–9 inform pending-stock / issued consistency; reconcile can repair stock gaps after V23.

\echo '=== 1. Duplicate user_rewards.voucher_pool_id assignments ==='
SELECT voucher_pool_id, COUNT(*) AS cnt
FROM user_rewards
WHERE voucher_pool_id IS NOT NULL
GROUP BY voucher_pool_id
HAVING COUNT(*) > 1;

\echo '=== 2. Voucher pool rows linked to inconsistent rewards/users ==='
SELECT vp.id AS voucher_pool_id,
       vp.assigned_user_id,
       vp.assigned_user_reward_id,
       ur.user_id AS reward_user_id,
       ur.voucher_pool_id AS reward_voucher_pool_id
FROM voucher_pool vp
LEFT JOIN user_rewards ur ON ur.id = vp.assigned_user_reward_id
WHERE vp.assigned_user_reward_id IS NOT NULL
  AND (
        ur.id IS NULL
     OR ur.user_id IS DISTINCT FROM vp.assigned_user_id
     OR ur.voucher_pool_id IS DISTINCT FROM vp.id
  );

\echo '=== 3. Duplicate voucher codes ==='
SELECT code, COUNT(*) AS cnt
FROM voucher_pool
GROUP BY code
HAVING COUNT(*) > 1;

\echo '=== 4. Rewards referencing nonexistent voucher rows ==='
SELECT ur.id AS user_reward_id, ur.voucher_pool_id
FROM user_rewards ur
LEFT JOIN voucher_pool vp ON vp.id = ur.voucher_pool_id
WHERE ur.voucher_pool_id IS NOT NULL
  AND vp.id IS NULL;

\echo '=== 5. Multiple active default campaigns on one line ==='
SELECT line_id, COUNT(*) AS cnt
FROM campaigns
WHERE is_default = TRUE
  AND deleted_at IS NULL
  AND line_id IS NOT NULL
GROUP BY line_id
HAVING COUNT(*) > 1;

\echo '=== 6. Existing idempotency-key duplicates (per user) ==='
SELECT user_id, idempotency_key, COUNT(*) AS cnt
FROM user_stamps
WHERE idempotency_key IS NOT NULL
GROUP BY user_id, idempotency_key
HAVING COUNT(*) > 1;

\echo '=== 7. Rows requiring idempotency fingerprint backfill (legacy null) ==='
SELECT COUNT(*) AS legacy_null_fingerprint_count
FROM user_stamps
WHERE idempotency_fingerprint IS NULL;

\echo '=== 8. Rewards in PENDING_STOCK without voucher ==='
SELECT COUNT(*) AS pending_stock_without_voucher
FROM user_rewards
WHERE status = 'PENDING_STOCK'
  AND voucher_pool_id IS NULL;

\echo '=== 9. Rewards marked ISSUED but voucher milestone lacking voucher link ==='
SELECT ur.id, ur.user_id, ur.milestone_id, ur.status, ur.voucher_pool_id
FROM user_rewards ur
INNER JOIN milestones m ON m.id = ur.milestone_id
WHERE ur.status = 'ISSUED'
  AND m.reward_type = 'VOUCHER'
  AND ur.voucher_pool_id IS NULL;

\echo '=== 10. Dangling reward / milestone / user references ==='
SELECT 'user_rewards->users' AS kind, ur.id
FROM user_rewards ur
LEFT JOIN users u ON u.id = ur.user_id
WHERE u.id IS NULL
UNION ALL
SELECT 'user_rewards->milestones', ur.id
FROM user_rewards ur
LEFT JOIN milestones m ON m.id = ur.milestone_id
WHERE m.id IS NULL
UNION ALL
SELECT 'user_stamps->users', us.id
FROM user_stamps us
LEFT JOIN users u ON u.id = us.user_id
WHERE u.id IS NULL;

\echo '=== Preflight complete (read-only) ==='
