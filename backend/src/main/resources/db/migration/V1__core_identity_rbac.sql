-- Core identity, auth, and RBAC tables (UUID-first).
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    firstname           VARCHAR(50),
    lastname            VARCHAR(50),
    username            VARCHAR(255) NOT NULL,
    email               VARCHAR(255) NOT NULL,
    phone_number        VARCHAR(255) NOT NULL,
    password            VARCHAR(255) NOT NULL,
    dob                 DATE,
    gender              BOOLEAN NOT NULL DEFAULT FALSE,
    bio                 VARCHAR(100),
    avatar_url          VARCHAR(500),
    oauth2provider      VARCHAR(50),
    status              VARCHAR(20),
    verified_at         TIMESTAMP,
    password_update_at  TIMESTAMP,
    token_version       BIGINT NOT NULL DEFAULT 0,
    created_by          VARCHAR(255),
    created_at          TIMESTAMP,
    updated_by          VARCHAR(255),
    updated_at          TIMESTAMP,

    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_phone UNIQUE (phone_number)
);

CREATE TABLE roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role        VARCHAR(64) NOT NULL,
    description VARCHAR(500),
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    system_role BOOLEAN NOT NULL DEFAULT FALSE,
    version     BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_roles_role UNIQUE (role)
);

CREATE TABLE permissions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    permission  VARCHAR(80) NOT NULL,
    description VARCHAR(500),
    version     BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_permissions_permission UNIQUE (permission)
);

CREATE TABLE role_permissions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id         UUID NOT NULL,
    permission_id   UUID NOT NULL,

    CONSTRAINT fk_role_permissions_role_id
        FOREIGN KEY (role_id) REFERENCES roles (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_role_permissions_permission_id
        FOREIGN KEY (permission_id) REFERENCES permissions (id)
            ON DELETE CASCADE,

    CONSTRAINT uq_role_permissions UNIQUE (role_id, permission_id)
);

CREATE TABLE user_roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL,
    role_id     UUID NOT NULL,

    CONSTRAINT fk_user_roles_role_id
        FOREIGN KEY (role_id) REFERENCES roles (id)
            ON DELETE CASCADE,

    CONSTRAINT uq_user_roles UNIQUE (user_id, role_id)
);

CREATE TABLE access_tokens (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL,
    token_hash          VARCHAR(255) NOT NULL,
    token_type          VARCHAR(10) NOT NULL,
    token_prefix        VARCHAR(10),
    expires_at          TIMESTAMP NOT NULL,
    revoked_at          TIMESTAMP,
    revoked_reason      VARCHAR(40),
    ip_address          VARCHAR(45) NOT NULL,
    user_agent          VARCHAR(1000) NOT NULL,
    device_fingerprint  VARCHAR(255) NOT NULL,
    created_at          TIMESTAMP NOT NULL,

    CONSTRAINT uq_access_tokens_token_hash UNIQUE (token_hash)
);

CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL,
    table_name      VARCHAR(100) NOT NULL,
    action_type     VARCHAR(100) NOT NULL,
    action_time     TIMESTAMP NOT NULL,
    old_value       TEXT,
    new_value       TEXT,
    changed_date    VARCHAR(255),
    ip_address      VARCHAR(100) NOT NULL
);

CREATE INDEX idx_user_roles_user_id ON user_roles (user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles (role_id);
CREATE INDEX idx_access_tokens_user_active ON access_tokens (user_id, expires_at) WHERE revoked_at IS NULL;
CREATE INDEX idx_access_tokens_token_hash ON access_tokens (token_hash);
CREATE INDEX idx_audit_logs_user_time ON audit_logs (user_id, action_time DESC);
