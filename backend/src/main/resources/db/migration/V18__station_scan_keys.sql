-- Production station scan keys (hashed secrets, lifecycle, installation metadata)

CREATE TABLE station_scan_keys (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    station_id                  UUID NOT NULL,
    scan_type                   VARCHAR(30) NOT NULL,
    key_hash                    VARCHAR(128) NOT NULL,
    key_prefix                  VARCHAR(32) NOT NULL,
    payload_scheme              VARCHAR(50) NOT NULL DEFAULT 'metrostamp://scan',
    label                       VARCHAR(100),
    placement_note              VARCHAR(255),
    status                      VARCHAR(30) NOT NULL,
    activated_at                TIMESTAMP NULL,
    revoked_at                  TIMESTAMP NULL,
    replaced_by_id              UUID NULL,
    last_seen_at                TIMESTAMP NULL,
    last_install_verified_at    TIMESTAMP NULL,
    installed_latitude          DOUBLE PRECISION NULL,
    installed_longitude         DOUBLE PRECISION NULL,
    installed_accuracy_meters   DOUBLE PRECISION NULL,
    installed_device_platform   VARCHAR(30) NULL,
    installed_app_version       VARCHAR(50) NULL,
    installed_by                UUID NULL,
    created_by                  UUID NULL,
    created_at                  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMP NOT NULL DEFAULT NOW(),
    version                     BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_station_scan_keys_station
        FOREIGN KEY (station_id) REFERENCES stations (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_station_scan_keys_replaced_by
        FOREIGN KEY (replaced_by_id) REFERENCES station_scan_keys (id)
            ON DELETE SET NULL,
    CONSTRAINT uq_station_scan_keys_key_hash UNIQUE (key_hash),
    CONSTRAINT chk_station_scan_keys_scan_type
        CHECK (scan_type IN ('NFC', 'QR_STATIC', 'QR_DYNAMIC_PLACEHOLDER')),
    CONSTRAINT chk_station_scan_keys_status
        CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE', 'REVOKED', 'LOST', 'REPLACED'))
);

CREATE INDEX idx_station_scan_keys_station_type_status
    ON station_scan_keys (station_id, scan_type, status);

CREATE INDEX idx_station_scan_keys_status
    ON station_scan_keys (status);

CREATE INDEX idx_station_scan_keys_last_seen_at
    ON station_scan_keys (last_seen_at);
