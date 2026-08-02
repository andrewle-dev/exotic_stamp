#!/usr/bin/env bash
# Rollback staging to a prior immutable image. Never reverses Flyway.
# Usage:
#   ./scripts/deployment/rollback_staging.sh --dry-run --image exotic-stamp-backend:git-abc1234
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"

DRY_RUN=0
IMAGE_REF="${ROLLBACK_IMAGE_REF:-}"
BASE_URL="${STAGING_BASE_URL:-}"
TIMEOUT_SEC="${DEPLOY_TIMEOUT_SEC:-180}"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --dry-run) DRY_RUN=1; shift ;;
    --image) IMAGE_REF="$2"; shift 2 ;;
    --base-url) BASE_URL="$2"; shift 2 ;;
    -h|--help) sed -n '1,6p' "$0"; exit 0 ;;
    *) echo "Unknown: $1" >&2; exit 2 ;;
  esac
done

TARGET_ENV="${TARGET_ENV:-staging}"
if [[ "$TARGET_ENV" != "staging" ]]; then
  echo "ERROR: rollback script is staging-only (got $TARGET_ENV)" >&2
  exit 1
fi
if [[ -z "$IMAGE_REF" ]]; then
  echo "ERROR: --image or ROLLBACK_IMAGE_REF required (immutable tag/digest)" >&2
  exit 1
fi

log() { echo "[rollback-staging $(date -u +%H:%M:%SZ)] $*"; }
mkdir -p artifacts/deploy
TS="$(date -u +"%Y%m%dT%H%M%SZ")"
LOG="artifacts/deploy/rollback-staging-${TS}.log"

{
  log "targetEnv=staging dryRun=$DRY_RUN image=$IMAGE_REF"
  log "NOTE: Flyway down-migrations are FORBIDDEN. App rollback only."
  log "1. verify prior image available"
  if [[ "$DRY_RUN" -eq 1 ]]; then
    log "DRY-RUN: would docker image inspect $IMAGE_REF"
  else
    docker image inspect "$IMAGE_REF" >/dev/null || docker pull "$IMAGE_REF"
  fi
  log "2. compatibility assumption: prior image must tolerate current additive schema"
  log "3. start prior image"
  if [[ "$DRY_RUN" -eq 1 ]]; then
    log "DRY-RUN: would start $IMAGE_REF and keep failed image retained"
  else
    log "Operator starts prior container on Lightsail (no AWS mutation in F.0 dry-run path)"
  fi
  log "4. wait readiness"
  if [[ -n "$BASE_URL" && "$DRY_RUN" -eq 0 ]]; then
    deadline=$((SECONDS + TIMEOUT_SEC))
    until curl -fsS "$BASE_URL/actuator/health/readiness" >/dev/null 2>&1; do
      [[ $SECONDS -ge $deadline ]] && { echo "readiness timeout"; exit 1; }
      sleep 3
    done
    log "readiness OK"
  else
    log "SKIP readiness wait"
  fi
  log "5. smoke tests"
  if [[ "$DRY_RUN" -eq 1 ]]; then
    log "DRY-RUN: would run staging smoke"
  elif [[ -n "$BASE_URL" ]]; then
    BASE_URL="$BASE_URL" ./scripts/smoke/staging_smoke.sh
  else
    log "SKIP smoke"
  fi
  echo "{\"event\":\"rollback\",\"image\":\"$IMAGE_REF\",\"timestampUtc\":\"$(date -u +%Y-%m-%dT%H:%M:%SZ)\",\"flywayReversed\":false}" \
    > "artifacts/deploy/rollback-event-${TS}.json"
  log "ROLLBACK EVENT RECORDED"
} | tee "$LOG"
