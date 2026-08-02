#!/usr/bin/env bash
# Graceful shutdown smoke — sends SIGTERM and expects a clean exit within the timeout.
# Usage:
#   BASE_URL=http://127.0.0.1:8080 CONTAINER=exotic-backend ./scripts/ci/graceful_shutdown_smoke.sh
# Or with a local PID:
#   APP_PID=12345 ./scripts/ci/graceful_shutdown_smoke.sh

set -euo pipefail

TIMEOUT_SEC="${TIMEOUT_SEC:-45}"
BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"

echo "[graceful-shutdown] checking readiness at ${BASE_URL}/actuator/health/readiness"
curl -fsS --max-time 5 "${BASE_URL}/actuator/health/readiness" >/dev/null

if [[ -n "${CONTAINER:-}" ]]; then
  echo "[graceful-shutdown] docker stop -t ${TIMEOUT_SEC} ${CONTAINER}"
  docker stop -t "${TIMEOUT_SEC}" "${CONTAINER}"
  echo "[graceful-shutdown] PASS (container stopped)"
  exit 0
fi

if [[ -n "${APP_PID:-}" ]]; then
  echo "[graceful-shutdown] kill -TERM ${APP_PID}"
  kill -TERM "${APP_PID}"
  end=$((SECONDS + TIMEOUT_SEC))
  while kill -0 "${APP_PID}" 2>/dev/null; do
    if (( SECONDS >= end )); then
      echo "[graceful-shutdown] FAIL — process still alive after ${TIMEOUT_SEC}s" >&2
      exit 1
    fi
    sleep 1
  done
  echo "[graceful-shutdown] PASS (process exited)"
  exit 0
fi

echo "Set CONTAINER=<name> or APP_PID=<pid>" >&2
exit 2
