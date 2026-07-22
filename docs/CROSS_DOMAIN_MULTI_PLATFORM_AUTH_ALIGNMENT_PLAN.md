# Cross-Domain Multi-Platform Auth Alignment Plan

**Audit date:** 2026-07-22  
**Scope:** `/backend` (Spring Boot), `/web` (Vite React SPA), `/mobile` (Flutter)  
**Method:** Source-grounded inspection only. Missing behavior is recorded as absent, not assumed.  
**This document does not modify code, migrations, dependencies, or API contracts.**

---

## Final verdicts

| Gate | Verdict |
|------|---------|
| BACKEND AUTH MODEL | **PARTIAL** |
| WEB AUTH MODEL | **NOT ALIGNED** |
| FLUTTER AUTH MODEL | **NOT ALIGNED** |
| COOKIE/CORS SECURITY | **NOT READY** |
| REFRESH ROTATION | **NOT READY** |
| SESSION REVOCATION | **PARTIAL** (logout-all / password change work; current-session logout broken by cookie Path) |
| SAFE TO IMPLEMENT | **BLOCKED** until P0 cookie Path, refresh reuse/race, and dual-transport design are decided |

Enterprise readiness is **not** claimed from token storage location alone.

---

## 1. Executive verdict

ExoticStamp implements a **single cookie-only refresh transport** shared by web and Flutter, with JWT access tokens, DB-backed refresh hashes in `access_tokens`, and Redis for reuse denylist / access jti denylist / `tokenVersion` cache.

That design is **not** an enterprise-grade cross-domain multi-platform auth architecture:

- Web stores the **access token in `localStorage`** (target: memory only).
- Flutter stores the **refresh credential in a plaintext cookie jar** under app documents (target: `flutter_secure_storage`), and depends on browser-style cookies rather than a native refresh contract.
- Backend refresh rotation exists but **treats rotated-token reuse as a full revoke-all**, with **no concurrent-refresh grace**, and Redis-down `isRevoked=true` also triggers **reuse-attack revoke-all**.
- Refresh cookie `Path=/api/v1/auth/refresh` prevents the browser from sending the cookie to `/logout`, so **current-session refresh often survives logout**.
- Cookie flags omit SameSite/Domain; clear uses `Secure=false` while set uses `request.isSecure()`.
- Staging docs incorrectly recommend `SameSite=None` for `report.facewashfox.com` ↔ `backend.facewashfox.com` (cross-origin, **same-site**).

**Preserve** exact CORS allowlists, hash-only refresh persistence, short access TTL, single-flight client refresh, and logout-all / password-change `tokenVersion` bumps.

---

## 2. Actual current backend design

### 2.1 Session / token model

| Artifact | Implementation |
|----------|----------------|
| Access token | JWT; claims: `iss`, `sub` (userId), `iat`, `exp`, `jti`, `email`, `roles`, `tokenType=ACCESS`, `tokenVersion` |
| Refresh token | JWT; claims: `iss`, `sub`, `iat`, `exp`, `tokenType=REFRESH` — **no `jti`, no `tokenVersion`, no `sessionId`** |
| Persistence | `access_tokens` rows with `tokenType=REFRESH`, SHA-256 `tokenHash` |
| Redis | `auth:refresh_token:valid:{userId}:{deviceFp}`, `auth:refresh_token:revoked:{hash}`, `denylist:{jti}`, `auth:access_jti:{userId}:{fp}`, `user:{id}:tokenVersion` |
| Transport | HttpOnly cookie only; body refresh cleared before response |
| Client typing | **No** `X-Client-Type` (or equivalent) |

### 2.2 File-level inventory (backend)

**Security / CORS**

| File | Role |
|------|------|
| `backend/src/main/java/metro/ExoticStamp/config/SecurityConfig.java` | Filter chain; CSRF off; CORS; STATELESS; public auth paths; JWT filter |
| `backend/src/main/java/metro/ExoticStamp/config/CorsProperties.java` | Binds origins/methods/headers/credentials; rejects `*` with credentials |
| `backend/src/main/resources/application.yml` | Default CORS, JWT TTLs (15m / 7d), token-revocation fail-closed |
| `backend/src/main/resources/application-dev.yml` | Dev CORS includes `https://report.facewashfox.com`; fail-open DB revocation; default JWT secret |
| `backend/src/main/resources/application-prod.yml` | `CORS_ALLOWED_ORIGINS` required; swagger off |

**Auth module (primary)**

| File | Role |
|------|------|
| `.../auth/presentation/AuthController.java` | Login/refresh/logout/logout-all/change-password; cookie set/clear |
| `.../auth/application/AuthCommandService.java` | Login, rotate refresh, reuse attack, logout, password flows |
| `.../auth/infrastructure/filter/JwtAuthFilter.java` | Bearer access only; denylist + tokenVersion check |
| `.../auth/infrastructure/jwt/JwtProvider.java` | Issue/parse access; generate/hash refresh |
| `.../auth/infrastructure/security/AccessTokenRevocationValidator.java` | jti denylist → version cache → DB |
| `.../auth/infrastructure/redis/RefreshTokenRedisRepository.java` | Valid/revoked keys; `isRevoked` fail-safe true |
| `.../auth/infrastructure/redis/AccessTokenRevocationRedisRepository.java` | Denylist, version cache, per-device access jti |
| `.../auth/domain/model/AccessToken.java` | Refresh session entity fields + revoke reasons |
| `.../auth/application/AuditLogService.java` | Audit persistence (login, password change, some OTP) |
| `.../common/exceptions/GlobalExceptionHandler.java` | Auth error codes → HTTP |

