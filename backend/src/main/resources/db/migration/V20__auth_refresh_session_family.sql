-- Refresh session lineage / concurrency fields on access_tokens (REFRESH rows).
-- Existing rows: family_id = id (self-family), version = 0.

ALTER TABLE access_tokens
    ADD COLUMN IF NOT EXISTS token_family_id UUID,
    ADD COLUMN IF NOT EXISTS parent_token_id UUID,
    ADD COLUMN IF NOT EXISTS replaced_by_token_id UUID,
    ADD COLUMN IF NOT EXISTS used_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS client_platform VARCHAR(20),
    ADD COLUMN IF NOT EXISTS user_agent_hash VARCHAR(64),
    ADD COLUMN IF NOT EXISTS ip_hash VARCHAR(64),
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

UPDATE access_tokens
SET token_family_id = id
WHERE token_family_id IS NULL;

ALTER TABLE access_tokens
    ALTER COLUMN token_family_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_access_tokens_family
    ON access_tokens (token_family_id);

CREATE INDEX IF NOT EXISTS idx_access_tokens_user_family
    ON access_tokens (user_id, token_family_id);
