#!/usr/bin/env bash
# Read-only database preflight for staging/local launch gates.
# Usage:
#   export DB_HOST=localhost DB_PORT=5432 DB_NAME=exotic_stamp DB_USERNAME=... PGPASSWORD=...
#   ./scripts/deployment/run_db_preflight.sh
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"

: "${DB_HOST:?DB_HOST required}"
: "${DB_PORT:=5432}"
: "${DB_NAME:?DB_NAME required}"
: "${DB_USERNAME:?DB_USERNAME required}"
: "${PGPASSWORD:?PGPASSWORD or DB_PASSWORD required — set PGPASSWORD (not on argv)}"

# Prefer PGPASSWORD env; allow mapping from DB_PASSWORD without echoing.
if [[ -z "${PGPASSWORD:-}" && -n "${DB_PASSWORD:-}" ]]; then
  export PGPASSWORD="$DB_PASSWORD"
fi
: "${PGPASSWORD:?set PGPASSWORD or DB_PASSWORD}"

export PGHOST="$DB_HOST"
export PGPORT="$DB_PORT"
export PGDATABASE="$DB_NAME"
export PGUSER="$DB_USERNAME"

mkdir -p artifacts/preflight
TS="$(date -u +"%Y%m%dT%H%M%SZ")"
REPORT="artifacts/preflight/db-preflight-${DB_NAME}-${TS}.txt"
FAIL=0

redact() {
  sed -E \
    -e 's/(password=)[^&[:space:]]+/\1***/Ig' \
    -e 's/(PWD=)[^;[:space:]]+/\1***/Ig' \
    -e 's|jdbc:postgresql://[^[:space:]]+|jdbc:postgresql://***|g'
}

{
  echo "=== Exotic Stamp DB preflight (READ-ONLY) ==="
  echo "timestampUtc=$TS"
  echo "host=$DB_HOST port=$DB_PORT db=$DB_NAME user=$DB_USERNAME"
  echo "jdbc=jdbc:postgresql://***/$DB_NAME (credentials redacted)"
  echo
} | tee "$REPORT"

psql_ro() {
  psql -v ON_ERROR_STOP=1 -X -q -c "SET default_transaction_read_only = on;" -c "$1" 2>&1 | redact
}

echo "=== PostgreSQL version / extensions ===" | tee -a "$REPORT"
psql_ro "SELECT version();" | tee -a "$REPORT" || FAIL=1
psql_ro "SELECT extname, extversion FROM pg_extension ORDER BY 1;" | tee -a "$REPORT" || true

echo "=== Flyway schema history ===" | tee -a "$REPORT"
FLY_SQL=$(cat <<'SQL'
SELECT version, description, success, installed_rank, installed_on
FROM flyway_schema_history
ORDER BY installed_rank;
SQL
)
if ! psql_ro "$FLY_SQL" | tee -a "$REPORT"; then
  echo "WARN: flyway_schema_history missing or unreadable" | tee -a "$REPORT"
  FAIL=1
fi

echo "=== Failed / invalid Flyway rows ===" | tee -a "$REPORT"
FAIL_SQL="SELECT version, description, success FROM flyway_schema_history WHERE success = false;"
FAIL_OUT="$(psql_ro "$FAIL_SQL" || true)"
echo "$FAIL_OUT" | tee -a "$REPORT"
if echo "$FAIL_OUT" | grep -E '^[[:space:]]*[0-9]+' >/dev/null 2>&1; then
  echo "LAUNCH-BLOCKING: failed Flyway rows present" | tee -a "$REPORT"
  FAIL=1
fi

echo "=== Current schema version ===" | tee -a "$REPORT"
psql_ro "SELECT MAX(version::int) AS current_version FROM flyway_schema_history WHERE success = true AND version ~ '^[0-9]+$';" | tee -a "$REPORT" || true

echo "=== Packaged migrations vs applied (local filesystem) ===" | tee -a "$REPORT"
PACKAGED="$(ls src/main/resources/db/migration/V*.sql 2>/dev/null | sed -E 's|.*/V([0-9]+)__.*|\1|' | sort -n | tr '\n' ' ')"
echo "packagedVersions=$PACKAGED" | tee -a "$REPORT"

echo "=== Batch E data preflight SQL ===" | tee -a "$REPORT"
PRE_SQL="$ROOT/docs/deployment/BATCH_E_DATA_PREFLIGHT.sql"
if [[ -f "$PRE_SQL" ]]; then
  # Force read-only session; never auto-fix.
  OUT="$(psql -v ON_ERROR_STOP=1 -X -q \
    -c "SET default_transaction_read_only = on;" \
    -f "$PRE_SQL" 2>&1 | redact || true)"
  echo "$OUT" | tee -a "$REPORT"
  # Heuristic: any SELECT that returns data rows for sections 1-6/10 is blocking.
  # Operators must review; non-empty duplicate sections fail the gate when PREFLIGHT_STRICT=1.
  if [[ "${PREFLIGHT_STRICT:-1}" == "1" ]]; then
    if echo "$OUT" | grep -E 'cnt[[:space:]]+\|[[:space:]]*[2-9]|cnt[[:space:]]+\|[[:space:]]*[1-9][0-9]' >/dev/null 2>&1; then
      echo "LAUNCH-BLOCKING: duplicate/integrity cnt rows detected (review report)" | tee -a "$REPORT"
      FAIL=1
    fi
  fi
else
  echo "ERROR: missing $PRE_SQL" | tee -a "$REPORT"
  FAIL=1
fi

echo "=== stored_assets integrity (if table exists) ===" | tee -a "$REPORT"
psql_ro "SELECT COUNT(*) AS stored_assets FROM information_schema.tables WHERE table_name='stored_assets';" | tee -a "$REPORT" || true
psql_ro "SELECT status, COUNT(*) FROM stored_assets GROUP BY status ORDER BY 1;" 2>/dev/null | tee -a "$REPORT" || echo "(stored_assets absent)" | tee -a "$REPORT"

echo "Report: $REPORT"
if [[ "$FAIL" -ne 0 ]]; then
  echo "PREFLIGHT FAIL"
  exit 1
fi
echo "PREFLIGHT PASS"
exit 0