**Flyway**

| Migration | Auth relevance |
|-----------|----------------|
| `V1__core_identity_rbac.sql` | `users.token_version`, `access_tokens`, `audit_logs` |
| `V8__auth_token_version_and_seed.sql` | `token_version` if missing + RBAC seeds |
| `V12__stage0_integrity_constraints.sql` | FKs for tokens / audit |

**Tests / docs / OpenAPI**

| Path | Role |
|------|------|
| `backend/src/test/.../auth/application/AuthCommandServiceTest.java` | Login, rotation, reuse, logout, change-password |
| `backend/src/test/.../auth/presentation/AuthControllerTest.java` | Cookie on login |
| `backend/src/test/.../config/CorsConfigurationTest.java` | CORS |
| `backend/docs/AUTH_REDIS_UNAVAILABLE_BEHAVIOR.md` | Redis fail modes (understates reuse-attack path) |
| `backend/docs/api/openapi.json` | Auth path snapshot |
| `backend/src/main/java/.../config/OpenApiConfig.java` | bearerAuth scheme |

### 2.3 Cookie flags (as implemented)

| Flag | Value |
|------|-------|
| Name | `refresh_token` |
| HttpOnly | `true` |
| Secure (set) | `request.isSecure()` |
| Secure (clear) | **`false`** (mismatch) |
| Path | `/api/v1/auth/refresh` |
| Max-Age | refresh TTL seconds / `0` on clear |
| SameSite | **Not set** |
| Domain | **Not set** |

### 2.4 Endpoints

| Endpoint | Auth | Behavior |
|----------|------|----------|
| `POST /api/v1/auth/login` | Public | Issue tokens; DB+Redis; Set-Cookie; strip refresh from body |
| `POST /api/v1/auth/refresh` | Public | Cookie only → rotate |
| `POST /api/v1/auth/logout` | Bearer | Denylist access jti; revoke refresh **if cookie present** |
| `POST /api/v1/auth/logout-all` | Bearer | Revoke all refreshes + bump `tokenVersion` |
| `POST /api/v1/auth/change-password` | Bearer | Validate password; revoke all + bump version; clear cookie |
| Forgot / reset / OTP | Public | OTP in Redis; reset revokes all + bump version |

### 2.5 Absent vs target

No dual transport, no refresh family/parent/replacedBy, no concurrent refresh lock, no `sessionId` claim, no HTTP login/refresh rate limit, no SameSite/Domain cookie API, no Origin CSRF checks, refresh does **not** enforce `UserStatus.ACTIVE`.

---

## 3. Actual current web design

### 3.1 Runtime

Vite + React SPA (`createBrowserRouter`), Axios `apiClient` with `withCredentials: true`, TanStack Query. **Not** Next.js.

### 3.2 File-level inventory (web)

| File | Role |
|------|------|
| `web/src/lib/api/client.ts` | Axios; credentials; single-flight 401 refresh |
| `web/src/lib/auth/tokenStore.ts` | Access token in **`localStorage`** key `exotic_stamp_admin_access_token` |
| `web/src/features/auth/api.ts` | login, refreshSession, logout, forgot/reset |
| `web/src/features/auth/AuthProvider.tsx` | Bootstrap; login/logout; auth context |
| `web/src/components/layout/RequireAuth.tsx` | Route guard waits on `isInitializing` |
| `web/src/lib/query/queryClient.ts` | QueryClient; retry skips 401/403/404 |
| `web/src/app/providers.tsx` | Module-level QueryClient + AuthProvider |
| `web/src/lib/api/errors.ts` | Backend `ErrorResponse` mapping |
| `web/.env.example` | `VITE_API_BASE_URL`, `VITE_APP_ENV` |

**Docs mismatch:** `web/docs/ADMIN_WEB_*` claim memory-only access token and sometimes `SameSite=None`; source uses `localStorage` and does not set cookies.

### 3.3 Gaps vs target

| Target | Status |
|--------|--------|
| Memory-only access token | **Gap** — localStorage |
| HttpOnly refresh (JS cannot read) | **Met** (never read; cookie assumed) |
| Silent refresh before protected calls | **Partial** — only if no stored access token |
| Single-flight refresh | **Met** |
| Global logout on refresh failure | **Partial** — clears tokenStore; does not clear AuthProvider user |
| React Query clear on logout | **Gap** |
| Logout-all / change-password UI | **Gap** |
| CSRF / CSP | **Gap** |
| Auth tests | **Gap** (vitest wired, zero auth tests) |

---

## 4. Actual current Flutter design

### 4.1 Runtime

Single Dio client: CookieManager → AuthInterceptor → CookieRefreshInterceptor → ErrorInterceptor.

### 4.2 File-level inventory (mobile)

