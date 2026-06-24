-- Stage 6 Community MVP: referral code status, share event fields, notification deep links

ALTER TABLE referral_codes
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE referral_codes
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

UPDATE referral_codes SET status = 'ACTIVE' WHERE status IS NULL;

ALTER TABLE referral_codes DROP CONSTRAINT IF EXISTS chk_referral_codes_status;
ALTER TABLE referral_codes ADD CONSTRAINT chk_referral_codes_status
    CHECK (status IN ('ACTIVE', 'INACTIVE'));

ALTER TABLE share_events
    ADD COLUMN IF NOT EXISTS share_type VARCHAR(30) NOT NULL DEFAULT 'OTHER';

ALTER TABLE share_events
    ADD COLUMN IF NOT EXISTS target_id UUID;

ALTER TABLE share_events
    ADD COLUMN IF NOT EXISTS metadata JSONB;

ALTER TABLE share_events DROP CONSTRAINT IF EXISTS chk_se_platform;
ALTER TABLE share_events ADD CONSTRAINT chk_se_platform
    CHECK (platform IN ('FACEBOOK', 'INSTAGRAM', 'ZALO', 'TIKTOK', 'COPY_LINK', 'OTHER'));

ALTER TABLE share_events DROP CONSTRAINT IF EXISTS chk_se_share_type;
ALTER TABLE share_events ADD CONSTRAINT chk_se_share_type
    CHECK (share_type IN ('STAMP_BOOK', 'REWARD', 'STATION', 'REFERRAL', 'OTHER'));

ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS deep_link VARCHAR(500);

ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS metadata JSONB;

CREATE INDEX IF NOT EXISTS idx_se_share_type ON share_events (user_id, share_type);
