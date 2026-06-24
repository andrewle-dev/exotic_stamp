-- Collection module Phase 1 MVP compliance
-- Baseline is V3__collection.sql. This migration aligns schema to MVP requirements:
-- - idempotency_key unique + not null (UUID string)
-- - scan_method -> collect_method
-- - device_id -> device_fingerprint
-- - default campaign per line support

-- 1) Campaign: support default-per-line campaigns (no FK to metro lines to avoid cross-module coupling)
ALTER TABLE campaigns
    ADD COLUMN IF NOT EXISTS line_id UUID,
    ADD COLUMN IF NOT EXISTS is_default BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_campaigns_line_id ON campaigns (line_id) WHERE line_id IS NOT NULL;

-- Enforce at most one default campaign per line
CREATE UNIQUE INDEX IF NOT EXISTS uq_campaigns_default_per_line
    ON campaigns (line_id)
    WHERE is_default = TRUE AND line_id IS NOT NULL;

-- 2) User stamps: rename columns and add idempotency
ALTER TABLE user_stamps
    RENAME COLUMN scan_method TO collect_method;

ALTER TABLE user_stamps
    RENAME COLUMN device_id TO device_fingerprint;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'collect_method_enum') THEN
        CREATE TYPE collect_method_enum AS ENUM ('NFC', 'QR');
    END IF;
END $$;

UPDATE user_stamps
SET collect_method = UPPER(collect_method)
WHERE collect_method IS NOT NULL;

ALTER TABLE user_stamps
    ALTER COLUMN collect_method TYPE collect_method_enum
    USING collect_method::collect_method_enum;

ALTER TABLE user_stamps
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(36);

-- Backfill existing rows (if any) with generated UUID strings
UPDATE user_stamps
SET idempotency_key = gen_random_uuid()::text
WHERE idempotency_key IS NULL;

ALTER TABLE user_stamps
    ALTER COLUMN idempotency_key SET NOT NULL;

ALTER TABLE user_stamps
    ALTER COLUMN device_fingerprint SET DEFAULT 'unknown';

UPDATE user_stamps
SET device_fingerprint = 'unknown'
WHERE device_fingerprint IS NULL;

ALTER TABLE user_stamps
    ALTER COLUMN device_fingerprint SET NOT NULL;

-- Global uniqueness for idempotency key (authoritative)
CREATE UNIQUE INDEX IF NOT EXISTS uq_user_stamps_idempotency_key ON user_stamps (idempotency_key);

-- Additional read indexes
CREATE INDEX IF NOT EXISTS idx_us_campaign_user ON user_stamps (campaign_id, user_id)
    WHERE campaign_id IS NOT NULL;