| File | Role |
|------|------|
| `mobile/lib/core/network/api_client.dart` | Dio factory; `clearSession()` |
| `mobile/lib/core/network/auth_interceptor.dart` | Bearer from secure storage |
| `mobile/lib/core/network/cookie_refresh_interceptor.dart` | Cookie refresh; single-flight |
| `mobile/lib/core/storage/secure_token_storage.dart` | Access token only; Android ESP |
| `mobile/lib/core/config/api_config.dart` | Host/port; Android emulator `10.0.2.2` |
| `mobile/lib/core/services/device_fingerprint_service.dart` | Android `id` / iOS `identifierForVendor` |
| `mobile/lib/features/auth/data/repositories/auth_repository_impl.dart` | Login/restore/logout |
| `mobile/lib/features/auth/presentation/cubit/auth_cubit.dart` | Session state |
| Profile privacy/security cubits | logout / logout-all |
| Auth tests under `mobile/test/features/auth/**` | Repository/cubit; **no** interceptor concurrency tests |

### 4.3 Gaps vs target

| Target | Status |
|--------|--------|
| Access token memory-only | **Gap** — secure storage + interceptor reads storage |
| Refresh in flutter_secure_storage | **Gap** — PersistCookieJar `.cookies/` |
| Native refresh body/header contract | **Gap** — empty-body cookie refresh |
| Single-flight 401 | **Met** |
| Wipe on logout / change-password | **Met** (`clearSession`) |
| Explicit iOS Keychain options | **Gap** |
| Distinct prod vs emulator HTTPS configs | **Partial** — dart-define / debug override only |
| App-generated random device id | **Gap** — hardware-ish identifiers |

---

## 5. Current login / refresh / logout diagrams

For each flow: only source-backed behavior. Absence is noted.

### 5.1 Web login

```mermaid
sequenceDiagram
  participant Browser
  participant API
  participant DB
  participant Redis
  Browser->>API: POST /api/v1/auth/login JSON credentials withCredentials
  API->>DB: save access_tokens refresh hash
  API->>Redis: valid key + device access jti
  API-->>Browser: accessToken + userInfo body; Set-Cookie refresh_token Path=/api/v1/auth/refresh
  Note over Browser: tokenStore.set accessToken in localStorage
```

| Aspect | Source behavior |
|--------|-----------------|
| Token source | Password login |
| Access destination | JSON → localStorage |
| Refresh destination | Set-Cookie HttpOnly |
| TX | `@Transactional` login |
| Failure | `INVALID_CREDENTIALS` / account status exceptions |

### 5.2 Web startup after page reload

1. If localStorage has access token → skip silent refresh; call `GET /users/me`.
2. If no access token → `POST /refresh` with credentials; on success store access token; then `GET /users/me`.
3. Failure → clear tokenStore; unauthenticated.

**Gap:** expired access token in localStorage can race protected queries before 401 refresh; silent refresh is not unconditional.

### 5.3 Web authenticated API call

Bearer from localStorage + cookies sent for credentialed requests. Protected routes wait on `RequireAuth` `isInitializing` only at route level — not per-query restoration beyond that.

### 5.4 Web access token expiry

401 → shared `refreshPromise` → one `POST /refresh` → retry original once. Refresh failure clears tokenStore; AuthProvider user may remain until navigation/guard re-eval.

### 5.5 Web refresh

Cookie-only; browser sends cookie only to Path `/api/v1/auth/refresh`. Response sets new cookie + new accessToken in body → localStorage.

### 5.6 Web logout

`POST /logout` with Bearer. **Browser does not send refresh cookie** (Path mismatch). Backend denylists access jti if parseable; refresh revoke often skipped. Clear-cookie header uses Path refresh + Secure=false. Client clears localStorage + React user state. **No QueryClient clear.**

### 5.7 Flutter login

Login JSON may include `deviceFingerprint`. Access token → secure storage. Refresh Set-Cookie → PersistCookieJar files. Cubit holds Session with accessToken copy (network still reads storage).

### 5.8 Flutter cold start

`restoreSession`: if access token present → `GET` current user; on auth failure → cookie refresh; else clear. If no access token → try cookie refresh. Timeout 20s in cubit. On unauthenticated, `clearSession`.

### 5.9 Flutter authenticated API call

AuthInterceptor reads secure storage for Bearer. Cookies attached by CookieManager.

### 5.10 Flutter refresh

`POST /auth/refresh` with `skipAuth`, empty body; cookie jar supplies cookie. New accessToken written to secure storage. Single-flight via `_refreshFuture`.

### 5.11 Flutter logout

Best-effort `POST /logout` then `clearSession` (secure storage + deleteAll cookies). Same Path/cookie limitations on server revoke of refresh.

### 5.12 Logout all

Backend: revoke all DB refreshes, Redis `revokeAllForUser` (deletes valid keys only), bump `tokenVersion`. Flutter profile flow calls endpoint then clears local session. Web: **not implemented**.

### 5.13 Change password

Backend: verify current password; revoke all; bump `tokenVersion`; clear cookie (Path-limited). Flutter: on success wipe session + navigate login. Web: **not implemented** (forgot/reset only).

### 5.14 Refresh token reuse detection

```mermaid
sequenceDiagram
  participant Client
  participant API
  participant Redis
  participant DB
  Client->>API: refresh with token T1
  API->>Redis: isRevoked(hash T1)
  alt Redis says revoked OR Redis down fail-safe true
    API->>DB: revokeAll REUSE_ATTACK
    API->>Redis: revokeAllForUser + bump tokenVersion
    API-->>Client: 401 SECURITY_BREACH
  else not revoked
    API->>DB: find valid hash; revoke ROTATED; save new
    API->>Redis: revoke old hash into revoked set; save new valid
    API-->>Client: new access + Set-Cookie new refresh
  end
```

