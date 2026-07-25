# Exotic Stamp Backend — Remediation Plan (v2)

**Audit version:** 2  
**Revised:** 2026-07-23  
**Companion:** `BACKEND_PRODUCTION_READINESS_AUDIT.md` v2  

**Batch A status (2026-07-23):** Code-side items **R-P0-01**, **R-P0-02**, **R-P0-03** (bound props only), **R-P0-08**, **R-P0-09** implemented. External secret **rotation** remains an ops action (not claimed complete). See `BATCH_A_IMPLEMENTATION_REPORT.md`.

**Batch B status (2026-07-23):** **R-P0-05** and **R-P1-10** remain **PARTIAL**. Batch B.1 fixed Testcontainers/Docker API and proved Failsafe executes ITs locally under `-Pci` with assert PASS; coverage gates and green GHA still open. See `BATCH_B_IMPLEMENTATION_REPORT.md` and `BATCH_B1_IMPLEMENTATION_REPORT.md`.

**Batch B.1 status (2026-07-23):** **PARTIAL** — Docker ITs execute (TC 1.21.4 + API 1.44); Failsafe 29/0 skipped under `-Pci`; assert manifest PASS; JaCoCo overall 53%/40% (gates fail); GHA not proven. See `BATCH_B1_IMPLEMENTATION_REPORT.md`.

**Batch C status (2026-07-24):** **R-P0-04**, **R-P1-05**, **R-P1-06**, **R-P1-07**, **R-P1-08** **DONE**. Security filter order defect from B.1 fixed; `ExcludeRateLimitFilterInitializer` removed; `SecurityFilterChainIT` proves real `RateLimitFilter` boots. See `BATCH_C_IMPLEMENTATION_REPORT.md` and `docs/security/RUNTIME_SECURITY_POLICY.md`.

**Batch D status (2026-07-24):** **R-P0-06**, **R-P0-07a–c** **DONE** (code). **R-P0-07d** **PARTIAL** (Caddyfile example + compose; host validate/smoke remaining). Lightsail↔S3 credential mechanism remains an ops decision. See `BATCH_D_IMPLEMENTATION_REPORT.md`.

**Batch F.0 status (2026-07-25):** Deployment preparation **DONE** for scripts/docs/local validation tooling. **R-P0-05 remains PARTIAL** (GitHub Actions green run not observed; `gh` unavailable locally). AWS staging resources **not created** (STOP POINT). See `BATCH_F0_IMPLEMENTATION_REPORT.md`.

**Batch E.2 status (2026-07-25):** **R-P1-10 DONE** (all JaCoCo gates pass locally under -Pci; thresholds unchanged). **R-P0-05 PARTIAL** until green GitHub Actions run on exotic is verified. See BATCH_E2_IMPLEMENTATION_REPORT.md.

**Batch E.1 status (2026-07-24):** Closes Batch E gaps for R-P1-01 (pending-stock + multi-instance lock) and R-P1-03 (fingerprint conflicts). See `BATCH_E1_IMPLEMENTATION_REPORT.md`.

**Batch E status (2026-07-24):** **R-P1-01**, **R-P1-02**, **R-P1-03**, **R-P1-09** **DONE**. **R-P1-10** remains **PARTIAL** (meaningful reward/collection tests added; JaCoCo gates still open; thresholds not lowered). See `BATCH_E_IMPLEMENTATION_REPORT.md`.

### v2 removals / priority changes

| Item | Change |
|------|--------|
| **R-P1-04** | **Removed** (was single-ACTIVE NFC unique / supersede-all) — contradicts OpenAPI multi-gate NFC |
| **F-004 / R-P0-05** | Remains **P0** but framed as **release-governance** (severity HIGH, not runtime BLOCKER) |
| **F-005** | Downgraded effort urgency (MEDIUM); align nested `.gitignore` |
| **F-010** | Split remediations: R-P0-06 (storage), R-P0-07a–d (shutdown, JVM, dockerignore, Caddy) |
| **R-P0-04** | Composite rate-limit keys + `Retry-After` |
| **R-P1-07** | Per-flow Redis matrix (OTP fail-closed) |
| **R-P1-01** | Compare sync / async+reconcile / outbox; **recommend async+reconcile for MVP** |
| **R-P1-04 (new)** | NFC lifecycle hardening (not single-ACTIVE) — optional P1 |
| **R-P0-08** | Remove tracked `.m2` (F-037) |

