-- Idempotency: allow reuse of same key after 1h (no global unique on idempotency_key)
-- Widen key column; drop global unique; add lookup index for time-window queries

ALTER TABLE user_stamps
    ALTER COLUMN idempotency_key TYPE VARCHAR(255);

DROP INDEX IF EXISTS uq_user_stamps_idempotency_key;

CREATE INDEX IF NOT EXISTS idx_user_stamps_idempotency_collected
    ON user_stamps (idempotency_key, collected_at DESC);