**No grace window. No family scope. Concurrent second refresh of T1 after first rotates → revoke-all.**

### 5.15 Account disabled or locked

Login checks `PENDING_VERIFIED` and `ACTIVE`. **Refresh loads user and issues new tokens without status check** — disabled accounts can refresh if they still hold a valid refresh cookie/token. Protected requests load UserDetails; inactive enforcement depends on user loading / role paths, not an explicit refresh gate.

---

## 6. Confirmed security violations

1. **Cookie Path too narrow** (`/api/v1/auth/refresh`) — logout/change-password cannot receive refresh cookie → incomplete session revoke.
2. **Cookie clear Secure mismatch** — set may be Secure; clear forces `Secure=false`.
3. **No SameSite** — inconsistent browser defaults across environments.
4. **Redis-down → reuse-attack** — `isRevoked` fallback `true` calls `handleReuseAttack` (mass revoke + version bump), not a soft reject.
5. **Concurrent/duplicate refresh false positive** — rotated token reuse triggers user-wide revoke-all with no grace.
6. **Single cookie transport only** — Flutter cannot meet secure-storage refresh without API dual transport or continued cookie jar.
7. **Web access token in localStorage** — XSS-exfiltratable; docs claim memory-only (false).
8. **Flutter refresh in plaintext cookie files** — not Keystore/Keychain.
9. **Refresh skips account ACTIVE check**.
10. **Docs recommend SameSite=None for same-site FaceWashFox subdomains** — incorrect for Case B.
11. **CSRF disabled** while refresh/logout are cookie-influenced POSTs.
12. **Device fingerprint from stable platform IDs** — not app-generated UUID; risk of over-binding semantics if later enforced.
13. **Dev JWT default secret and fail-open DB revocation** in `application-dev.yml` — must not leak to prod.
14. **Auth audit gaps** — logout, refresh, reuse attack not fully audited (reuse only `log.error`).

---

## 7. Good patterns to preserve

- Exact CORS origin allowlist; startup rejection of `*` with `allowCredentials=true`.
- Refresh token never left in JSON for JS (`clearRefreshToken()` after Set-Cookie).
- Refresh stored as SHA-256 hash in DB, not plaintext.
- Short access TTL (15m) with separate refresh TTL (7d).
- Directionally split revocation: `tokenVersion` (global) + jti denylist (individual access) + refresh row revoke.
- Client single-flight refresh (web Axios promise; Flutter QueuedInterceptor + `_refreshFuture`).
- Logout-all and password change bump `tokenVersion` and revoke DB refreshes.
- OTP cooldown / max attempts when Redis healthy.
- Flutter logout / logout-all / change-password wipe local token material.
- OpenAPI documents cookie-based refresh.

---

## 8. Deployment topology analysis

### Evidence from source / config

| Env | Web | API | Notes |
|-----|-----|-----|-------|
| Local | `http://localhost:5173` | `http://localhost:8080` | Vite; `VITE_API_BASE_URL`; CORS defaults include both 3000 and 5173 |
| Documented staging hosts | `https://report.facewashfox.com` | `https://backend.facewashfox.com` | Same registrable domain `facewashfox.com` |
| Prod | `${FRONTEND_URL}` / `CORS_ALLOWED_ORIGINS` | `${BACKEND_URL}` | Env-driven; checklist forbids localhost in prod CORS |

### Classification

| Case | Definition | This project |
|------|------------|--------------|
| A Same-origin | Identical scheme/host/port | Not default for admin SPA |
| B Cross-origin, same-site | Different origins, same eTLD+1 | **Intended FaceWashFox topology** |
| C Cross-site | Different sites | Only if frontend moved off-parent domain |

**Correction applied:** `report.facewashfox.com` and `backend.facewashfox.com` are different **origins** but the same **site**. SameSite=Lax is appropriate; SameSite=None is not required.

Flutter native apps are not browser same-site actors; they should use an explicit refresh credential transport, not rely on cookie site rules.

---

## 9. Cookie policy matrix

### Current vs recommended

| Flag | Current | Recommended Case B (prod HTTPS) | Recommended local HTTP |
|------|---------|----------------------------------|------------------------|
| Name | `refresh_token` | keep | keep |
| HttpOnly | true | true | true |
| Secure | `isSecure()` / clear `false` | **true**; clear must match | false on HTTP; clear must match |
| SameSite | unset | **`Lax`** | `Lax` |
| Domain | unset | host-only (omit) unless shared subdomain cookie required | omit |
| Path | `/api/v1/auth/refresh` | **`/api/v1/auth`** (covers refresh + logout + change-password) | same |
| Max-Age | refresh TTL | keep aligned with refresh TTL | keep |
| Deletion | Path ok, Secure wrong | **identical Domain/Path/SameSite/Secure** as set | identical |

### Per topology

| Case | SameSite | Secure | CSRF extra |
|------|----------|--------|------------|
| A Same-origin | Lax | env | Origin check still useful |
| B Cross-origin same-site | **Lax** | true on HTTPS | Origin/Referer allowlist on cookie auth POSTs |
| C Cross-site | **None** | mandatory true | CSRF token or strict Origin allowlist **required**; CORS ≠ CSRF |