Priority bands: **P0** before Internet-facing or production release gates; **P1** before real-user launch; **P2** shortly after; **P3** optional.

---

## P0 — Before Internet-facing deployment / production release

### R-P0-01 — Rotate and remove committed secrets (F-001) — PARTIAL (code done)

| Field | Value |
|-------|-------|
| Finding IDs | F-001 |
| Status | **Code-side complete (Batch A).** YAML secret defaults removed. **External rotation of previously exposed credentials remains mandatory ops work** — not completed by this batch. |
| Files | `application-dev.yml`; `application.yml`; `.env.example` |
| Minimal safe change | Delete YAML secret defaults; rotate JWT/SMTP/DB/Redis if ever matched; require env |
| Test | Missing `JWT_SECRET` fails boot; `JwtSecretRequiredTest` |
| Compatibility | Local `.env` now required for secrets under `dev` |
| Data migration | None |
| Rollback | Restore prior YAML only if emergency; never restore real secret defaults to git |
| Complexity | S |
| Dependencies | None |

### R-P0-02 — Gate production seeders (F-002) — DONE (Batch A)

| Field | Value |
|-------|-------|
| Finding IDs | F-002 |
| Status | **Complete.** `MetroLineSeeder` and `CollectionBootstrapper` are `@Profile("dev")`. Fresh prod data via admin APIs + Flyway RBAC seeds (V8+). |
| Files | `MetroLineSeeder.java`; `CollectionBootstrapper.java` |
| Test | `DevOnlySeederProfileTest`; `ProdSeederExclusionIT` (Docker) |
| Complexity | XS |
| Dependencies | None |

### R-P0-03 — Prod fail-fast + path/URL hygiene (F-011, F-015, F-028) — DONE for bound props (Batch A)

| Field | Value |
|-------|-------|
| Finding IDs | F-011, F-015, F-028 |
| Status | **Complete for currently bound properties.** `ProdStartupValidator` + issuer `exotic-stamp`. JWT Base64/length harden remains **R-P1-06**. S3 env requirements remain Batch D. |
| Files | `ProdStartupValidator.java`; `ApplicationSiteProperties.java`; `application.yml` |
| Test | `ProdStartupValidatorTest`; `ProdConfigurationFailFastTest`; `CorsPropertiesWildcardTest` |
| Complexity | M |
| Dependencies | R-P0-01 |

### R-P0-04 — Composite rate limits + Retry-After (F-006) — DONE (Batch C)

| Field | Value |
|-------|-------|
| Finding IDs | F-006 |
| Status | **Complete.** Redis Lua token-bucket rate limits with composite HMAC keys; HTTP 429 + `Retry-After`; fail-closed 503 on Redis outage in prod. Memory backend forbidden in prod. |
| Files | `infra/security/ratelimit/*`; `SecurityConfig`; `GlobalExceptionHandler`; `ProdStartupValidator`; `.env.example` |
| Test | `InMemoryRateLimiterTest`; `RateLimitKeyHasherTest`; `ClientIpResolverTest`; `RedisLuaRateLimiterIT` (Docker) |
| Complexity | M |
| Dependencies | None |
| Test | Burst → 429 with Retry-After header |
| Compatibility | Clients must honor 429 |
| Complexity | M |
| Dependencies | Redis preferred; Caddy edge can complement |

### R-P0-05 — CI governance with Failsafe ITs (F-004, F-019) — P0 release gate — PARTIAL (Batch B)

| Field | Value |
|-------|-------|
| Finding IDs | F-004 (HIGH governance), F-019 |
| Status | **PARTIAL (Batch B + B.1).** Failsafe executes **29** ITs with **0** skipped under `mvn verify -Pci` when Docker is available (Testcontainers 1.21.4 + Docker API 1.44). Manifest assert PASS. **Not complete:** GitHub Actions green run not proven; JaCoCo gates still fail (see R-P1-10). |
| Files | `.github/workflows/backend-ci.yml`; `pom.xml` (Surefire/Failsafe/JaCoCo/Enforcer/Testcontainers); `scripts/ci/assert_test_reports.py`; `scripts/ci/expected_integration_tests.txt` |
| Remaining | Green GHA `mvn verify -Pci` with assert PASS **and** coverage gates (R-P1-10) |
| Compatibility | None for runtime |
| Complexity | M |
| Dependencies | None |
| Note | Mandatory before **production release**; not itself a runtime source defect. See `BATCH_B1_IMPLEMENTATION_REPORT.md`. |

