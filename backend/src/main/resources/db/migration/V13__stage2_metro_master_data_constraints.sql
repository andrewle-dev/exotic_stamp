-- Stage 2: Metro master data status model, scan-key constraints, permissions

-- Lines: tri-state status, display fields, sort order
ALTER TABLE lines ADD COLUMN IF NOT EXISTS display_name VARCHAR(100);
ALTER TABLE lines ADD COLUMN IF NOT EXISTS description VARCHAR(500);
ALTER TABLE lines ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 0;
ALTER TABLE lines ADD COLUMN IF NOT EXISTS status VARCHAR(20);

UPDATE lines SET status = CASE WHEN is_active THEN 'ACTIVE' ELSE 'INACTIVE' END WHERE status IS NULL;
UPDATE lines SET display_name = name WHERE display_name IS NULL;

ALTER TABLE lines ALTER COLUMN status SET NOT NULL;
ALTER TABLE lines DROP COLUMN IF EXISTS is_active;

ALTER TABLE lines DROP CONSTRAINT IF EXISTS chk_lines_status;
ALTER TABLE lines ADD CONSTRAINT chk_lines_status CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE'));

ALTER TABLE lines DROP CONSTRAINT IF EXISTS chk_lines_sort_order;
ALTER TABLE lines ADD CONSTRAINT chk_lines_sort_order CHECK (sort_order >= 0);

DROP INDEX IF EXISTS idx_lines_is_active;
CREATE INDEX IF NOT EXISTS idx_lines_status_sort ON lines (status, sort_order);

-- Stations: tri-state status, scan-key fields, geo zone
ALTER TABLE stations ADD COLUMN IF NOT EXISTS display_name VARCHAR(100);
ALTER TABLE stations ADD COLUMN IF NOT EXISTS address VARCHAR(255);
ALTER TABLE stations ADD COLUMN IF NOT EXISTS zone_radius_meters INT;
ALTER TABLE stations ADD COLUMN IF NOT EXISTS stamp_preview_url VARCHAR(255);
ALTER TABLE stations ADD COLUMN IF NOT EXISTS scan_key_status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE';
ALTER TABLE stations ADD COLUMN IF NOT EXISTS last_qr_rotated_at TIMESTAMP;
ALTER TABLE stations ADD COLUMN IF NOT EXISTS last_scan_key_updated_at TIMESTAMP;
ALTER TABLE stations ADD COLUMN IF NOT EXISTS status VARCHAR(20);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'stations' AND column_name = 'sequence'
    ) THEN
        ALTER TABLE stations RENAME COLUMN sequence TO sort_order;
    END IF;
END $$;

UPDATE stations SET status = CASE WHEN is_active THEN 'ACTIVE' ELSE 'INACTIVE' END WHERE status IS NULL;
UPDATE stations SET display_name = name WHERE display_name IS NULL;
UPDATE stations SET scan_key_status = 'ACTIVE'
WHERE scan_key_status = 'INACTIVE'
  AND (nfc_tag_id IS NOT NULL OR qr_code_token IS NOT NULL)
  AND status = 'ACTIVE';

ALTER TABLE stations ALTER COLUMN status SET NOT NULL;
ALTER TABLE stations DROP COLUMN IF EXISTS is_active;

ALTER TABLE stations DROP CONSTRAINT IF EXISTS uq_stations_code;
ALTER TABLE stations DROP CONSTRAINT IF EXISTS uq_stations_line_sequence;
ALTER TABLE stations ADD CONSTRAINT uq_stations_line_code UNIQUE (line_id, code);
ALTER TABLE stations ADD CONSTRAINT uq_stations_line_sort_order UNIQUE (line_id, sort_order);

ALTER TABLE stations DROP CONSTRAINT IF EXISTS chk_stations_status;
ALTER TABLE stations ADD CONSTRAINT chk_stations_status CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE'));

ALTER TABLE stations DROP CONSTRAINT IF EXISTS chk_stations_scan_key_status;
ALTER TABLE stations ADD CONSTRAINT chk_stations_scan_key_status CHECK (scan_key_status IN ('ACTIVE', 'INACTIVE'));

ALTER TABLE stations DROP CONSTRAINT IF EXISTS chk_stations_latitude;
ALTER TABLE stations ADD CONSTRAINT chk_stations_latitude
    CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90));

ALTER TABLE stations DROP CONSTRAINT IF EXISTS chk_stations_longitude;
ALTER TABLE stations ADD CONSTRAINT chk_stations_longitude
    CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180));

ALTER TABLE stations DROP CONSTRAINT IF EXISTS chk_stations_zone_radius;
ALTER TABLE stations ADD CONSTRAINT chk_stations_zone_radius
    CHECK (zone_radius_meters IS NULL OR (zone_radius_meters BETWEEN 20 AND 1000));

ALTER TABLE stations DROP CONSTRAINT IF EXISTS chk_stations_sort_order;
ALTER TABLE stations ADD CONSTRAINT chk_stations_sort_order CHECK (sort_order >= 0);

DROP INDEX IF EXISTS idx_stations_is_active;
DROP INDEX IF EXISTS idx_stations_line_seq;
CREATE INDEX IF NOT EXISTS idx_stations_line_status_sort ON stations (line_id, status, sort_order);

-- Metro RBAC permissions
INSERT INTO permissions (permission, description, version)
VALUES
    ('METRO_LINE_MANAGE', 'Manage metro lines (admin)', 0),
    ('METRO_STATION_MANAGE', 'Manage metro stations and scan keys (admin)', 0),
    ('UPLOAD_PUBLIC_ASSET', 'Upload public assets under /uploads/public', 0)
ON CONFLICT (permission) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.role = 'ADMIN'
  AND p.permission IN ('METRO_LINE_MANAGE', 'METRO_STATION_MANAGE', 'UPLOAD_PUBLIC_ASSET')
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