**Do not** default FaceWashFox to SameSite=None.

---

## 10. CORS policy

### Current

| Setting | Value |
|---------|-------|
| Origins | CSV allowlist (`localhost:3000/5173`, `report.facewashfox.com` in dev; prod from env) |
| Methods | GET,POST,PUT,PATCH,DELETE,OPTIONS,HEAD |
| Headers | `*` (Spring allowed headers) |
| Credentials | true |
| Wildcard origin | Rejected at startup when credentials true |
| Exposed headers | **Not configured** |
| Max-Age | **Not configured** |

### Recommended

- Keep exact origin allowlist; never `*` with credentials.
- Prod: only real admin origins; no localhost.
- Document that `Authorization` and any future transport header (e.g. `X-Client-Transport`) must be allowed if not covered by `*`.
- CORS does **not** replace CSRF defense for cookie POSTs.
- Flutter native Dio is not a browser CORS client; CORS policy is for web only.

---

## 11. Web token lifecycle

### Target

1. Login → access in **memory**; refresh HttpOnly cookie.
2. Reload → **always** silent refresh before protected data; wait for restoration.
3. API calls → Bearer from memory; credentials for refresh cookie.
4. Concurrent 401 → one refresh; queued retries once.
5. Refresh fail → one global logout; clear auth state + **user-scoped QueryClient caches**.
6. Logout / account switch → wipe memory + server revoke + cache clear.

### Current delta

| Step | Current |
|------|---------|
| Access storage | localStorage |
| Reload | Silent refresh only if no stored access token |
| Refresh fail | Clears storage; may leave AuthProvider user |
| Cache | No clear |
| Logout-all / change-password | Missing UI |

### Remediation direction

- Replace `tokenStore` with in-memory holder (+ optional broadcast channel for multi-tab later).
- Bootstrap: always attempt silent refresh (or refresh if access missing/expired), gate data loads on auth ready.
- On logout and interceptor refresh failure: clear user, navigate login, `queryClient.clear()`.
- Align docs with code.

---

## 12. Flutter token lifecycle

### Target

1. Login → access memory; refresh secure storage (Keystore/Keychain).
2. Cold start → restore refresh from secure storage; exchange via **native refresh contract**; then API.
3. 401 → single-flight refresh with body/header credential; no cookie dependency.
4. Logout / logout-all / change-password → wipe memory + secure storage.
5. Distinct configs: emulator (`10.0.2.2`), simulator, physical device host, production HTTPS.

### Current delta

| Step | Current |
|------|---------|
| Access | Secure storage (not memory-only for network) |
| Refresh | PersistCookieJar files |
| Contract | Cookie-only empty POST |
| Device id | Platform hardware-ish ids |
| Prod HTTPS base | Not first-class flavor |

### Remediation direction

Requires **additive backend dual transport**: web keeps cookie; native sends refresh in body (or dedicated header). Transport selector header may choose format only — **never** grant authorization. Until then Flutter cannot meet the target without insecure cookie jar persistence.

---

## 13. Refresh rotation / session-family design

### Current model fields (`AccessToken` refresh row)

| Field | Present |
|-------|---------|
| Token id (UUID) | Yes |
| Token hash | Yes |
| User id | Yes |
| Session id (auth) | **No** |
| Device id / fingerprint | Yes (`deviceFingerprint`) |
| Family id | **No** |
| Parent token id | **No** |
| issuedAt (`createdAt`) | Yes |
| expiresAt | Yes |
| usedAt | **No** |
| revokedAt / revokedReason | Yes |
| replacedBy | **No** |
| Client platform | **No** |
| UA / IP | Yes (copied on refresh; not re-validated) |

### Rotation completeness: **PARTIAL / UNSAFE**

- Rotate-on-refresh: yes (old → `ROTATED`, new row).
- Family lineage: no.
- Reuse detection: Redis revoked hash → **user-wide** revoke-all + `tokenVersion` bump + `SECURITY_BREACH`.
- Cannot distinguish stolen reuse vs duplicate network retry vs concurrent refresh vs late response.

### Recommended design (commit)

1. Session row gains: `familyId`, `parentId` / `replacedByTokenId`, optional generation/`version`, `usedAt`, `clientPlatform`.
2. Persist hash only; never raw refresh.
3. **Atomic conditional update** (`UPDATE … WHERE revoked_at IS NULL` or optimistic version) inside transaction.
4. **Reuse policy:**
   - Within short grace (e.g. 5–30s) for same parent → return current child (retry tolerance).
   - Outside grace → revoke **family** (not necessarily all user sessions) + audit + force re-auth for that family.
5. Do **not** revoke-all-user on every repeated refresh unless policy explicitly escalates (confirmed multi-family breach).
6. Redis revoked set is a cache/accelerator; DB session state is source of truth for validity/rotation.

---

## 14. Concurrency strategy

### Current locking

**None** for refresh: no row lock, no optimistic version, no Redis lock. Check-then-update via Redis `isRevoked` + DB `isValid()`.

### Race catalogue (source)