### R-P0-06 — Production object storage posture (F-003, F-010) — DONE (Batch D code)

| Field | Value |
|-------|-------|
| Finding IDs | F-003, F-010 |
| Status | **Code complete (Batch D).** AWS SDK v2 `S3StorageService`; prod requires `STORAGE_PROVIDER=s3` + bucket/region/`STORAGE_PUBLIC_BASE_URL`; `stored_assets` + orphan cleanup; LocalStack IT present. **External:** Lightsail↔S3 credential mechanism must still be validated by ops (do not assume EC2 instance profile). |
| Files | `infra/storage/**`; `V21__stored_assets.sql`; `ProdStartupValidator`; `.env.example`; S3/Lightsail runbooks |
| Test | Unit key/orphan/replace; `S3StorageLocalStackIT`; `FlywayV21MigrationIT` |
| Compatibility | Legacy URL columns retained; public URL = base + object_key |
| Data migration | Additive V21; widen URL columns to 512 |
| Complexity | L |
| Dependencies | Ops credential decision |

### R-P0-07a — Graceful shutdown (F-031) — DONE (Batch D)

| Field | Value |
|-------|-------|
| Finding IDs | F-031 |
| Status | **Complete.** `server.shutdown=graceful` + bounded lifecycle timeout; orphan job respects shutdown; smoke scripts added. |
| Files | `application.yml`; `application-prod.yml`; `scripts/ci/graceful_shutdown_smoke.*` |
| Test | Smoke script / SIGTERM |
| Complexity | XS |

### R-P0-07b — JVM / container memory (F-032) — DONE (Batch D)

| Field | Value |
|-------|-------|
| Finding IDs | F-032 |
| Status | **Complete.** `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=75.0` in Dockerfile (override per Lightsail plan). |
| Files | `Dockerfile` |
| Complexity | XS |

### R-P0-07c — `.dockerignore` (F-033) — DONE (Batch D)

| Field | Value |
|-------|-------|
| Finding IDs | F-033 |
| Status | **Complete.** `.dockerignore` excludes `.git`, `.m2`, `target`, `.env`, uploads, docs, IDE junk. |
| Files | `.dockerignore` |
| Complexity | XS |

### R-P0-07d — Caddy / runtime checklist (F-034) — PARTIAL (Batch D)

| Field | Value |
|-------|-------|
| Finding IDs | F-034 |
| Status | **PARTIAL.** Example Caddyfile + network runbook + prod-like compose (backend unpublished). Syntax validation + staging HTTPS smoke still required on target host. |
| Files | `infra/caddy/Caddyfile.example`; `docker-compose.prod-like.yml`; `LIGHTSAIL_NETWORK_AND_CADDY_RUNBOOK.md` |
| Remaining | `caddy validate` on deploy host; staging TLS cookie/CORS smoke |
| Complexity | S |
| Dependencies | R-P0-03 |

### R-P0-08 — Untrack `.m2` from backend git (F-037) — DONE (Batch A)

| Field | Value |
|-------|-------|
| Finding IDs | F-037 |
| Status | **Complete.** Removed **2086** tracked `.m2/**` paths from the backend git index (`git rm -r --cached .m2`). History not rewritten. |
| Test | `git ls-files '.m2/**'` → empty |
| Complexity | S |
| Dependencies | None |

### R-P0-09 — Nested `.gitignore` alignment (F-005) — DONE (Batch A)

| Field | Value |
|-------|-------|
| Finding IDs | F-005 (MEDIUM) |
| Status | **Complete.** Backend `.gitignore` ignores `.env`, `.env.*` (except `.env.example`), `uploads/**`, `*.log`. |
| Test | `git check-ignore -v .env` succeeds; `.env.example` trackable |
| Complexity | XS |
| Dependencies | None |

---

## P1 — Before real-user production launch

### R-P1-01 — Reliable reward completion (F-009) — DONE (Batch E + E.1)

| Field | Value |
|-------|-------|
| Finding IDs | F-009 |
| Status | **Complete (Option B + E.1).** Missing-reward reconcile + PENDING_STOCK fulfill; PostgreSQL advisory lock; admin dry-run/busy; issued-only events. |
| Options | **A** sync in collect TX · **B** async + idempotent reconciliation · **C** transactional outbox |
| **MVP recommendation (3–5k users)** | **B** — keep async listener; add scheduled/admin reconcile for stamps lacking expected rewards; rely on `uq_user_rewards_once` |
| When to choose C | If missed events become ops-painful or multi-instance workers need durable delivery |
| When to choose A | If collect latency budget allows and domain coupling is acceptable |
| Files (for B) | `RewardReconcileService`; `RewardReconcileCandidateRepository`; `RewardProperties`; admin reconcile API |
| Test | `RewardReconcileIT` — stamps without reward → reconcile issues once; second pass empty |
| Complexity | M (B) / L (C) / S–M (A) |
| Dependencies | None |

