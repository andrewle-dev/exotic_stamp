#!/usr/bin/env bash
# Staging / local-prod-like smoke tests. Never prints JWTs or voucher codes.
# Usage:
#   BASE_URL=https://api-staging.example.com ./scripts/smoke/staging_smoke.sh
#   BASE_URL=http://localhost SMOKE_MODE=local ./scripts/smoke/staging_smoke.sh
set -euo pipefail

BASE_URL="${BASE_URL:?BASE_URL required}"
BASE_URL="${BASE_URL%/}"
MODE="${SMOKE_MODE:-staging}"
ADMIN_USER="${SMOKE_ADMIN_USER:-}"
ADMIN_PASS="${SMOKE_ADMIN_PASSWORD:-}"
MOBILE_USER="${SMOKE_MOBILE_USER:-}"
MOBILE_PASS="${SMOKE_MOBILE_PASSWORD:-}"
FAIL=0

redact() {
  sed -E \
    -e 's/[Aa]uthorization:[[:space:]]*Bearer[[:space:]]+[^[:space:]]+/Authorization: Bearer ***/g' \
    -e 's/"accessToken"[[:space:]]*:[[:space:]]*"[^"]+"/"accessToken":"***"/g' \
    -e 's/"refreshToken"[[:space:]]*:[[:space:]]*"[^"]+"/"refreshToken":"***"/g' \
    -e 's/"token"[[:space:]]*:[[:space:]]*"[^"]+"/"token":"***"/g' \
    -e 's/"code"[[:space:]]*:[[:space:]]*"[^"]+"/"code":"***"/g' \
    -e 's/"voucherCode"[[:space:]]*:[[:space:]]*"[^"]+"/"voucherCode":"***"/g'
}

req() {
  local method="$1" path="$2"; shift 2
  curl -sS -D /tmp/smoke_hdrs.txt -o /tmp/smoke_body.txt \
    -X "$method" "${BASE_URL}${path}" \
    -H 'Content-Type: application/json' \
    -H 'Accept: application/json' \
    "$@" || true
  CODE="$(awk 'BEGIN{c=0} /^HTTP/{c=$2} END{print c}' /tmp/smoke_hdrs.txt)"
  # never echo raw body with tokens
  BODY_SAFE="$(redact < /tmp/smoke_body.txt | head -c 500)"
  echo "HTTP $CODE $method $path :: $BODY_SAFE"
}

pass() { echo "PASS: $*"; }
fail() { echo "FAIL: $*"; FAIL=1; }

echo "=== staging smoke mode=$MODE base=$BASE_URL ==="

# Public
req GET /actuator/health/liveness
[[ "$CODE" == "200" ]] && pass "liveness" || fail "liveness ($CODE)"

req GET /actuator/health/readiness
[[ "$CODE" == "200" ]] && pass "readiness" || fail "readiness ($CODE)"

req GET /swagger-ui/index.html
[[ "$CODE" == "404" || "$CODE" == "401" || "$CODE" == "403" ]] && pass "swagger blocked ($CODE)" || fail "swagger exposed ($CODE)"

req POST /api/v1/auth/login -d '{"identifier":"invalid@example.com","password":"wrong-password-not-real"}'
[[ "$CODE" == "401" || "$CODE" == "400" || "$CODE" == "403" ]] && pass "invalid login ($CODE)" || fail "invalid login unexpected ($CODE)"

# Rate limit burst (login) — best effort
RL_HIT=0
for i in $(seq 1 25); do
  req POST /api/v1/auth/login -d '{"identifier":"ratelimit@example.com","password":"wrong"}' >/dev/null
  if [[ "$CODE" == "429" ]]; then
    RL_HIT=1
    if grep -qi 'Retry-After' /tmp/smoke_hdrs.txt; then
      pass "rate-limit 429 + Retry-After"
    else
      fail "429 without Retry-After"
    fi
    break
  fi
done
[[ "$RL_HIT" -eq 1 ]] || echo "WARN: rate-limit 429 not observed in burst (may be soft in local)"

# Admin optional
if [[ -n "$ADMIN_USER" && -n "$ADMIN_PASS" ]]; then
  req POST /api/v1/auth/login -d "{\"email\":\"$ADMIN_USER\",\"password\":\"$ADMIN_PASS\"}"
  if [[ "$CODE" == "200" ]]; then
    pass "admin login"
    TOKEN="$(python3 -c 'import json,sys; print(json.load(open("/tmp/smoke_body.txt")).get("data",{}).get("accessToken") or json.load(open("/tmp/smoke_body.txt")).get("accessToken",""))' 2>/dev/null || true)"
    if [[ -n "$TOKEN" ]]; then
      req GET /api/v1/admin/stations -H "Authorization: Bearer $TOKEN"
      [[ "$CODE" == "200" ]] && pass "admin stations" || fail "admin stations ($CODE)"
      req GET /api/v1/admin/campaigns -H "Authorization: Bearer $TOKEN"
      [[ "$CODE" == "200" ]] && pass "admin campaigns" || fail "admin campaigns ($CODE)"
    fi
    unset TOKEN
  else
    fail "admin login ($CODE)"
  fi
else
  echo "SKIP admin authenticated checks (SMOKE_ADMIN_USER/PASSWORD unset)"
fi

# Mobile optional fixture flows
if [[ -n "$MOBILE_USER" && -n "$MOBILE_PASS" ]]; then
  req POST /api/v1/auth/login -d "{\"email\":\"$MOBILE_USER\",\"password\":\"$MOBILE_PASS\"}"
  [[ "$CODE" == "200" ]] && pass "mobile login" || fail "mobile login ($CODE)"
else
  echo "SKIP mobile fixture flows (SMOKE_MOBILE_USER/PASSWORD unset)"
fi

# Failure-mode notes (manual / compose orchestration)
echo "INFO: Redis-down / S3-down / SIGTERM scenarios documented in STAGING_SMOKE_TEST_PLAN.md"

if [[ "$FAIL" -ne 0 ]]; then
  echo "SMOKE FAIL"
  exit 1
fi
echo "SMOKE PASS (mandatory checks)"
exit 0