| Race | Current outcome |
|------|-----------------|
| Two concurrent refreshes with same T1 | First rotates; second hits revoked → **revoke-all** |
| Logout during refresh | Path cookie often missing on logout; refresh may still succeed |
| Logout-all during refresh | Version bump invalidates new access; refresh may still write new row depending on timing |
| Password change during refresh | Same as logout-all class |
| Redis update before DB rollback | Possible divergence; Redis writes not 2PC |
| Access issued before refresh persist | Access issued in same method before/around persist — failure mid-method can leave partial state |
| Mobile retry after timeout | Likely triggers reuse-attack path |
| Web multi-tab refresh | Client single-flight is per tab only; two tabs → two refreshes |

### Recommended deterministic strategy

**Primary:** DB atomic conditional rotate (pessimistic lock **or** optimistic version on session row).  
**Secondary (optional):** Redis lock on `tokenHash`/`sessionId` for short TTL.  
**Never:** check-then-update without race defense.  
**Redis down:** reject refresh with distinct error — **never** call `handleReuseAttack`.

---

## 15. Revocation strategy

### Current responsibilities (overlapping / unclear)

| Mechanism | Used for |
|-----------|----------|
| Access `exp` | Natural expiry |
| jti denylist | Logout current access; prior access on refresh |
| `tokenVersion` | Logout-all, password reset/change, reuse attack |
| Refresh DB row | Session revoke / rotate |
| Redis refresh revoked | Reuse detection trigger |
| Account status | Login only (not refresh) |

### Recommended non-duplicated model

| Mechanism | Sole responsibility |
|-----------|---------------------|
| Short access TTL | Bound stolen-access window |
| `tokenVersion` | **Global user** invalidation only |
| jti denylist | **Immediate individual access** kill (current logout / rotate prior access) |
| Refresh session DB (+ Redis mirror) | Session lifecycle |
| Account status | Enforced on **login and refresh** (and preferably authenticated entry) |

JwtAuthFilter already checks denylist + version — keep. Prod must use fail-closed DB version checks (`fail-open-on-db-error: false`).

---

## 16. Device / risk strategy

### Current

- Login optional `deviceFingerprint`; else hash(UA+IP).
- Flutter sends Android `id` / iOS `identifierForVendor`.
- Fingerprint keys Redis valid slot and is copied on refresh.
- IP/UA stored; **not** hard-checked on refresh (good for mobile IP churn).

### Recommended

- App-generated random UUID, persisted securely, resettable on reinstall.
- Not derived from forbidden stable hardware identifiers as a secret.
- Device id is **not** an authentication factor.
- UA/IP = audit + risk signals only; anomalies may trigger step-up later, not permanent bind.
- Do not permanently break mobile sessions solely because IP changed.

---

## 17. CSRF / XSS strategy

### CSRF (web)

- Spring CSRF **disabled**.
- Refresh and logout are state-changing POSTs; refresh uses cookie.
- Case B + SameSite=Lax reduces cross-site CSRF; still recommend **Origin/Referer allowlist** matching CORS origins on cookie-mutating auth endpoints.
- Case C requires SameSite=None **and** CSRF token or equivalent.
- **CORS is not CSRF defense.**

### XSS (web)

- Refresh HttpOnly: JS cannot read refresh string — **good**.
- Access in localStorage: XSS can steal access — **bad**.
- XSS can still invoke credentialed requests while session cookie exists.
- No CSP in `index.html` / Vite config.
- Remediation: memory access token; CSP; avoid unsafe HTML; treat HttpOnly as necessary but insufficient.

---

## 18. Error model

### Current codes (auth-relevant)

| Code | HTTP | Typical trigger |
|------|------|-----------------|
| `INVALID_CREDENTIALS` | 401 | Bad login |
| `TOKEN_EXPIRED` | 401 | Expired JWT when bubbled |
| `INVALID_TOKEN` | 401 | Missing/invalid refresh, etc. |
| `SECURITY_BREACH` | 401 | Refresh reuse path |
| `ACCOUNT_NOT_VERIFIED` / `USER_NOT_ACTIVE` | 403 | Login gates |
| `UNAUTHORIZED` | 401 | Entry point |
| `ACCESS_DENIED` | 403 | Access denied |
| Password / OTP codes | 400/429 | Change password / OTP |

JwtAuthFilter often swallows invalid/expired access and continues unauthenticated → clients see generic `UNAUTHORIZED` on protected routes.

### Recommended stable codes

| Code | Meaning |
|------|---------|
| `ACCESS_TOKEN_EXPIRED` | Access JWT past exp |
| `REFRESH_TOKEN_EXPIRED` | Refresh past exp |
| `REFRESH_TOKEN_REVOKED` | Session revoked (logout etc.) |
| `REFRESH_TOKEN_REUSED` | Confirmed reuse outside grace |
| `SESSION_REVOKED` | Explicit session/family revoke |
| `DEVICE_RISK_DETECTED` | Risk signal requiring reauth (not IP-only bind) |
| `ACCOUNT_DISABLED` | Non-active account on login/refresh |
| `AUTHENTICATION_REQUIRED` | No valid principal |
| `REFRESH_UNAVAILABLE` | Infra (e.g. Redis) cannot safely evaluate reuse — **not** reuse |

Web and Flutter must map these consistently (today: mostly generic 401 parsing).

### Logging rules

Never log: access tokens, refresh tokens, cookie values, Authorization headers, passwords, full auth request bodies. Prefer userId, jti prefix, error codes. Remove Flutter `TODO(debug-bug2)` verbose refresh debug prints before production hardening.

