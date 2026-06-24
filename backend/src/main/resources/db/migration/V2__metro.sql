-- Metro Network module (UUID-first)
CREATE TABLE lines (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(10) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    color           VARCHAR(7),
    total_stations  INT NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_by      VARCHAR(255),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by      VARCHAR(255),
    updated_at      TIMESTAMP,

    CONSTRAINT uq_lines_code UNIQUE (code)
);

CREATE INDEX idx_lines_is_active ON lines (is_active);

CREATE TABLE stations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    line_id         UUID NOT NULL,
    code            VARCHAR(20) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    sequence        INT NOT NULL,
    description     VARCHAR(500),
    historical_info TEXT,
    image_url       VARCHAR(255),
    latitude        DECIMAL(9, 6),
    longitude       DECIMAL(9, 6),
    nfc_tag_id      VARCHAR(100),
    qr_code_token   VARCHAR(100),
    collector_count INT NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_by      VARCHAR(255),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by      VARCHAR(255),
    updated_at      TIMESTAMP,

    CONSTRAINT fk_stations_line_id
        FOREIGN KEY (line_id) REFERENCES lines (id)
            ON DELETE RESTRICT,

    CONSTRAINT uq_stations_code UNIQUE (code),
    CONSTRAINT uq_stations_nfc_tag_id UNIQUE (nfc_tag_id),
    CONSTRAINT uq_stations_qr_token UNIQUE (qr_code_token),
    CONSTRAINT uq_stations_line_sequence UNIQUE (line_id, sequence)
);

CREATE INDEX idx_stations_nfc_tag_id ON stations (nfc_tag_id) WHERE nfc_tag_id IS NOT NULL;
CREATE INDEX idx_stations_qr_code_token ON stations (qr_code_token) WHERE qr_code_token IS NOT NULL;
CREATE INDEX idx_stations_line_seq ON stations (line_id, sequence);
CREATE INDEX idx_stations_is_active ON stations (is_active);