### R-P1-02 — Voucher uniqueness both directions (F-008) — DONE (Batch E)

| Field | Value |
|-------|-------|
| Finding IDs | F-008 |
| Status | **Complete.** Partial unique `uq_user_rewards_voucher_pool_id` on `user_rewards(voucher_pool_id) WHERE NOT NULL` (pairs with V16 `uq_vp_assigned_user_reward`). Evaluation releases voucher on link unique race. |
| Files | `V22__reward_voucher_and_campaign_integrity.sql`; `RewardEvaluationService` |
| Minimal safe change | Partial unique on `user_rewards(voucher_pool_id) WHERE voucher_pool_id IS NOT NULL` |
| Test | `FlywayV22MigrationIT` duplicate link rejected; unit voucher-link race |
| Data migration | Clean duplicates first (none expected) |
| Complexity | S |
| Dependencies | Data audit |

### R-P1-03 — Idempotency alignment (F-016) — DONE (Batch E + E.1)

| Field | Value |
|-------|-------|
| Finding IDs | F-016 |
| Status | **Complete (E.1).** SHA-256 collect fingerprint; same key different logical payload → `IDEMPOTENCY_CONFLICT`; legacy null fingerprint compares station/campaign only. |
| Files | `CollectionPolicyService`; `CollectionCommandService` |
| Minimal safe change | Map `uq_user_stamps_user_idempotency` to idempotent replay; document permanent per-user key uniqueness |
| Test | `CollectionCommandServiceTest` idempotency unique race |
| Complexity | M |
| Dependencies | None |

### R-P1-04 — NFC lifecycle hardening (F-036) — **replaces removed single-ACTIVE work**

| Field | Value |
|-------|-------|
| Finding IDs | F-036 |
| **Not in scope** | Forcing one ACTIVE NFC per station (OpenAPI allows multi-gate) |
| Files | `StationScanKeyCommandService`; domain transition guards; optional audit query; QR policy doc |
| Minimal safe change | 1. Enforce full NFC state machine transitions |
| | 2. Never re-expose raw/`payloadToWrite` after create |
| | 3. Concurrent activate same key → optimistic lock 409 (verify IT) |
| | 4. Require/encourage `label`/`placementNote` for multi-gate installs |
| | 5. Audit trail queryable for create/activate/revoke/lost/verify |
| | 6. Document **QR policy separately** (static vs dynamic placeholder) |
| Test | Lifecycle IT; create returns payload once; list has no raw |
| Complexity | M |
| Dependencies | None |

### R-P1-05 — Upload magic-byte validation (F-012) — DONE (Batch C)

| Field | Value |
|-------|-------|
| Finding IDs | F-012 |
| Status | **Complete.** Magic-byte detection (JPEG/PNG/WebP), MIME match, dimension/pixel limits; GENERIC no longer MIME-only. |
| Files | `FileValidator.java`; `StorageProperties` |
| Test | `FileValidatorTest` (spoof/truncated/oversized/path traversal) |
| Complexity | S |
| Dependencies | R-P0-06 preferred for object storage later |

### R-P1-06 — JWT claim hardening (F-013, F-014, F-028) — DONE (Batch C)

| Field | Value |
|-------|-------|
| Finding IDs | F-013, F-014, F-028 |
| Status | **Complete.** Base64 secret ≥32 decoded bytes; issuer + clock skew; ACCESS/REFRESH separation. Audience deferred (not currently emitted). |
| Files | `JwtProvider`; `JwtProperties`; `JwtSecretValidator`; `ProdStartupValidator` |
| Test | `JwtProviderTest`; `JwtSecretRequiredTest`; `ProdStartupValidatorTest` |
| Complexity | S |
| Dependencies | R-P0-01 |

### R-P1-07 — Redis per-flow outage matrix (F-007, F-025) — DONE (Batch C)

