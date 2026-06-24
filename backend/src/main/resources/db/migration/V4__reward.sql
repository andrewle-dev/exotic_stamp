-- Reward module (UUID-first)
CREATE TABLE partners (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(100) NOT NULL,
    logo_url            VARCHAR(255),
    contact_email       VARCHAR(100),
    contract_start_date DATE,
    contract_end_date   DATE,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_by          VARCHAR(255),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by          VARCHAR(255),
    updated_at          TIMESTAMP
);

CREATE INDEX idx_partners_is_active ON partners (is_active);

ALTER TABLE campaigns
    ADD CONSTRAINT fk_campaigns_partner_id
        FOREIGN KEY (partner_id) REFERENCES partners (id)
            ON DELETE SET NULL;

CREATE TABLE milestones (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    line_id         UUID,
    campaign_id     UUID,
    stamps_required INT NOT NULL,
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(255),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_by      VARCHAR(255),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by      VARCHAR(255),
    updated_at      TIMESTAMP,

    CONSTRAINT fk_milestones_line_id
        FOREIGN KEY (line_id) REFERENCES lines (id)
            ON DELETE SET NULL,

    CONSTRAINT fk_milestones_campaign_id
        FOREIGN KEY (campaign_id) REFERENCES campaigns (id)
            ON DELETE SET NULL
);

CREATE INDEX idx_milestones_line_stamps ON milestones (line_id, stamps_required);
CREATE INDEX idx_milestones_active ON milestones (is_active);

CREATE TABLE rewards (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    milestone_id    UUID NOT NULL,
    partner_id      UUID,
    reward_type     VARCHAR(20) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(255),
    value_amount    DECIMAL(10, 2),
    expiry_days     INT,
    total_stock     INT,
    issued_count    INT NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_by      VARCHAR(255),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by      VARCHAR(255),
    updated_at      TIMESTAMP,

    CONSTRAINT fk_rewards_milestone_id
        FOREIGN KEY (milestone_id) REFERENCES milestones (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_rewards_partner_id
        FOREIGN KEY (partner_id) REFERENCES partners (id)
            ON DELETE SET NULL
);

CREATE INDEX idx_rewards_milestone_active ON rewards (milestone_id, is_active);
CREATE INDEX idx_rewards_partner_id ON rewards (partner_id) WHERE partner_id IS NOT NULL;

CREATE TABLE voucher_pool (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reward_id   UUID NOT NULL,
    code        VARCHAR(50) NOT NULL,
    is_redeemed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_vp_reward_id
        FOREIGN KEY (reward_id) REFERENCES rewards (id)
            ON DELETE RESTRICT,

    CONSTRAINT uq_voucher_pool_code UNIQUE (code)
);

CREATE INDEX idx_vp_available ON voucher_pool (reward_id, is_redeemed) WHERE is_redeemed = FALSE;

CREATE TABLE user_rewards (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL,
    reward_id       UUID NOT NULL,
    milestone_id    UUID NOT NULL,
    voucher_pool_id UUID,
    issued_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMP,
    redeemed_at     TIMESTAMP,
    status          VARCHAR(20) NOT NULL DEFAULT 'ISSUED',

    CONSTRAINT fk_ur_reward_id
        FOREIGN KEY (reward_id) REFERENCES rewards (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_ur_milestone_id
        FOREIGN KEY (milestone_id) REFERENCES milestones (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_ur_voucher_pool_id
        FOREIGN KEY (voucher_pool_id) REFERENCES voucher_pool (id)
            ON DELETE SET NULL,

    CONSTRAINT uq_user_rewards_once UNIQUE (user_id, milestone_id)
);

CREATE INDEX idx_ur_user_id ON user_rewards (user_id);
CREATE INDEX idx_ur_status ON user_rewards (user_id, status);
