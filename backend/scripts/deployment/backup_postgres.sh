#!/usr/bin/env bash
# Logical PostgreSQL backup (custom format). Passwords via PGPASSWORD only.
# Usage:
#   export APP_ENV=local-prod-like DB_HOST=... DB_PORT=5432 DB_NAME=... DB_USERNAME=... PGPASSWORD=...
#   ./scripts/deployment/backup_postgres.sh
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"

: "${APP_ENV:?APP_ENV required (e.g. local-prod-like|staging — not production without override)}"
: "${DB_HOST:?DB_HOST required}"
: "${DB_PORT:=5432}"
: "${DB_NAME:?DB_NAME required}"
: "${DB_USERNAME:?DB_USERNAME required}"

if [[ -z "${PGPASSWORD:-}" && -n "${DB_PASSWORD:-}" ]]; then
  export PGPASSWORD="$DB_PASSWORD"
fi
: "${PGPASSWORD:?set PGPASSWORD or DB_PASSWORD}"

if [[ "$APP_ENV" == "production" || "$APP_ENV" == "prod" ]]; then
  if [[ "${ALLOW_PRODUCTION_BACKUP:-}" != "YES" ]]; then
    echo "ERROR: production backup requires ALLOW_PRODUCTION_BACKUP=YES" >&2
    exit 1
  fi
fi

export PGHOST="$DB_HOST" PGPORT="$DB_PORT" PGDATABASE="$DB_NAME" PGUSER="$DB_USERNAME"

SCHEMA_VER="unknown"
SCHEMA_VER="$(psql -X -q -t -A -c "SELECT COALESCE(MAX(version::int)::text,'unknown') FROM flyway_schema_history WHERE success AND version ~ '^[0-9]+$';" 2>/dev/null || echo unknown)"
SCHEMA_VER="$(echo "$SCHEMA_VER" | tr -d '[:space:]')"

TS="$(date -u +"%Y%m%dT%H%M%SZ")"
OUT_DIR="${BACKUP_DIR:-$ROOT/artifacts/backups}"
mkdir -p "$OUT_DIR"
BASE="${APP_ENV}_${DB_NAME}_fw${SCHEMA_VER}_${TS}"
DUMP="$OUT_DIR/${BASE}.dump"

echo "Creating custom-format backup (credentials not on argv)..."
pg_dump -Fc -f "$DUMP"

if command -v sha256sum >/dev/null 2>&1; then
  SHA="$(sha256sum "$DUMP" | awk '{print $1}')"
else
  SHA="$(shasum -a 256 "$DUMP" | awk '{print $1}')"
fi
echo "$SHA  $(basename "$DUMP")" > "${DUMP}.sha256"

SIZE="$(wc -c < "$DUMP" | tr -d ' ')"
echo "BACKUP_PATH=$DUMP"
echo "BACKUP_SHA256=$SHA"
echo "BACKUP_BYTES=$SIZE"
echo "FLYWAY_SCHEMA=$SCHEMA_VER"
