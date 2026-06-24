-- Community module (UUID-first)
CREATE TABLE referral_codes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL,
    code            VARCHAR(20) NOT NULL,
    total_referrals INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_referral_codes_user_id UNIQUE (user_id),
    CONSTRAINT uq_referral_codes_code UNIQUE (code)
);

CREATE INDEX idx_rc_code ON referral_codes (code);
CREATE INDEX idx_rc_user_id ON referral_codes (user_id);

CREATE TABLE referrals (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    referrer_user_id    UUID NOT NULL,
    referred_user_id    UUID NOT NULL,
    referral_code_id    UUID NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    referred_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at        TIMESTAMP,
    reward_issued_at    TIMESTAMP,

    CONSTRAINT fk_r_referral_code_id
        FOREIGN KEY (referral_code_id) REFERENCES referral_codes (id)
            ON DELETE RESTRICT,

    CONSTRAINT uq_referrals_referred_user UNIQUE (referred_user_id),
    CONSTRAINT chk_referrals_status CHECK (status IN ('PENDING', 'COMPLETED', 'REWARDED')),
    CONSTRAINT chk_referrals_no_self_refer CHECK (referrer_user_id != referred_user_id)
);

CREATE INDEX idx_r_referrer_user_id ON referrals (referrer_user_id);
CREATE INDEX idx_r_referred_user_id ON referrals (referred_user_id);
CREATE INDEX idx_r_leaderboard ON referrals (referrer_user_id, status);

CREATE TABLE share_events (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL,
    stamp_id    UUID,
    platform    VARCHAR(20) NOT NULL,
    shared_at   TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_se_stamp_id
        FOREIGN KEY (stamp_id) REFERENCES user_stamps (id)
            ON DELETE SET NULL,

    CONSTRAINT chk_se_platform CHECK (platform IN ('FACEBOOK', 'INSTAGRAM', 'ZALO', 'TIKTOK'))
);

CREATE INDEX idx_se_user_id ON share_events (user_id);
CREATE INDEX idx_se_shared_at ON share_events (user_id, shared_at DESC);
CREATE INDEX idx_se_platform ON share_events (platform);

CREATE TABLE notifications (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL,
    title           VARCHAR(100) NOT NULL,
    body            VARCHAR(500) NOT NULL,
    type            VARCHAR(30) NOT NULL,
    reference_id    VARCHAR(100),
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    read_at         TIMESTAMP,

    CONSTRAINT chk_notifications_type CHECK (type IN ('MILESTONE', 'REWARD', 'CAMPAIGN', 'REFERRAL', 'SYSTEM'))
);

CREATE INDEX idx_n_user_unread ON notifications (user_id, is_read) WHERE is_read = FALSE;
CREATE INDEX idx_n_user_id ON notifications (user_id);
CREATE INDEX idx_n_created_at ON notifications (user_id, created_at DESC);
