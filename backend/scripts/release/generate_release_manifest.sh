#!/usr/bin/env bash
# Wrapper: generate_release_manifest.py
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
exec python3 "$ROOT/scripts/release/generate_release_manifest.py" "$@"
