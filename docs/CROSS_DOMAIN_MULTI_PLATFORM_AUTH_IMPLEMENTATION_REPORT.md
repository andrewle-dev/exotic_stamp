# Cross-Domain Multi-Platform Auth Implementation Report

**Date:** 2026-07-22 (hardened)  
**Source plan:** [`docs/CROSS_DOMAIN_MULTI_PLATFORM_AUTH_ALIGNMENT_PLAN.md`](CROSS_DOMAIN_MULTI_PLATFORM_AUTH_ALIGNMENT_PLAN.md)

---

## Final verdicts

| Gate | Verdict |
|------|---------|
| BACKEND AUTH MODEL | **READY** |
| WEB AUTH MODEL | **READY** |
| FLUTTER AUTH MODEL | **READY** |
| COOKIE/CORS SECURITY | **READY** |
| REFRESH ROTATION | **READY** |
| SESSION REVOCATION | **READY** |
| TEST GATES | **PASS** (full required commands) |

**Overall release readiness: READY**

---

## 1. Architecture after implementation

One backend session model (`access_tokens` REFRESH rows with family lineage) and two transports:

| Client | Access | Refresh | Transport header |
|--------|--------|---------|------------------|
| Web (Vite SPA) | Memory | HttpOnly cookie | `X-Client-Transport: cookie` (default) |
| Flutter | Memory | `flutter_secure_storage` | `X-Client-Transport: body` |

`X-Client-Transport` selects response shaping only. It never authorizes. CSRF and credential resolution use the **resolved credential source** (cookie vs body), not the header alone.

**Refresh decision:** single endpoint `POST /api/v1/auth/refresh`.

- Body `refreshToken` present → native (BODY); refresh returned in JSON; cookie cleared.
- Cookie only → web (COOKIE); Set-Cookie rotated; refresh omitted from JSON.
- Both present and unequal → `400 CONFLICTING_REFRESH_CREDENTIALS`.

---

## 2. Backend session model

Flyway [`V20__auth_refresh_session_family.sql`](../backend/src/main/resources/db/migration/V20__auth_refresh_session_family.sql) adds family lineage columns. Raw refresh tokens are never persisted — only SHA-256 hashes.

---

## 3. Web token lifecycle

1. Login → access in memory (`tokenStore`); refresh cookie Path=`/api/v1/auth`, SameSite=Lax.
2. Bootstrap always silent-refreshes; `markAuthBootstrapComplete()` unlocks the API client before `/users/me`.
3. Protected requests await bootstrap (except auth bootstrap-exempt paths).
4. Single-flight refresh shared by bootstrap and 401 interceptor (`refreshAccessTokenOnce`).
5. Logout / failed bootstrap → one unauthenticated state + `queryClient.clear()`.
6. Legacy `localStorage` access key removed on startup.

---

## 4. Flutter token lifecycle

1. Login with body transport → access memory + refresh secure storage.
2. Rotated refresh must persist; write failure clears session and forces re-auth (no authenticated continuation with unpersisted refresh).
3. `NativeRefreshInterceptor` single-flight; storage failure clears tokens, emits session-invalidated once, avoids retry loops.
4. Device id = app-generated UUID in secure storage.

---

## 5. Cookie matrix + legacy Path cleanup

| Property | Value |
|----------|-------|
| Name | `refresh_token` |
| Path (current) | `/api/v1/auth` |
| Path (legacy, expired on set/clear) | `/api/v1/auth/refresh` |
| HttpOnly | true |
| SameSite | Lax (Case B FaceWashFox) |
| Secure | prod: always (`secure-always: true`); non-prod: follows request / config |
| Domain | optional via `AUTH_COOKIE_DOMAIN` |

**One-release compatibility:**

- On successful web login/refresh: set Path=`/api/v1/auth` **and** expire legacy Path=`/api/v1/auth/refresh`.
- On logout / logout-all / change-password: clear **both** paths with matching Domain/SameSite/Secure.
- Tests: `RefreshCookieSupportTest` proves dual Set-Cookie on set and clear.

---

## 6. CORS configuration

- Exact origin allowlist; credentials true; rejects `*` with credentials.
- Prod origins from `CORS_ALLOWED_ORIGINS` only.
- Prod: `server.forward-headers-strategy: framework` for reverse-proxy HTTPS.

---

## 7. CSRF strategy (credential-source Origin policy)

[`CookieAuthOriginFilter`](../backend/src/main/java/metro/ExoticStamp/modules/auth/infrastructure/filter/CookieAuthOriginFilter.java):

| Credential source | Origin/Referer |
|-------------------|----------------|
| Cookie present (refresh/logout cookie auth) | **Required** — allowlisted Origin, or Referer-derived origin; missing → `403 ORIGIN_REQUIRED` |
| Body-only refresh (native) | Origin **not** required |
| Spoofed `X-Client-Transport: body` without body credential + cookie present | Still requires Origin (cannot bypass) |

Filter is registered only via `SecurityConfig` `@Bean` (not servlet `@Component`) so `@WebMvcTest` slices stay isolated.

Tests: `CookieAuthOriginFilterTest` (5 cases).

---

## 8. Production proxy / Secure cookie

- Prod YAML: `application.auth.cookie.secure-always: true` + forwarded-headers strategy.
- `@PostConstruct` on `AuthCookieProperties`: **fails startup** if active profiles include `prod` and `secure-always=false`.
- Cookie Secure resolution: `secure-always` / `secure` flags take precedence over `request.isSecure()` alone.
- Tests: `AuthCookiePropertiesTest`.

---

## 9. Refresh rotation algorithm

