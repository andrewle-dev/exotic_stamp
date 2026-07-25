-- Batch E.1: idempotency fingerprint + notification reward dedup + pending-stock support indexes.
-- Forward-only. Does not edit V1-V22.
--
-- Operator: run docs/deployment/BATCH_E_DATA_PREFLIGHT.sql before applying in shared envs.
-- Legacy user_stamps.idempotency_fingerprint remains NULL; replay compares station/campaign only.

ALTER TABLE user_stamps
    ADD COLUMN IF NOT EXISTS idempotency_fingerprint VARCHAR(64);

COMMENT ON COLUMN user_stamps.idempotency_fingerprint IS
    'SHA-256 hex of canonical COLLECT fingerprint (user|station|campaign|scanType); never stores raw NFC payload';

CREATE UNIQUE INDEX IF NOT EXISTS uq_notifications_user_type_ref
    ON notifications (user_id, type, reference_id)
    WHERE reference_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_user_rewards_pending_stock
    ON user_rewards (status, issued_at)
    WHERE status = 'PENDING_STOCK' AND voucher_pool_id IS NULL;
