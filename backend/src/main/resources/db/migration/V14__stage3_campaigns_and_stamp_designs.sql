-- Stage 3: Campaign configuration + stamp design management
-- Baseline is V3/V9 collection schema. Do not edit V1–V13.

-- =============================================================================
-- campaigns: evolve to Stage 3 model
-- =============================================================================

ALTER TABLE campaigns RENAME COLUMN start_date TO start_at;
ALTER TABLE campaigns RENAME COLUMN end_date TO end_at;
ALTER TABLE campaigns RENAME COLUMN banner_url TO banner_image_url;

ALTER TABLE campaigns
    ADD COLUMN IF NOT EXISTS display_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS campaign_type VARCHAR(20),
    ADD COLUMN IF NOT EXISTS status VARCHAR(20),
    ADD COLUMN IF NOT EXISTS thumbnail_image_url VARCHAR(255),
    ADD COLUMN IF NOT EXISTS priority INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

UPDATE campaigns
SET display_name = name
WHERE display_name IS NULL;

UPDATE campaigns
SET campaign_type = 'STANDARD'
WHERE campaign_type IS NULL;

UPDATE campaigns
SET status = CASE WHEN is_active = TRUE THEN 'ACTIVE' ELSE 'INACTIVE' END
WHERE status IS NULL;

UPDATE campaigns
SET priority = 0
WHERE priority IS NULL;

ALTER TABLE campaigns
    ALTER COLUMN display_name SET NOT NULL,
    ALTER COLUMN campaign_type SET NOT NULL,
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE campaigns DROP COLUMN IF EXISTS is_active;

DROP INDEX IF EXISTS idx_campaigns_active_dates;

ALTER TABLE campaigns DROP CONSTRAINT IF EXISTS chk_campaigns_dates;
ALTER TABLE campaigns ADD CONSTRAINT chk_campaigns_dates CHECK (end_at > start_at);

ALTER TABLE campaigns DROP CONSTRAINT IF EXISTS chk_campaigns_status;
ALTER TABLE campaigns ADD CONSTRAINT chk_campaigns_status
    CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE', 'ARCHIVED'));

ALTER TABLE campaigns DROP CONSTRAINT IF EXISTS chk_campaigns_type;
ALTER TABLE campaigns ADD CONSTRAINT chk_campaigns_type
    CHECK (campaign_type IN ('STANDARD', 'SEASONAL', 'EVENT'));

ALTER TABLE campaigns DROP CONSTRAINT IF EXISTS chk_campaigns_priority;
ALTER TABLE campaigns ADD CONSTRAINT chk_campaigns_priority CHECK (priority >= 0);

CREATE INDEX IF NOT EXISTS idx_campaigns_status
    ON campaigns (status)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_campaigns_active_window
    ON campaigns (status, start_at, end_at)
    WHERE status = 'ACTIVE' AND deleted_at IS NULL;

-- =============================================================================
-- stamp_designs: evolve to Stage 3 model
-- =============================================================================

ALTER TABLE stamp_designs RENAME COLUMN artwork_url TO image_url;

ALTER TABLE stamp_designs
    ADD COLUMN IF NOT EXISTS description VARCHAR(500),
    ADD COLUMN IF NOT EXISTS preview_image_url VARCHAR(255),
    ADD COLUMN IF NOT EXISTS rarity VARCHAR(20),
    ADD COLUMN IF NOT EXISTS status VARCHAR(20),
    ADD COLUMN IF NOT EXISTS sort_order INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

UPDATE stamp_designs
SET status = CASE WHEN is_active = TRUE THEN 'ACTIVE' ELSE 'INACTIVE' END
WHERE status IS NULL;

UPDATE stamp_designs
SET rarity = 'COMMON'
WHERE rarity IS NULL;

UPDATE stamp_designs
SET sort_order = 0
WHERE sort_order IS NULL;

ALTER TABLE stamp_designs
    ALTER COLUMN status SET NOT NULL,
    ALTER COLUMN rarity SET NOT NULL;

ALTER TABLE stamp_designs DROP COLUMN IF EXISTS is_active;

DROP INDEX IF EXISTS idx_sd_is_active;

ALTER TABLE stamp_designs DROP CONSTRAINT IF EXISTS chk_stamp_designs_status;
ALTER TABLE stamp_designs ADD CONSTRAINT chk_stamp_designs_status
    CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE'));

ALTER TABLE stamp_designs DROP CONSTRAINT IF EXISTS chk_stamp_designs_rarity;
ALTER TABLE stamp_designs ADD CONSTRAINT chk_stamp_designs_rarity
    CHECK (rarity IN ('COMMON', 'RARE', 'EPIC', 'LEGENDARY'));

ALTER TABLE stamp_designs DROP CONSTRAINT IF EXISTS chk_stamp_designs_sort_order;
ALTER TABLE stamp_designs ADD CONSTRAINT chk_stamp_designs_sort_order CHECK (sort_order >= 0);

CREATE UNIQUE INDEX IF NOT EXISTS uq_stamp_design_active_per_campaign_station
    ON stamp_designs (campaign_id, station_id)
    WHERE status = 'ACTIVE' AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_sd_campaign_station_status
    ON stamp_designs (campaign_id, station_id, status)
    WHERE deleted_at IS NULL;

-- =============================================================================
-- Stage 3 RBAC permissions
-- =============================================================================

INSERT INTO permissions (permission, description, version)
VALUES
    ('CAMPAIGN_MANAGE', 'Manage campaigns and station assignments (admin)', 0),
    ('STAMP_DESIGN_MANAGE', 'Manage stamp designs (admin)', 0)
ON CONFLICT (permission) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.role = 'ADMIN'
  AND p.permission IN ('CAMPAIGN_MANAGE', 'STAMP_DESIGN_MANAGE')
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
