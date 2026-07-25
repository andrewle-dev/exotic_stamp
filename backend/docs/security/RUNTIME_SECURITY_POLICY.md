# Runtime Security Policy — Exotic Stamp Backend

**Version:** 1.1  
**Date:** 2026-07-24  
**Batch:** C  

---

## Security filter chain order

| Order | Filter | Notes |
|-------|--------|-------|
| 1 | `CookieAuthOriginFilter` | Cookie auth origin checks |
| 2 | `JwtAuthFilter` | Sets `SecurityContext` for bearer access tokens |
| 3 | `RateLimitFilter` | After JWT so `COLLECT` can HMAC authenticated user id; public auth/scan policies ignore principal |

Servlet-container registration is **disabled** for both JWT and rate-limit filters (`FilterRegistrationBean#setEnabled(false)`). Each runs **once** in the Security filter chain.

---

## Rate limiting

| Item | Policy |
|------|--------|
| Storage | Redis (Lua token bucket), atomic; `memory` backend allowed only outside `prod` |
| Keys | Versioned: `rl:v1:{policy}:{ip}:{hmac…}`; HMAC-SHA256 of identifiers (`RATE_LIMIT_KEY_PEPPER`) |
| Pepper | Mandatory in prod; independent of `JWT_SECRET`; never logged; rotating pepper resets active buckets |
| Response | HTTP **429** `RATE_LIMIT_EXCEEDED` + `Retry-After` (seconds) |
| Redis down | HTTP **503** `SECURITY_DEPENDENCY_UNAVAILABLE` (fail-closed for security endpoints) |

Endpoints: login, register, OTP issue/resend, OTP verify/reset, refresh, scan resolve, collect.

## Client IP

Uses `HttpServletRequest.getRemoteAddr()` only after Spring `forward-headers-strategy=framework` in prod (Caddy). Spoofed `X-Forwarded-For` is not parsed in application code.

## Redis outage matrix

| Flow | Behavior |
|------|----------|
| OTP issue/resend/verify/reset | **Fail closed** → 503; no email if challenge not persisted |
| Rate limit | **Fail closed** → 503 |
| Public read caches | Soft miss → DB fallback OK |
| Access denylist check | **Fail closed** → 503 (per-token revocation cannot be verified) |
| tokenVersion DB | Source of truth for logout-all; prod DB errors reject token |
| Refresh | DB row + lock SoT; Redis grace is hint only |
| Logout with access JTI | Denylist write must succeed or logout fails |

## JWT

| Item | Policy |
|------|--------|
| Secret | Base64 (or URL-safe) of ≥32 random bytes; no SHA-256 short-secret padding |
| Claims | Issuer required; ACCESS vs REFRESH type separation; access needs `jti` + `tokenVersion` |
| Audience | **Deferred** (tokens do not currently set `aud`) |
| Rotation | Changing `JWT_SECRET` invalidates all access and refresh JWTs |

Generate: `openssl rand -base64 32`

## Uploads

JPEG / PNG / WebP only. Magic-byte + declared MIME match + dimension/pixel limits (default max 2560×2560, 6_553_600 pixels). SVG/HTML/executables rejected. GENERIC purpose still fully validated.

## Swagger

Permitted only when springdoc enabled **and** active profile is not `prod`. Otherwise matchers `denyAll` (unauthenticated clients typically see **401**).

## Error codes

| Code | HTTP |
|------|------|
| `RATE_LIMIT_EXCEEDED` | 429 |
| `SECURITY_DEPENDENCY_UNAVAILABLE` | 503 |
| Existing `INVALID_TOKEN` / `FORBIDDEN` | 401 / 403 |
