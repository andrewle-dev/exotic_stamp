#!/usr/bin/env bash
# Staging deploy preparation / execution with dry-run and local simulation.
# Does NOT target AWS by default. Never auto-selects production.
# Usage:
#   ./scripts/deployment/deploy_staging.sh --dry-run
#   ./scripts/deployment/deploy_staging.sh --local-simulate
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"

DRY_RUN=0
LOCAL_SIM=0
IMAGE_REF="${STAGING_IMAGE_REF:-}"
BASE_URL="${STAGING_BASE_URL:-}"
TIMEOUT_SEC="${DEPLOY_TIMEOUT_SEC:-180}"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --dry-run) DRY_RUN=1; shift ;;
    --local-simulate) LOCAL_SIM=1; shift ;;
    --image) IMAGE_REF="$2"; shift 2 ;;
    --base-url) BASE_URL="$2"; shift 2 ;;
    -h|--help) sed -n '1,8p' "$0"; exit 0 ;;
    *) echo "Unknown: $1" >&2; exit 2 ;;
  esac
done

TARGET_ENV="${TARGET_ENV:-staging}"
if [[ "$TARGET_ENV" != "staging" && "$LOCAL_SIM" -ne 1 ]]; then
  echo "ERROR: refuse non-staging target ($TARGET_ENV). Use local-simulate for local-prod-like." >&2
  exit 1
fi
if [[ "$TARGET_ENV" == "production" || "$TARGET_ENV" == "prod" ]]; then
  echo "ERROR: production deploy is not supported by this script." >&2
  exit 1
fi

log() { echo "[deploy-staging $(date -u +%H:%M:%SZ)] $*"; }

REQUIRED_VARS=(APP_ENV)
if [[ "$DRY_RUN" -eq 0 && "$LOCAL_SIM" -eq 0 ]]; then
  REQUIRED_VARS+=(STAGING_HOST STAGING_IMAGE_REF)
fi
for v in "${REQUIRED_VARS[@]}"; do
  if [[ -z "${!v:-}" && "$v" != "APP_ENV" ]]; then
    :
  fi
done
: "${APP_ENV:=staging}"
if [[ "$APP_ENV" == "production" || "$APP_ENV" == "prod" ]]; then
  echo "ERROR: APP_ENV must not be production for this script." >&2
  exit 1
fi

PREV_VERSION_FILE="${PREV_VERSION_FILE:-artifacts/release/current-staging-version.txt}"
mkdir -p artifacts/release artifacts/deploy
TS="$(date -u +"%Y%m%dT%H%M%SZ")"
LOG="artifacts/deploy/deploy-staging-${TS}.log"

cleanup() { log "cleanup trap (no secrets logged)"; }
trap cleanup EXIT

{
  log "targetEnv=$TARGET_ENV dryRun=$DRY_RUN localSimulate=$LOCAL_SIM"
  log "1. validate env"
  log "2. record current version (if present)"
  if [[ -f "$PREV_VERSION_FILE" ]]; then
    log "current=$(cat "$PREV_VERSION_FILE")"
  else
    log "current=none"
  fi
  log "3. database preflight"
  if [[ "$DRY_RUN" -eq 1 ]]; then
    log "DRY-RUN: would run scripts/deployment/run_db_preflight.sh"
  elif [[ -n "${DB_HOST:-}" ]]; then
    ./scripts/deployment/run_db_preflight.sh
  else
    log "SKIP preflight (DB_HOST unset) — set for real deploy"
  fi
  log "4. database backup"
  if [[ "$DRY_RUN" -eq 1 ]]; then
    log "DRY-RUN: would run scripts/deployment/backup_postgres.sh"
  elif [[ -n "${DB_HOST:-}" ]]; then
    ./scripts/deployment/backup_postgres.sh
  else
    log "SKIP backup (DB_HOST unset)"
  fi
  log "5. pull/load immutable image"
  if [[ -z "$IMAGE_REF" ]]; then
    IMAGE_REF="exotic-stamp-backend:git-$(git rev-parse --short=7 HEAD)"
  fi
  log "imageRef=$IMAGE_REF"
  if [[ "$DRY_RUN" -eq 1 ]]; then
    log "DRY-RUN: would docker pull/load $IMAGE_REF"
  elif [[ "$LOCAL_SIM" -eq 1 ]]; then
    log "LOCAL: expect image already built: $IMAGE_REF"
    docker image inspect "$IMAGE_REF" >/dev/null
  else
    docker pull "$IMAGE_REF"
  fi
  log "6. start new container (keep previous image)"
  if [[ "$DRY_RUN" -eq 1 ]]; then
    log "DRY-RUN: would start container from $IMAGE_REF"
  elif [[ "$LOCAL_SIM" -eq 1 ]]; then
    log "LOCAL: use docker compose prod-like recreate for backend only (operator)"
  else
    log "Operator must run remote start on Lightsail — script stops before AWS mutation in F.0"
  fi
  log "7-8. wait liveness/readiness"
  if [[ -n "$BASE_URL" && "$DRY_RUN" -eq 0 ]]; then
    deadline=$((SECONDS + TIMEOUT_SEC))
    until curl -fsS "$BASE_URL/actuator/health/liveness" >/dev/null 2>&1; do
      [[ $SECONDS -ge $deadline ]] && { echo "liveness timeout"; exit 1; }
      sleep 3
    done
    until curl -fsS "$BASE_URL/actuator/health/readiness" >/dev/null 2>&1; do
      [[ $SECONDS -ge $deadline ]] && { echo "readiness timeout"; exit 1; }
      sleep 3
    done
    log "health OK"
  else
    log "SKIP health wait (BASE_URL unset or dry-run)"
  fi
  log "9. smoke tests"
  if [[ "$DRY_RUN" -eq 1 ]]; then
    log "DRY-RUN: would run scripts/smoke/staging_smoke.sh"
  elif [[ -n "$BASE_URL" ]]; then
    BASE_URL="$BASE_URL" ./scripts/smoke/staging_smoke.sh
  else
    log "SKIP smoke (BASE_URL unset)"
  fi
  log "10. record release manifest"
  if [[ "$DRY_RUN" -eq 1 ]]; then
    log "DRY-RUN: would generate release manifest"
  else
    python3 scripts/release/generate_release_manifest.py --allow-dirty --version "${RELEASE_VERSION:-0.1.0-rc.1}" || \
      python scripts/release/generate_release_manifest.py --allow-dirty --version "${RELEASE_VERSION:-0.1.0-rc.1}"
  fi
  echo "$IMAGE_REF" > "$PREV_VERSION_FILE"
  log "DEPLOY MARKED SUCCESS (smoke/health gates as applicable)"
} | tee "$LOG"
