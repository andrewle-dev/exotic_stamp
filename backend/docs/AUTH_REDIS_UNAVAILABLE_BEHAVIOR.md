# Auth Redis Unavailable Behavior

This document describes runtime behavior when Redis is unreachable for auth-related flows. Implementation lives in `metro.ExoticStamp.infra.redis.RedisKeyValueSupport` and auth Redis repositories under `modules/auth/infrastructure/redis/`.

**Operational expectation:** Redis is required for correct auth throttling and OTP/verification token storage. Some paths fail-open (security controls weakened); others fail-safe (requests rejected).

## Summary table

| Flow | Redis operation | Redis down behavior | User-visible effect |
|------|-----------------|---------------------|---------------------|
| **Refresh** | `RefreshTokenRedisRepository.isRevoked` | **Fail-safe** (`hasKey` fallback `true`) | Refresh rejected — token treated as revoked |
| **Refresh** | `save` / `revoke` | Write fails silently (logged) | DB `access_tokens` remains source of truth for hash lookup |
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

1. `RefreshTokenRedisRepository.isRevoked(tokenHash)` uses `hasKey(..., fallbackOnError=true)`.
2. When Redis is down, **every refresh token is treated as revoked** → `InvalidTokenException` / reuse-handling path.
3. This is **fail-safe** for session security (prevents reuse when revocation state cannot be read).
4. Redis `save`/`revoke` failures do not roll back the DB transaction; `access_tokens` table still records rotation/revocation.

**Config:** refresh Redis TTL from `cache.refresh-token-ttl` (via `CacheProperties`).

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
