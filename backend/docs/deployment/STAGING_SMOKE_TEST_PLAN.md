# Staging Smoke Test Plan

**Batch:** F.0
Scripts: `scripts/smoke/staging_smoke.{sh,ps1}`

Never use production credentials. Never print JWT, voucher codes, NFC payloads, or Authorization headers.

## Public

| Check | Expect |
|-------|--------|
| Liveness | 200 |
| Readiness | 200 |
| Swagger / OpenAPI | 404 (or blocked) |
| Invalid login | 401/400/403 |
| Rate-limit burst | 429 + Retry-After |

## Admin (staging test user)

Login, station list, campaign list, valid image upload, invalid upload rejection, public asset URL, reward reconcile authorization.

## Mobile / backend (staging fixtures)

Login, refresh, station listing, scan resolve, collect, same-key replay, different-payload conflict, stamp visibility, reward eligibility when fixture permits.

## Failure behavior

| Scenario | Expect |
|----------|--------|
| Redis unavailable | Protected security flows 503 |
| S3 unavailable | Upload failure; no DB pointer |
| Backend SIGTERM | Graceful within shutdown timeout |
| Backend restart | Readiness recovers |

## Local mode

`SMOKE_MODE=local` + `BASE_URL=http://localhost` (Caddy edge). Mandatory public checks must pass; authenticated flows optional when credentials unset.
