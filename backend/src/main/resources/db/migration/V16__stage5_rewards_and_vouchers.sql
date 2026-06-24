-- Stage 5: Milestone-centric rewards and voucher allocation
-- Baseline V4 rewards schema. Do not edit V1–V15.

-- ---------------------------------------------------------------------------
-- milestones: reward metadata on milestone
-- ---------------------------------------------------------------------------
ALTER TABLE milestones
    ADD COLUMN IF NOT EXISTS code VARCHAR(50),
    ADD COLUMN IF NOT EXISTS reward_type VARCHAR(30),
    ADD COLUMN IF NOT EXISTS reward_title VARCHAR(100),
    ADD COLUMN IF NOT EXISTS reward_description VARCHAR(255),
    ADD COLUMN IF NOT EXISTS reward_image_url VARCHAR(255),
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

UPDATE milestones m
SET code = COALESCE(m.code, 'MILESTONE_' || REPLACE(m.id::text, '-', '')),
    reward_type = COALESCE(m.reward_type, r.reward_type, 'DIGITAL_STICKER'),
    reward_title = COALESCE(m.reward_title, r.name, m.name),
    reward_description = COALESCE(m.reward_description, r.description, m.description),
    status = CASE
        WHEN m.deleted_at IS NOT NULL THEN 'INACTIVE'
        WHEN m.is_active = TRUE THEN 'ACTIVE'
        ELSE 'INACTIVE'
    END,
    sort_order = COALESCE(m.sort_order, 0)
FROM rewards r
WHERE r.milestone_id = m.id;

UPDATE milestones m
SET code = COALESCE(m.code, 'MILESTONE_' || REPLACE(m.id::text, '-', '')),
    reward_type = COALESCE(m.reward_type, 'DIGITAL_STICKER'),
    reward_title = COALESCE(m.reward_title, m.name),
    status = CASE WHEN m.is_active = TRUE THEN 'ACTIVE' ELSE 'INACTIVE' END
WHERE NOT EXISTS (SELECT 1 FROM rewards r WHERE r.milestone_id = m.id);

ALTER TABLE milestones ALTER COLUMN code SET NOT NULL;
ALTER TABLE milestones ALTER COLUMN reward_type SET NOT NULL;
ALTER TABLE milestones ALTER COLUMN reward_title SET NOT NULL;

ALTER TABLE milestones DROP CONSTRAINT IF EXISTS chk_milestones_status;
ALTER TABLE milestones ADD CONSTRAINT chk_milestones_status
    CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE', 'ARCHIVED'));

ALTER TABLE milestones DROP CONSTRAINT IF EXISTS chk_milestones_reward_type;
ALTER TABLE milestones ADD CONSTRAINT chk_milestones_reward_type
    CHECK (reward_type IN ('DIGITAL_BADGE', 'DIGITAL_STICKER', 'VOUCHER', 'PHYSICAL_GIFT_PLACEHOLDER', 'BONUS_STAMP'));

ALTER TABLE milestones DROP CONSTRAINT IF EXISTS chk_milestones_sort_order;
ALTER TABLE milestones ADD CONSTRAINT chk_milestones_sort_order
    CHECK (sort_order >= 0);

CREATE UNIQUE INDEX IF NOT EXISTS uq_milestones_campaign_code
    ON milestones (campaign_id, code)
    WHERE deleted_at IS NULL AND campaign_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_milestones_campaign_status_stamps
    ON milestones (campaign_id, status, stamps_required)
    WHERE deleted_at IS NULL;

-- ---------------------------------------------------------------------------
-- voucher_pool: milestone-scoped allocation
-- ---------------------------------------------------------------------------
ALTER TABLE voucher_pool
    ADD COLUMN IF NOT EXISTS milestone_id UUID,
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    ADD COLUMN IF NOT EXISTS assigned_user_id UUID,
    ADD COLUMN IF NOT EXISTS assigned_user_reward_id UUID,
    ADD COLUMN IF NOT EXISTS assigned_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

UPDATE voucher_pool vp
SET milestone_id = r.milestone_id,
    status = CASE
        WHEN vp.is_redeemed = TRUE OR EXISTS (
            SELECT 1 FROM user_rewards ur WHERE ur.voucher_pool_id = vp.id
        ) THEN 'CLAIMED'
        ELSE 'AVAILABLE'
    END
FROM rewards r
WHERE vp.reward_id = r.id AND vp.milestone_id IS NULL;

UPDATE voucher_pool vp
SET assigned_user_id = ur.user_id,
    assigned_user_reward_id = ur.id,
    assigned_at = ur.issued_at
FROM user_rewards ur
WHERE ur.voucher_pool_id = vp.id
  AND vp.assigned_user_reward_id IS NULL;

ALTER TABLE voucher_pool DROP CONSTRAINT IF EXISTS chk_voucher_pool_status;
ALTER TABLE voucher_pool ADD CONSTRAINT chk_voucher_pool_status
    CHECK (status IN ('AVAILABLE', 'RESERVED', 'CLAIMED', 'EXPIRED', 'DISABLED'));

ALTER TABLE voucher_pool DROP CONSTRAINT IF EXISTS fk_vp_milestone_id;
ALTER TABLE voucher_pool ADD CONSTRAINT fk_vp_milestone_id
    FOREIGN KEY (milestone_id) REFERENCES milestones (id) ON DELETE RESTRICT;

ALTER TABLE voucher_pool ALTER COLUMN reward_id DROP NOT NULL;

CREATE INDEX IF NOT EXISTS idx_vp_milestone_status_expires
    ON voucher_pool (milestone_id, status, expires_at)
    WHERE milestone_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_vp_assigned_user_reward
    ON voucher_pool (assigned_user_reward_id)
    WHERE assigned_user_reward_id IS NOT NULL;

-- ---------------------------------------------------------------------------
-- user_rewards: campaign denormalization + extended status
-- ---------------------------------------------------------------------------
ALTER TABLE user_rewards
    ADD COLUMN IF NOT EXISTS campaign_id UUID;

UPDATE user_rewards ur
SET campaign_id = m.campaign_id
FROM milestones m
WHERE ur.milestone_id = m.id AND ur.campaign_id IS NULL;

ALTER TABLE user_rewards ALTER COLUMN reward_id DROP NOT NULL;

ALTER TABLE user_rewards DROP CONSTRAINT IF EXISTS chk_user_rewards_status;
ALTER TABLE user_rewards ADD CONSTRAINT chk_user_rewards_status
    CHECK (status IN ('ISSUED', 'PENDING_STOCK', 'FAILED', 'CANCELLED', 'REDEEMED', 'EXPIRED'));

CREATE INDEX IF NOT EXISTS idx_user_rewards_user_campaign
    ON user_rewards (user_id, campaign_id)
    WHERE campaign_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_user_rewards_user_milestone
    ON user_rewards (user_id, milestone_id);

-- ---------------------------------------------------------------------------
-- RBAC: Stage 5 permissions
-- ---------------------------------------------------------------------------
INSERT INTO permissions (permission, description, version)
VALUES
    ('REWARD_MILESTONE_MANAGE', 'Manage reward milestones (admin)', 0),
    ('VOUCHER_POOL_MANAGE', 'Manage voucher pool import and allocation (admin)', 0)
ON CONFLICT (permission) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.role = 'ADMIN'
  AND p.permission IN ('REWARD_MILESTONE_MANAGE', 'VOUCHER_POOL_MANAGE')
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
