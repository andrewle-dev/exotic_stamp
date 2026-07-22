# Auth Redis Unavailable Behavior

This document describes runtime behavior when Redis is unreachable for auth-related flows. Implementation lives in `metro.ExoticStamp.infra.redis.RedisKeyValueSupport` and auth Redis repositories under `modules/auth/infrastructure/redis/`.

**Operational expectation:** Redis is required for correct auth throttling and OTP/verification token storage. Some paths fail-open (security controls weakened); others fail-safe (requests rejected).

## Summary table

| Flow | Redis operation | Redis down behavior | User-visible effect |
|------|-----------------|---------------------|---------------------|
| **Refresh** | `RefreshTokenRedisRepository.isKnownRevoked` | **Fail-open** (`false` on error) | DB session lock remains source of truth; **does not** trigger reuse-attack |
| **Refresh** | Grace credentials cache | Write/read may miss on Redis down | Concurrent retry may fail closed as reuse outside grace if DB shows ROTATED without grace payload |
| **Refresh** | `save` / `revoke` (after commit) | Write fails silently (logged) | DB `access_tokens` remains source of truth |
| **Logout** | `addToDenylist` (access jti) | Write fails silently | Access token may remain valid until JWT expiry if DB `token_version` unchanged |
| **Logout** | `refreshTokenStore.revoke` | Write fails silently | DB refresh row still revoked via `AccessTokenRepository` |
| **OTP** | `isOnCooldown` | **Fail-open** (`hasKey` fallback `false`) | Cooldown not enforced |
| **OTP** | `isMaxAttemptsExceeded` | **Fail-open** (`getValue` → empty) | Attempt limit not enforced |
| **OTP** | `find` | **Fail-closed** (`getValue` → empty) | `OtpExpiredException` on reset |
| **OTP** | `save` / `saveCooldown` | Write fails silently | OTP may not be stored; email still sent |
| **Verification token** | `isOnCooldown` | **Fail-open** | Resend cooldown not enforced server-side |
| **Verification token** | `findUserIdByToken` | **Fail-closed** | Verify-email fails — invalid/expired token |
| **Verification token** | `saveToken` / `saveCooldown` | Write fails silently | Token may not persist; user may not verify |
| **Access revocation cache** | `isDenylisted` | **Fail-open** | Denylist miss; DB `token_version` check still runs |
| **Access revocation cache** | `getCachedTokenVersion` | Cache miss → DB fallback | See `application.security.token-revocation.fail-open-on-db-error` |

## Refresh (`POST /api/v1/auth/refresh`)

1. `RefreshTokenRedisRepository.isKnownRevoked(tokenHash)` returns **false** on Redis errors (fail-open).
2. Session validity and rotation use a **DB pessimistic lock** on `access_tokens`; Redis is not the reuse-attack trigger.
3. A short-lived **grace credentials** cache (`auth:refresh_token:grace:{oldHash}`) supports concurrent/retry refresh within `application.auth.refresh-reuse-grace` (default 30s).
4. Redis `save`/`revoke` run after DB commit when a transaction is active; failures do not roll back the DB row.

**Config:** refresh Redis TTL from `cache.refresh-token-ttl` (via `CacheProperties`); grace TTL from `application.auth.refresh-reuse-grace`.

## Logout (`POST /api/v1/auth/logout`, `/logout-all`)

1. **Access token:** `AccessTokenRevocationPort.addToDenylist(jti)` → Redis `putValue`; failure is logged only.
2. **Refresh token:** `AccessTokenRepository.revokeByTokenHash` (DB) + `RefreshTokenStorePort.revoke` (Redis).
3. **Logout-all:** bumps `token_version` in DB and syncs Redis cache when possible.
4. If Redis denylist write fails, access JWT may work until expiry unless `token_version` was bumped (logout-all) or DB revocation applies.

## OTP (forgot-password / resend-otp / reset-password)

**Config keys** (`application.auth.otp` in `application.yml`):

| Key | Default | Used by |
|-----|---------|---------|
| `length` | `6` | `AuthCommandService.generateOtp()` |
| `ttl` | `5m` | `OtpRepository.save` |
| `cooldown-ttl` | `2m` | `OtpRepository.saveCooldown` |
| `attempts-ttl` | `1h` | `OtpRepository.incrementAttempts` |
| `max-attempts` | `5` | `OtpRepository.isMaxAttemptsExceeded` |

**Redis down:**

- Cooldown and max-attempt checks return **not limited** → rate limits weakened (fail-open).
- `find` returns empty → password reset with OTP fails with `OtpExpiredException`.
- `save` after `forgotPassword` may not persist OTP even if email is sent.

**Public enumeration:** `forgotPassword` and `resendVerification` return generic success; throttling is enforced server-side when Redis is healthy.

## Account verification OTP

**Config keys** (`application.auth.otp.email-verify`):

| Key | Default | Used by |
|-----|---------|---------|
| `ttl` | `10m` | `OtpRepository.save` for `OtpType.EMAIL_VERIFY` |
| `cooldown-ttl` | `2m` | Resend cooldown |
| `attempts-ttl` / `max-attempts` | `1h` / `5` | Resend rate limit |

**Redis down:**

- `resendVerificationOtp` cooldown check returns **not on cooldown** (fail-open).
- `verifyAccount` cannot resolve OTP from Redis → `OtpExpiredException`.
- Register/resend OTP `save` may not persist.

**Public response:** `POST /api/v1/auth/resend-verification-otp` returns generic success for unknown or already verified email; cooldown/max attempts return 429 when Redis is healthy.

## Access token revocation (JWT filter)

Documented separately in Stage 0 cleanup:

- Redis denylist: fail-open when Redis down.
- DB `token_version` check: controlled by `application.security.token-revocation.fail-open-on-db-error` (default `false` = fail-closed in prod).

## Metrics

Redis errors increment `cache.error` with domain tags such as `auth.otp`, `auth.verify_token`, `auth.refresh_token`, `auth.access_revocation`.

## Related files

- `infra/redis/RedisKeyValueSupport.java`
- `modules/auth/infrastructure/redis/OtpRepository.java`
- `modules/auth/infrastructure/redis/RefreshTokenRedisRepository.java`
- `modules/auth/infrastructure/redis/AccessTokenRevocationRedisRepository.java`
- `modules/auth/config/AuthSecurityProperties.java`
- `modules/auth/application/AuthCommandService.java`