| Field | Value |
|-------|-------|
| Finding IDs | F-007, F-025 |
| Status | **Complete.** OTP fail-closed; rate-limit fail-closed; denylist unavailable → 503; public caches soft-miss; refresh DB SoT. Documented in `RUNTIME_SECURITY_POLICY.md`. |
| Files | `RedisKeyValueSupport`; `OtpRepository`; revocation Redis/validator; `JwtAuthFilter`; `AuthCommandService` |
| Test | `AccessTokenRevocationValidatorTest`; `AuthCommandServiceTest` OTP paths |
| Complexity | M |
| Dependencies | R-P0-04 |

### R-P1-08 — Swagger permitAll defense-in-depth (F-021) — DONE (Batch C)

| Field | Value |
|-------|-------|
| Finding IDs | F-021 |
| Status | **Complete.** Docs permitted only when springdoc enabled and profile is not `prod`; otherwise `denyAll`. |
| Files | `SecurityConfig.java` |
| Test | `SecurityConfigSwaggerTest`; `ProdSwaggerDisabledIT` (Docker) |
| Complexity | XS |
| Dependencies | None |

### R-P1-09 — Soft-delete-aware default campaign unique (F-024) — DONE (Batch E)

| Field | Value |
|-------|-------|
| Finding IDs | F-024 |
| Status | **Complete.** V22 recreates `uq_campaigns_default_per_line` with `deleted_at IS NULL`; `existsActiveDefaultByLineId`; soft-delete clears `isDefault`. |
| Files | `V22__…integrity.sql`; `JpaCampaignRepository`; `CampaignCommandService` |
| Minimal safe change | Partial unique `WHERE deleted_at IS NULL` |
| Complexity | S |
| Dependencies | None |

### R-P1-10 — Coverage + critical IT gaps (F-019) — PARTIAL (Batch E.1)

| Field | Value |
|-------|-------|
| Finding IDs | F-019 |
| Status | **PARTIAL (Batch B + B.1 + E + E.1).** Overall ~53.9%/40.7%; reward ~38.3%/28%. Thresholds **not** lowered. Gates still fail. |
| Files | `pom.xml` (JaCoCo + `ci` check); Failsafe IT manifest; Batch E reward/collection tests |
| Complexity | M |
| Dependencies | R-P0-05 |

### R-P1-11 — Observability baseline (F-018)

| Field | Value |
|-------|-------|
| Finding IDs | F-018 |
| Files | `logback-spring.xml`; request-id filter |
| Minimal safe change | JSON prod logs; MDC correlation; redaction |
| Complexity | M |
| Dependencies | None |

### R-P1-12 — Vercel cookie/CORS (F-015, F-022)

| Field | Value |
|-------|-------|
| Finding IDs | F-015, F-022 |
| Files | prod cookie/CORS env |
| Minimal safe change | Exact Vercel origins; SameSite decision for admin |
| Complexity | S |
| Dependencies | R-P0-03 |

---

## P2 — Shortly after launch

| ID | Findings | Change | Complexity |
|----|----------|--------|------------|
| R-P2-01 | F-023 | Remove unused Cloudinary/MySQL (keep Bucket4j if used by R-P0-04) | XS |
| R-P2-02 | F-027 | Hikari/Tomcat/async tuning + staging load tests | M |
| R-P2-03 | F-004 remaining | Staging→manual prod deploy, checksum, rollback | L |
| R-P2-04 | F-036 | NFC audit retention / admin audit UI | S |

### Performance scenarios (staging only)

Unchanged intent from v1: baseline browse, login burst, scan burst, idempotent retry, concurrent collect, voucher race, stamp-book read, S3 upload, Redis down, slow DB, graceful restart. Do **not** run against production.

---

## P3 — Optional

| ID | Change | Complexity |
|----|--------|------------|
| R-P3-01 | Split persistence entities from domain (F-020) | XL |
| R-P3-02 | Remove unused cache TTL keys (F-029) | XS |
| R-P3-03 | Argon2 password hashing migration | L |
| R-P3-04 | Upgrade reconcile (B) → transactional outbox (C) | L |
| R-P3-05 | OpenTelemetry | M |

---

## Suggested order

1. R-P0-01, R-P0-02, R-P0-08, R-P0-09  
2. R-P0-03, R-P0-05 (CI governance) in parallel with R-P0-04  
3. R-P0-06 + R-P0-07a–d  
4. P1 integrity (R-P1-01 B, R-P1-02, R-P1-03) and NFC R-P1-04  
5. R-P1-05–12  
6. P2 load test + deploy automation  

**Re-audit gate:** After P0+P1, reassess for CONDITIONALLY READY.
