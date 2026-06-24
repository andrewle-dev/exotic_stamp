-- Stage 0 integrity constraints (additive only; do not edit V1-V11).
--
-- Idempotency note (user_stamps.idempotency_key):
-- V10 intentionally removed global UNIQUE to allow key reuse after the app idempotency window (1h).
-- Uniqueness within the sliding window is enforced in application code (CollectionDomainService).
-- Concurrent inserts with the same key may race; no static partial UNIQUE is feasible in PostgreSQL.
--
-- Milestones scope XOR deferred: global milestones (line_id AND campaign_id both NULL) are valid by design.

-- ---------------------------------------------------------------------------
-- CHECK constraints
-- ---------------------------------------------------------------------------

DO $$ BEGIN
    ALTER TABLE users ADD CONSTRAINT chk_users_status
        CHECK (status IS NULL OR status IN ('ACTIVE','INACTIVE','PENDING_VERIFIED','SUSPENDED','BANNED'));
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    ALTER TABLE roles ADD CONSTRAINT chk_roles_status
        CHECK (status IN ('ACTIVE','INACTIVE'));
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    ALTER TABLE user_rewards ADD CONSTRAINT chk_user_rewards_status
        CHECK (status IN ('ISSUED','REDEEMED','EXPIRED'));
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    ALTER TABLE mail_jobs ADD CONSTRAINT chk_mail_jobs_status
        CHECK (status IN ('PENDING','PROCESSING','SENT','FAILED','DEAD'));
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    ALTER TABLE mail_jobs ADD CONSTRAINT chk_mail_jobs_content_type
        CHECK (content_type IN ('HTML','TEXT'));
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    ALTER TABLE rewards ADD CONSTRAINT chk_rewards_reward_type
        CHECK (reward_type IN ('VOUCHER','DIGITAL_STICKER','BONUS_STAMP'));
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    ALTER TABLE milestones ADD CONSTRAINT chk_milestones_stamps_required
        CHECK (stamps_required >= 1);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- ---------------------------------------------------------------------------
-- Foreign keys to users(id)
-- ---------------------------------------------------------------------------

DO $$ BEGIN
    ALTER TABLE user_roles ADD CONSTRAINT fk_user_roles_user_id
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    ALTER TABLE access_tokens ADD CONSTRAINT fk_access_tokens_user_id
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    ALTER TABLE audit_logs ADD CONSTRAINT fk_audit_logs_user_id
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    ALTER TABLE user_stamps ADD CONSTRAINT fk_user_stamps_user_id
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    ALTER TABLE user_rewards ADD CONSTRAINT fk_user_rewards_user_id
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    ALTER TABLE referral_codes ADD CONSTRAINT fk_referral_codes_user_id
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    ALTER TABLE referrals ADD CONSTRAINT fk_referrals_referrer_user_id
        FOREIGN KEY (referrer_user_id) REFERENCES users (id) ON DELETE RESTRICT;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    ALTER TABLE referrals ADD CONSTRAINT fk_referrals_referred_user_id
        FOREIGN KEY (referred_user_id) REFERENCES users (id) ON DELETE RESTRICT;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    ALTER TABLE share_events ADD CONSTRAINT fk_share_events_user_id
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    ALTER TABLE notifications ADD CONSTRAINT fk_notifications_user_id
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    ALTER TABLE ad_impressions ADD CONSTRAINT fk_ad_impressions_user_id
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    ALTER TABLE affiliate_banner_clicks ADD CONSTRAINT fk_affiliate_banner_clicks_user_id
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
