#!/usr/bin/env bash
# Verify a pg_dump custom-format backup.
# Usage: ./scripts/deployment/verify_backup.sh /path/to/file.dump
set -euo pipefail

DUMP="${1:-}"
if [[ -z "$DUMP" || ! -f "$DUMP" ]]; then
  echo "Usage: $0 <backup.dump>" >&2
  exit 2
fi

SIZE="$(wc -c < "$DUMP" | tr -d ' ')"
if [[ "$SIZE" -le 0 ]]; then
  echo "FAIL: zero-size backup" >&2
  exit 1
fi

SHA_FILE="${DUMP}.sha256"
if [[ -f "$SHA_FILE" ]]; then
  EXPECTED="$(awk '{print $1}' "$SHA_FILE")"
  if command -v sha256sum >/dev/null 2>&1; then
    ACTUAL="$(sha256sum "$DUMP" | awk '{print $1}')"
  else
    ACTUAL="$(shasum -a 256 "$DUMP" | awk '{print $1}')"
  fi
  if [[ "$EXPECTED" != "$ACTUAL" ]]; then
    echo "FAIL: checksum mismatch" >&2
    exit 1
  fi
  echo "PASS: checksum matches"
else
  echo "WARN: no .sha256 sidecar; computing hash only"
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$DUMP"
  else
    shasum -a 256 "$DUMP"
  fi
fi

echo "=== pg_restore --list (first 40 lines) ==="
pg_restore --list "$DUMP" | head -n 40
echo "VERIFY PASS ($SIZE bytes)"
