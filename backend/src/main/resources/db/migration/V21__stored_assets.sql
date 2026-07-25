-- Batch D: additive stored-asset metadata for S3/local object lifecycle.
-- Legacy image URL columns remain; public URLs stay derived as
-- STORAGE_PUBLIC_BASE_URL + '/' + object_key (or legacy local/full URLs).
-- Later cleanup migration may drop unused URL formats after backfill.

CREATE TABLE IF NOT EXISTS stored_assets (
    id              UUID PRIMARY KEY,
    provider        VARCHAR(32)  NOT NULL,
    object_key      VARCHAR(512) NOT NULL,
    content_type    VARCHAR(128),
    byte_size       BIGINT,
    checksum        VARCHAR(128),
    visibility      VARCHAR(16)  NOT NULL,
    status          VARCHAR(16)  NOT NULL,
    entity_type     VARCHAR(64),
    entity_id       UUID,
    public_url      VARCHAR(512),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    orphaned_at     TIMESTAMP,
    delete_after    TIMESTAMP,
    CONSTRAINT uq_stored_assets_object_key UNIQUE (object_key),
    CONSTRAINT chk_stored_assets_visibility CHECK (visibility IN ('PUBLIC', 'PRIVATE')),
    CONSTRAINT chk_stored_assets_status CHECK (status IN ('PENDING', 'ACTIVE', 'ORPHANED'))
);

CREATE INDEX IF NOT EXISTS idx_stored_assets_status_delete_after
    ON stored_assets (status, delete_after);

CREATE INDEX IF NOT EXISTS idx_stored_assets_entity
    ON stored_assets (entity_type, entity_id);

CREATE INDEX IF NOT EXISTS idx_stored_assets_public_url
    ON stored_assets (public_url);

-- Widen legacy URL columns so CDN base + object key fits.
ALTER TABLE stations
    ALTER COLUMN image_url TYPE VARCHAR(512),
    ALTER COLUMN stamp_preview_url TYPE VARCHAR(512);

ALTER TABLE campaigns
    ALTER COLUMN banner_image_url TYPE VARCHAR(512),
    ALTER COLUMN thumbnail_image_url TYPE VARCHAR(512);

ALTER TABLE stamp_designs
    ALTER COLUMN image_url TYPE VARCHAR(512),
    ALTER COLUMN preview_image_url TYPE VARCHAR(512);

ALTER TABLE partners
    ALTER COLUMN logo_url TYPE VARCHAR(512),
    ALTER COLUMN banner_image_url TYPE VARCHAR(512);

ALTER TABLE milestones
    ALTER COLUMN reward_image_url TYPE VARCHAR(512);
