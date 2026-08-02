# Batch C Implementation Report — Runtime Security Hardening and Security Filter Repair

**Date:** 2026-07-24  
**Scope:** R-P0-04, R-P1-05, R-P1-06, R-P1-07, R-P1-08 + SecurityConfig filter-order defect from B.1  
**Verdict:** **PASS** (security remediations + filter repair verified; R-P1-10 coverage gates remain PARTIAL / out of Batch C scope to lower)

---

## 1. Verdict

| Field | Value |
|-------|-------|
| **Verdict** | **PASS** |
| Notes | Full Spring context boots with **real** `RateLimitFilter`. `ExcludeRateLimitFilterInitializer` **removed**. Surefire **504**, Failsafe **31** / **0** skipped under `-Pci`. Assert manifest **PASS**. JaCoCo overall still below gate (~53%/40%) — thresholds **not** lowered; R-P1-10 stays PARTIAL. |

---

## 2. Security filter defect — root cause and fix

| Item | Detail |
|------|--------|
| Root cause | `addFilterBefore(rateLimit, JwtAuthFilter.class)` ran **before** `JwtAuthFilter` was registered on `HttpSecurity` → Spring Security threw `JwtAuthFilter does not have a registered order` |
| Intended order | `CookieAuthOriginFilter` → `JwtAuthFilter` → `RateLimitFilter` → … |
| Why RateLimit after JWT | `COLLECT` policy hashes authenticated user id from `SecurityContext`; public policies (login/scan) do not need auth |
| Fix | Register JWT first; `http.addFilterAfter(rateLimitFilter, JwtAuthFilter.class)` |
| Double registration | `FilterRegistrationBean<JwtAuthFilter>` + existing RateLimit `FilterRegistrationBean` both `setEnabled(false)` — filters run **once** in the Security chain only |
| Proof | Debug log: `CookieAuthOriginFilter, JwtAuthFilter, RateLimitFilter, …`; `SecurityFilterChainIT` asserts RateLimit index > Jwt index and single instances |

---

## 3. Removed test workaround

Deleted `src/test/java/metro/ExoticStamp/support/ExcludeRateLimitFilterInitializer.java`.  
Removed `@ContextConfiguration(initializers = …)` from all SpringBoot ITs.

---

## 4. Exact files changed (this closure)

### Production
- `SecurityConfig.java` — filter order + JwtAuthFilter registration bean

### Tests / CI
- `SecurityFilterChainIT.java` — **new**
- `SecurityFilterOrderTest.java` — **new**
- SpringBoot ITs (Application, ProdSeeder, ProdSwagger, Upload) — no Exclude initializer
- `ProdSwaggerDisabledIT` — HTTP denial assertions (401/403/404)
- `scripts/ci/expected_integration_tests.txt` — includes `SecurityFilterChainIT`
- Deleted `ExcludeRateLimitFilterInitializer.java`

### Docs
- This report; `RUNTIME_SECURITY_POLICY.md`; `BACKEND_REMEDIATION_PLAN.md`

Prior Batch C security packages (rate limit, JWT, upload, Redis fail-closed, Swagger denyAll) remain in place from earlier Batch C work.

---

## 5–10. Rate limit / IP / 429 (unchanged design, verified)

| Topic | Choice |
|-------|--------|
| Implementation | Redis Lua token bucket (`RedisLuaRateLimiter`); memory only non-prod |
| Atomicity | Single Lua `EVAL` |
| Keys | `rl:v1:{policy}:{ip}:{hmac…}`; HMAC-SHA256 peppered; no raw email/phone/JWT/NFC/idempotency |
| Pepper | `RATE_LIMIT_KEY_PEPPER` mandatory in prod (`ProdStartupValidator`) |
| IP | `ClientIpResolver` → `remoteAddr` after framework forwarded-headers; no blind XFF |
| 429 | `RATE_LIMIT_EXCEEDED` + `Retry-After` |
| Redis down | `SECURITY_DEPENDENCY_UNAVAILABLE` → **503** (not 401) |

Policies: login, register, otp-issue, otp-verify, refresh, scan-resolve, collect.

---

## 11–16. Redis OTP / JWT / upload / Swagger

See `docs/security/RUNTIME_SECURITY_POLICY.md` (OTP fail-closed, denylist 503, JWT Base64≥32, upload magic bytes, Swagger denyAll in prod).

---

## 17–18. Test counts (`mvn clean verify -Pci`)

| Suite | Executed | Skipped |
|-------|----------|---------|
| Surefire | **504** | 0 |
| Failsafe | **31** | **0** |
| Assert manifest | **13/13** executed | |

---

## 19. Coverage (after this run)

| Scope | LINE | BRANCH |
|-------|------|--------|
| Overall | 53.40% | 40.34% |
| Auth | 60.03% | 53.62% |
| Collection | 64.54% | 46.47% |
| Reward | 35.52% | 25.22% |

Gate under `-Pci`: **FAIL** (expected; not weakened). R-P1-10 remains PARTIAL.

---

## 20. GitHub Actions

Not re-observed in this session. Push Batch C filter-fix branch separately if required. Local `-Pci` test execution is green aside from JaCoCo gate.

---

## 21–26. Compatibility / rollback / blockers

- Clients: honor 429/`Retry-After` and 503 security dependency codes.
- Operators: `JWT_SECRET` Base64≥32 bytes; `RATE_LIMIT_KEY_PEPPER`.
- Rollback: revert `SecurityConfig` filter-order commit (restores broken startup if RateLimitFilter present).
- Remaining: R-P0-05 GHA green; R-P1-10 coverage; Batch D S3; etc.
- **Unchanged:** Flyway, NFC, collection idempotency, reward/voucher semantics, S3, Dockerfile, Caddy, API success DTOs, JaCoCo thresholds.

---

## Remediations marked complete

| ID | Status |
|----|--------|
| R-P0-04 | **DONE** (incl. filter chain boots with real limiter) |
| R-P1-05 | **DONE** |
| R-P1-06 | **DONE** |
| R-P1-07 | **DONE** |
| R-P1-08 | **DONE** (prod IT exercises chain) |