---

## 19. Database / Redis consistency model

### Current

- DB `access_tokens.isValid()` is required for successful refresh after reuse check.
- Redis revoked key drives reuse escalation (fail-safe true when Redis unhealthy).
- Redis writes are not transactional with DB; failures often logged only.
- `logout-all` Redis `revokeAllForUser` deletes valid keys but does not write every historical hash into revoked set.
- Documented in `AUTH_REDIS_UNAVAILABLE_BEHAVIOR.md` (understates mass revoke on refresh when Redis down).

### Recommended

1. **DB is source of truth** for session validity and rotation.
2. Redis mirrors for speed / denylist / version cache.
3. Order: atomic DB rotate/revoke **commit** → then Redis mirror.
4. Redis unavailable: reject refresh with `REFRESH_UNAVAILABLE` / fail-closed — **never** escalate to reuse-attack.
5. Access denylist fail-open only if `tokenVersion` DB path remains fail-closed in prod.
6. After password change / logout-all, DB revoke + version bump must succeed even if Redis mirror fails.

---

## 20. P0 / P1 / P2 remediation backlog

### P0 — blocks enterprise claim / SAFE TO IMPLEMENT

1. Widen refresh cookie Path to `/api/v1/auth` (or equivalent covering logout).
2. Cookie clear must match Domain/Path/SameSite/Secure of set.
3. Stop Redis-down → `handleReuseAttack`; distinct fail-closed path.
4. Concurrent refresh / legitimate retry must not revoke-all (grace + atomic rotate).
5. Enforce `UserStatus.ACTIVE` on refresh.
6. Decide and specify dual-transport contract for native (additive) vs cookie web.

### P1 — alignment to target model

7. Web memory-only access token + unconditional silent refresh + wait for restoration.
8. Global logout path on refresh failure (auth state + navigation).
9. React Query `clear()` on logout / account switch.
10. Flutter: refresh in secure storage + memory access + body refresh client.
11. Env-driven SameSite/Secure cookie policy for Case B.
12. Origin/Referer validation on cookie auth POSTs.
13. Session family schema + atomic rotation.
14. Stable error code matrix wired through clients.
15. Remove debug token-adjacent logging.

### P2 — hardening / ops

16. Audit logs for logout, refresh, reuse, logout-all.
17. HTTP rate limits on login/refresh.
18. CSP for admin web.
19. Explicit iOS Keychain accessibility options.
20. Named Flutter env flavors (emulator / device / staging / prod HTTPS).
21. App-generated device UUID.
22. Auth unit/E2E tests for the Phase 11 matrix.
23. Correct admin docs (memory token; SameSite for same-site).
24. Exposed CORS headers / preflight maxAge if needed.

---

## 21. Exact files expected to change (future implementation)

**Not modified by this audit.** When remediation proceeds:

### Backend

- `modules/auth/presentation/AuthController.java` (cookie Path/SameSite/Secure; dual transport)
- New cookie helper using `ResponseCookie`
- `modules/auth/application/AuthCommandService.java` (rotation, grace, status check, Redis-down)
- `modules/auth/domain/model/AccessToken.java` (+ possibly new session entity)
- Redis refresh / revocation repositories
- `config/SecurityConfig.java` / CORS / cookie properties
- `GlobalExceptionHandler` + error codes
- OpenAPI snapshot
- Auth tests

### Web

- `src/lib/auth/tokenStore.ts`
- `src/features/auth/AuthProvider.tsx`
- `src/lib/api/client.ts`
- `src/app/providers.tsx` / query clear hooks
- Auth API + settings for logout-all / change-password (if product requires)
- Docs under `web/docs/`

### Mobile

- `secure_token_storage.dart` (access memory layer + refresh keys)
- `cookie_refresh_interceptor.dart` → native refresh interceptor
- `api_client.dart` / DI
- `device_fingerprint_service.dart`
- `api_config.dart` environments
- Auth repository / tests

---

## 22. Schema migrations expected (future)

**Not generated by this audit.**

Expected Flyway additions when implementing:

- Session/family columns on `access_tokens` **or** new `refresh_sessions` table:
  - `family_id`, `parent_token_id` / `replaced_by`, `used_at`, `client_platform`, optional `version`
- Indexes on `token_hash`, `(user_id, family_id)`, `(user_id, revoked_at)`
- No change required solely for cookie Path/SameSite (config/code only)

---

## 23. Backward-compatibility plan

1. **Cookie Path change:** set new Path cookie on next login/refresh; optionally clear old Path cookie explicitly once to avoid duplicates.
2. **Dual transport:** additive — keep cookie refresh for web; accept refresh body for native when transport header selects native. Old mobile cookie clients continue until deprecation window ends.
3. **Error codes:** add new codes; keep mapping old `INVALID_TOKEN` / `SECURITY_BREACH` during transition.
4. **Family rotation:** migrate existing rows with `family_id = id` (self-family) so one-device sessions remain valid.
5. **Do not** break existing Bearer access tokens mid-TTL except via intentional version bump.

---

## 24. Rollout and rollback strategy

### Rollout order

