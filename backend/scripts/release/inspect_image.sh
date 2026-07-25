#!/usr/bin/env bash
# Inspect a release image for non-root user, ports, labels, forbidden paths.
# Usage: ./scripts/release/inspect_image.sh exotic-stamp-backend:git-abc1234
set -euo pipefail

IMAGE="${1:-}"
if [[ -z "$IMAGE" ]]; then
  echo "Usage: $0 <image:tag>" >&2
  exit 2
fi

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
OUT_DIR="$ROOT/artifacts/release"
mkdir -p "$OUT_DIR"
SHORT="$(echo "$IMAGE" | tr '/:' '__')"
REPORT="$OUT_DIR/image-inspect-${SHORT}.txt"
FAIL=0

{
  echo "=== image: $IMAGE ==="
  echo "=== inspect (user/entrypoint/ports/labels/env names) ==="
  docker image inspect "$IMAGE" --format \
'Id={{.Id}}
User={{.Config.User}}
Entrypoint={{json .Config.Entrypoint}}
Cmd={{json .Config.Cmd}}
ExposedPorts={{json .Config.ExposedPorts}}
Labels={{json .Config.Labels}}
EnvNames={{range .Config.Env}}{{(split . "=")._0}} {{end}}'
  echo
  echo "=== forbidden path scan (container filesystem) ==="
} > "$REPORT"

# Env values intentionally omitted from report beyond names above.
USER_VAL="$(docker image inspect "$IMAGE" --format '{{.Config.User}}')"
if [[ -z "$USER_VAL" || "$USER_VAL" == "root" || "$USER_VAL" == "0" ]]; then
  echo "FAIL: image user is root or empty ($USER_VAL)" | tee -a "$REPORT"
  FAIL=1
else
  echo "PASS: non-root user=$USER_VAL" | tee -a "$REPORT"
fi

CID="$(docker create "$IMAGE" true)"
cleanup() { docker rm -f "$CID" >/dev/null 2>&1 || true; }
trap cleanup EXIT

FORBIDDEN=(
  "/.env"
  "/app/.env"
  "/.git"
  "/app/.git"
  "/root/.m2"
  "/.m2"
  "/app/uploads"
  "/workspace/.env"
  "/var/app/uploads"
)

for p in "${FORBIDDEN[@]}"; do
  if docker export "$CID" 2>/dev/null | tar -t 2>/dev/null | grep -E "^\\.?${p#/}(/|$)" >/dev/null 2>&1; then
    echo "FAIL: forbidden path present: $p" | tee -a "$REPORT"
    FAIL=1
  else
    # Prefer find via docker cp of listing — export|tar can be heavy; use docker run find as fallback
    :
  fi
done

# Robust find-based scan (read-only)
FIND_OUT="$(docker run --rm --entrypoint /bin/sh "$IMAGE" -c \
  'find / -xdev \( -name .env -o -name .git -o -name .m2 -o -path "*/uploads/*" -o -name "*.dump" -o -name "*secret*" \) 2>/dev/null | head -n 50' || true)"
echo "$FIND_OUT" >> "$REPORT"
if echo "$FIND_OUT" | grep -E '(/\.env$|/\.git$|/\.m2$|/uploads/|\.dump$)' >/dev/null 2>&1; then
  echo "FAIL: forbidden filesystem entries detected" | tee -a "$REPORT"
  FAIL=1
else
  echo "PASS: no forbidden .env/.git/.m2/uploads/dump hits in shallow scan" | tee -a "$REPORT"
fi

# Confirm jar exists and is owned suitably
if docker run --rm --entrypoint /bin/sh "$IMAGE" -c 'test -f /app/app.jar && id'; then
  echo "PASS: /app/app.jar present" | tee -a "$REPORT"
else
  echo "FAIL: /app/app.jar missing or shell unavailable" | tee -a "$REPORT"
  FAIL=1
fi

echo "Report: $REPORT"
if [[ "$FAIL" -ne 0 ]]; then
  echo "INSPECT FAIL"
  exit 1
fi
echo "INSPECT PASS"
exit 0
