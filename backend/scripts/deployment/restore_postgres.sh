#!/usr/bin/env bash
# Restore a custom-format dump into an explicit non-production database.
# Usage:
#   export APP_ENV=local-prod-like DB_HOST=... DB_USERNAME=... PGPASSWORD=...
#   ./scripts/deployment/restore_postgres.sh --dump path.dump --target-db exotic_stamp_restore_tmp --confirm YES
set -euo pipefail

DUMP=""
TARGET_DB=""
CONFIRM=""
CREATE_DB=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --dump) DUMP="$2"; shift 2 ;;
    --target-db) TARGET_DB="$2"; shift 2 ;;
    --confirm) CONFIRM="$2"; shift 2 ;;
    --create-db) CREATE_DB=1; shift ;;
    -h|--help) sed -n '1,6p' "$0"; exit 0 ;;
    *) echo "Unknown: $1" >&2; exit 2 ;;
  esac
done

: "${APP_ENV:?APP_ENV required}"
: "${DB_HOST:?DB_HOST required}"
: "${DB_PORT:=5432}"
: "${DB_USERNAME:?DB_USERNAME required}"
: "${DUMP:?--dump required}"
: "${TARGET_DB:?--target-db required}"
: "${CONFIRM:?--confirm YES required}"

if [[ -z "${PGPASSWORD:-}" && -n "${DB_PASSWORD:-}" ]]; then
  export PGPASSWORD="$DB_PASSWORD"
fi
: "${PGPASSWORD:?set PGPASSWORD or DB_PASSWORD}"

if [[ "$CONFIRM" != "YES" ]]; then
  echo "ERROR: refuse restore without --confirm YES" >&2
  exit 1
fi

# Block production target by default
if [[ "$APP_ENV" == "production" || "$APP_ENV" == "prod" || "$TARGET_DB" == "exotic_stamp_prod" || "$TARGET_DB" == "exotic_stamp_production" ]]; then
  if [[ "${ALLOW_PRODUCTION_RESTORE:-}" != "YES_I_UNDERSTAND" ]]; then
    echo "ERROR: production restore blocked. Set ALLOW_PRODUCTION_RESTORE=YES_I_UNDERSTAND only with explicit authorization." >&2
    exit 1
  fi
fi

if [[ ! -f "$DUMP" ]]; then
  echo "ERROR: dump not found: $DUMP" >&2
  exit 1
fi

export PGHOST="$DB_HOST" PGPORT="$DB_PORT" PGUSER="$DB_USERNAME"

echo "Restoring into target database '$TARGET_DB' (no DROP DATABASE)."
if [[ "$CREATE_DB" -eq 1 ]]; then
  # Connect to maintenance DB to create target if missing; never drop.
  psql -d postgres -v ON_ERROR_STOP=1 -c "SELECT 1 FROM pg_database WHERE datname='${TARGET_DB}'" | grep -q 1 \
    || psql -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE \"${TARGET_DB}\";"
fi

# Restore into existing empty/disposable DB. Do not --clean drop the database.
pg_restore --no-owner --no-acl -d "$TARGET_DB" "$DUMP"
echo "RESTORE PASS → $TARGET_DB"