1. Grace-cache hit → return same new credentials (concurrent retry).
2. Validate JWT; load session with **pessimistic write lock**.
3. Enforce not expired, not revoked, user ACTIVE.
4. Re-check account status + `tokenVersion` before issuing (logout-all / disable / change-password races).
5. Rotate DB row; write Redis grace payload (TTL = refresh-reuse-grace, default 30s).
6. After commit → Redis mirror revoke/save.

---

## 10. Confirmed reuse revocation scope — **Policy B (global)**

Confirmed refresh-token reuse **outside grace**:

- `revokeAllByUserId` (all sessions)
- bump `tokenVersion` (global access invalidation)
- audit `scope=ALL_SESSIONS`, `tokenVersionBumped=true`
- error `REFRESH_TOKEN_REUSED`

This is **not** family-only. Documentation, audit, implementation, and tests are aligned (`AuthCommandServiceTest.refresh_reuseOutsideGrace_revokesAllSessions`).

Losing-race access tokens with stale `tokenVersion` are rejected by `AccessTokenRevocationValidator`.

---

## 11. Redis grace-cache failure behavior (P1)

If DB rotation commits and Redis grace payload is missing within the grace window (Redis put swallowed / unavailable):

- Retry of the **old** token → `RefreshUnavailableException` → **503** (safe temporary error / re-login)
- **Does not** classify as reuse / compromise
- **Does not** mass-revoke on infrastructure uncertainty

Test: `refresh_graceWithoutCache_throwsUnavailableNotReuse`.

---

## 12. Refresh / logout race coverage

| Scenario | Behavior / test |
|----------|-----------------|
| Refresh vs global revocation (`tokenVersion` bump) | `SessionRevokedException` before issue |
| Account disable during refresh | `UserNotActiveException` (initial + mid-refresh re-check) |
| Confirmed reuse | Global revoke + version bump |
| Access after global revocation | Version mismatch → REVOKED |
| Grace Redis miss | Unavailable, not reuse |

---

## 13. Flutter secure-storage failure (P1)

After successful refresh/login:

- Persist refresh first; on write failure → clear memory access + local refresh → `FailureCode.unauthorized` / session invalidated → login redirect path
- No retry loop with unpersisted rotated token

Tests: `auth_repository_impl_test`, `native_refresh_interceptor_test`.

---

## 14. Web startup / cache isolation (P1)

- Protected API calls wait for bootstrap completion.
- Failed bootstrap → single unauthenticated state.
- Logout clears React Query cache (`queryClient.clear()`).
- Bootstrap + 401 share one in-flight refresh.

Tests: `client.auth.test.ts`, `tokenStore.test.ts`.

---

## 15. Token logging audit (P1)

Searched backend / web / Flutter for Authorization, cookie, body, and raw token logging.

- `JwtAuthFilter` logs only userId / error messages — not bearer values.
- Focused capture test: `JwtAuthFilterLogSafetyTest`.
- Removed debug auth-startup prints from `AuthCubit`.
- No Dio/Axios debug interceptors dumping secrets found on the auth path.

---

## 16. Full release gate results

### Backend — **PASS**

```
.\mvnw.cmd clean test
```

Result: **BUILD SUCCESS** — Tests run: **441**, Failures: **0**, Errors: **0**, Skipped: **4** (Docker/IT environment skips).

### Web — **PASS**

```
npm run lint
npm run typecheck
npm test
npm run build
```

All exit **0**. AssetImageFieldCard `help`/`purpose` TypeScript errors fixed. Lint Fast Refresh issue on providers resolved by moving `appQueryClient` out of the component module.

### Flutter — **PASS**

```
flutter analyze
flutter test
```

Analyze: **No issues found**. Tests: **297 passed**.

---

## 17. Remaining risks

- Grace cache still holds short-lived raw tokens in Redis (TTL 30s) — protect Redis ACL.
- Architecture waiver: `auth.presentation` may depend on selected auth domain transport types until relocated (documented in `ArchitectureBoundaryTest`).
- Online voucher redeem remains product-disabled (`canRedeem => false`); test updated to match.
- Skipped backend IT tests still require Docker for full environment coverage (not auth-blocking).

---

## 18. Rollout checklist

1. Apply Flyway V20 on staging.
2. Deploy backend (`secure-always`, cookie Path `/api/v1/auth`, Origin filter, Policy B reuse).
3. Deploy web (memory access, bootstrap gate, dual-path cookie cleanup via backend).
4. Deploy Flutter (body transport + secure refresh fail-closed).
5. Verify login web: Set-Cookie current + Max-Age=0 legacy path.
6. Verify logout clears both cookie paths.
7. Verify cookie refresh missing Origin → 403; body refresh without Origin → 200-class auth path.
8. Verify concurrent refresh does not revoke-all; confirmed reuse outside grace does revoke-all.

---

## 19. Rollback checklist

1. Revert client deploys first; keep body refresh support on backend.
2. Backend rollback: keep V20 columns (expand-only); revert code if needed.
3. Do not delete V20 in place.

---

## Key files changed (hardening)

**Backend:** `AuthCommandService`, `CookieAuthOriginFilter`, `RefreshCookieSupport`, `AuthCookieProperties`, `SecurityConfig`, exception handlers, auth tests, ArchUnit waiver.

**Web:** `client.ts` (bootstrap gate + single-flight), `AuthProvider`, `api.ts`, `AssetImageFieldCard`, `queryClient` / `providers`.

**Mobile:** `NativeRefreshInterceptor`, `AuthRepositoryImpl`, `AuthCubit`, storage-failure tests.

**Docs:** this report.