1. P0 cookie Path + Secure deletion parity (low risk, high fix).
2. Redis-down / reuse-attack separation (prevents accidental mass logout).
3. Atomic rotate + short grace (stop false revoke-all).
4. Refresh ACTIVE check.
5. Dual-transport behind feature flag; ship Flutter secure refresh.
6. Web memory access + QueryClient clear.
7. SameSite=Lax + Origin checks for Case B.
8. Schema family fields when ready.

### Rollback

- Cookie: briefly dual-set old+new Path only if needed; never leave Secure mismatch.
- Feature-flag dual transport off → cookie-only (accept temporary Flutter jar behavior).
- Rotation grace: disable grace only if abuse observed; prefer family revoke over user-wide.
- DB migrations: expand-contract; avoid destructive drops until clients cut over.

---

## 25. Test plan

### Matrix (required scenarios)

| # | Scenario | Backend | Web | Flutter |
|---|----------|---------|-----|---------|
| 1 | Web login sets HttpOnly refresh cookie | Assert Set-Cookie flags | Assert no refresh in JS | n/a |
| 2 | Web JS cannot read refresh token | — | `document.cookie` absence | n/a |
| 3 | Web reload restores via refresh | Cookie valid | Silent refresh path | n/a |
| 4 | Protected requests wait for restoration | — | Guard/bootstrap race test | n/a |
| 5 | Cross-origin credential CORS pass | Allowlisted origin | withCredentials | n/a |
| 6 | Unapproved origin rejected | Negative CORS | — | n/a |
| 7 | Web concurrent 401 → one refresh | Optional | Interceptor spy | n/a |
| 8 | Flutter login native refresh contract | Body/cookie dual | n/a | Assert refresh stored securely |
| 9 | Flutter refresh in secure storage | — | — | No cookie-jar refresh secret |
| 10 | Flutter cold start restores | — | — | restoreSession IT |
| 11 | Flutter concurrent 401 → one refresh | — | — | Interceptor test |
| 12 | Rotation invalidates old token | Unit/IT | — | — |
| 13 | Legitimate duplicate refresh no revoke-all | Concurrency IT | — | Retry simulation |
| 14 | Confirmed reuse → documented response | Unit | Map error code | Map error code |
| 15 | Logout revokes current session only | Cookie received | — | — |
| 16 | Logout-all revokes every session | IT | — | clearSession |
| 17 | Change password invalidates all | IT | — | Wipe + reauth |
| 18 | Disabled account cannot refresh | Unit | — | — |
| 19 | DB/Redis partial failure deterministic | IT + Redis down | — | — |
| 20 | Tokens absent from logs | Log assert | — | Log assert |
| 21 | Cookie deletion identical Domain/Path/SameSite/Secure | Controller test | — | — |
| 22 | Physical device no browser cookie dependency | Dual transport | — | Secure storage only |

### Existing coverage to extend

- `AuthCommandServiceTest` (reuse, rotation) — add concurrency + Redis-down + ACTIVE.
- `AuthControllerTest` — Path/SameSite/Secure set+clear.
- `CorsConfigurationTest` — keep.
- Web: add vitest for tokenStore memory + interceptor single-flight.
- Flutter: add interceptor single-flight + storage location tests.

---

## 26. Non-goals

- This audit does **not** modify source code, generate Flyway migrations, install dependencies, move files, or change live API contracts.
- Does not claim READY based only on where tokens are stored.
- Does not require IP hard-binding for mobile.
- Does not mandate SameSite=None for same-site FaceWashFox deployments.
- Does not treat `X-Client-Type` / User-Agent as authorization.
- Does not recommend revoke-all on every repeated refresh without grace/atomic strategy.
- Does not expand product scope (new admin features) beyond what auth alignment requires.
- Physical device MDM / certificate pinning is out of scope unless separately chartered.

---

## Appendix A — Source inventory completeness checklist

### Backend inspected

SecurityConfig, CORS, JwtAuthFilter, JwtProvider, claims, jti denylist, tokenVersion, AccessToken model, Redis refresh/revocation, login/refresh/logout/logout-all, password reset/change, device fingerprint, cookie set/clear, GlobalExceptionHandler, audit (partial), OTP rate limits, Flyway V1/V8/V12, auth tests, OpenAPI.

### Web inspected

Vite SPA, Axios credentials, localStorage access, cookie refresh assumption, AuthProvider bootstrap, QueryClient (no clear), RequireAuth, concurrent 401, logout only, env, no CSRF/CSP, no auth tests.

### Flutter inspected

Dio, interceptors, flutter_secure_storage (access only), PersistCookieJar refresh, restore, single-flight, logout/logout-all/change-password, device fingerprint, ApiConfig, debug logging, partial tests.

---

## Appendix B — Correction checklist (validated)

| Correction | Applied finding |
|------------|-----------------|
| Cross-origin ≠ cross-site | FaceWashFox subdomains = Case B |
| SameSite=None only for true cross-site | Do not use for Case B; staging docs wrong |
| Client-type headers spoofable | No transport header today; future header must not authorize |
| IP not hard device bind | IP stored, not enforced on refresh (keep) |
| Reuse detection must tolerate races | **Current fails this** — P0 |
| Access blacklist vs tokenVersion | Directionally split; document and avoid Redis-down misuse |

---

*End of alignment plan. Implementation workstreams must treat SAFE TO IMPLEMENT as BLOCKED until P0 items are resolved.*
