-- Post-schema seeds and compatibility updates.
ALTER TABLE users ADD COLUMN IF NOT EXISTS token_version BIGINT NOT NULL DEFAULT 0;

UPDATE roles SET system_role = TRUE WHERE role = 'ADMIN';

INSERT INTO permissions (permission, description, version)
VALUES
    ('RBAC_ADMIN', 'Administrative RBAC management APIs', 0),
    ('INTERNAL', 'Internal service APIs (e.g. metro collector increment)', 0),
    ('READ_SALES', NULL, 0),
    ('UPLOAD_SALES', NULL, 0),
    ('READ_CUSTOMER', NULL, 0),
    ('UPLOAD_CUSTOMER', NULL, 0),
    ('READ_SERVICE', NULL, 0),
    ('UPLOAD_SERVICE', NULL, 0),
    ('READ_BOOKING', NULL, 0),
    ('UPLOAD_BOOKING', NULL, 0),
    ('READ_REALTIME', NULL, 0),
    ('MANAGE_USER', NULL, 0),
    ('ASSIGN_ROLE', NULL, 0),
    ('UPLOAD_APP_USAGE', NULL, 0)
ON CONFLICT (permission) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.role = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
);
