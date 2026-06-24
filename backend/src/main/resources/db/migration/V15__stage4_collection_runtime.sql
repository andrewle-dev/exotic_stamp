-- Stage 4: Collection runtime metadata on user_stamps
-- Baseline is V3/V9/V10 user_stamps. Do not edit V1–V14.

ALTER TABLE user_stamps
    ADD COLUMN IF NOT EXISTS source_scan_type VARCHAR(30),
    ADD COLUMN IF NOT EXISTS device_platform VARCHAR(20),
    ADD COLUMN IF NOT EXISTS app_version VARCHAR(50),
    ADD COLUMN IF NOT EXISTS gps_distance_meters DECIMAL(10, 2),
    ADD COLUMN IF NOT EXISTS gps_accuracy_meters DECIMAL(10, 2),
    ADD COLUMN IF NOT EXISTS collection_policy VARCHAR(50) NOT NULL DEFAULT 'MVP_ONCE_PER_STATION_CAMPAIGN';

UPDATE user_stamps
SET source_scan_type = UPPER(collect_method::text)
WHERE source_scan_type IS NULL AND collect_method IS NOT NULL;

UPDATE user_stamps
SET collection_policy = 'MVP_ONCE_PER_STATION_CAMPAIGN'
WHERE collection_policy IS NULL;

ALTER TABLE user_stamps DROP CONSTRAINT IF EXISTS chk_user_stamps_source_scan_type;
ALTER TABLE user_stamps ADD CONSTRAINT chk_user_stamps_source_scan_type
    CHECK (source_scan_type IS NULL OR source_scan_type IN ('NFC', 'QR_STATIC', 'QR_DYNAMIC_PLACEHOLDER'));

ALTER TABLE user_stamps DROP CONSTRAINT IF EXISTS chk_user_stamps_collection_policy;
ALTER TABLE user_stamps ADD CONSTRAINT chk_user_stamps_collection_policy
    CHECK (collection_policy IN ('MVP_ONCE_PER_STATION_CAMPAIGN'));

CREATE INDEX IF NOT EXISTS idx_user_stamps_user_campaign
    ON user_stamps (user_id, campaign_id)
    WHERE campaign_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_user_stamps_campaign_station
    ON user_stamps (campaign_id, station_id)
    WHERE campaign_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_user_stamps_user_idempotency
    ON user_stamps (user_id, idempotency_key)
    WHERE idempotency_key IS NOT NULL;
