# Batch A Implementation Report — Production Readiness Remediation

**Date:** 2026-07-23  
**Scope:** R-P0-01 (code-side), R-P0-02, R-P0-03 (bound properties), R-P0-08, R-P0-09  
**Verdict:** **PASS** (with external secret rotation still outstanding)

---

## 1. Git repository ownership

| Location | `git rev-parse --show-toplevel` | Remotes | Notes |
|----------|----------------------------------|---------|-------|
| `D:/Part-time/ExoticStamp` | `D:/Part-time/ExoticStamp` | `origin` → `https://github.com/andrewle-dev/exotic_stamp.git` | Monorepo; tracks `backend/**` as normal files (not a git submodule; no `.gitmodules`) |
| `D:/Part-time/ExoticStamp/backend` | `D:/Part-time/ExoticStamp/backend` | `origin` → `https://github.com/itdept-studio/EXOTIC_PC.git`; `exotic` → `https://github.com/itdept-studio/EXOTIC_STAMP.git` | **Nested git directory** (`.git` is a directory with Hidden attribute). Owns backend Maven history including previously tracked `.m2` |

**Backend source ownership for Batch A hygiene:** the **nested backend git repository** (`D:/Part-time/ExoticStamp/backend`). Index changes (`.m2` untrack, `.gitignore`) were applied there.

**Where `.github/workflows` must live (future Batch B):**

- For CI against the nested backend remotes (`EXOTIC_STAMP` / `EXOTIC_PC`): **`backend/.github/workflows`** (repo root of the nested backend git).
- If the monorepo remote (`exotic_stamp`) is the deployment source of truth, also add workflows under the **parent** repo that build `backend/` — do not assume a single remote.

Neither repository was deleted, moved, or flattened.

---

## 2. Remediation IDs completed

| ID | Status | Notes |
|----|--------|-------|
| **R-P0-01** | **PARTIAL** | Code-side secret defaults removed. **External rotation not performed / not claimed.** |
| **R-P0-02** | **DONE** | Seeders gated with `@Profile("dev")` |
| **R-P0-03** | **DONE** (bound props) | `ProdStartupValidator`; issuer → `exotic-stamp`; Windows path default removed. JWT Base64 length harden deferred to **R-P1-06**. |
| **R-P0-08** | **DONE** | 2086 `.m2` paths removed from index |
| **R-P0-09** | **DONE** | Backend `.gitignore` aligned |

---

## 3. Exact files changed

### Configuration / source
- `src/main/resources/application-dev.yml` — removed secret defaults; require env for secrets
- `src/main/resources/application.yml` — issuer `exotic-stamp`; portable `./uploads` default; `MAIL_HOST`/`MAIL_PORT` env
- `src/main/java/metro/ExoticStamp/ExoticStampApplication.java` — enable `ApplicationSiteProperties`
- `src/main/java/metro/ExoticStamp/config/ApplicationSiteProperties.java` — **new**
- `src/main/java/metro/ExoticStamp/config/ProdStartupValidator.java` — **new**
- `src/main/java/metro/ExoticStamp/config/CorsProperties.java` — public PostConstruct for tests
- `src/main/java/metro/ExoticStamp/modules/metro/infrastructure/seeder/MetroLineSeeder.java` — `@Profile("dev")`
- `src/main/java/metro/ExoticStamp/modules/collection/infrastructure/bootstrap/CollectionBootstrapper.java` — `@Profile("dev")`
- `src/main/java/metro/ExoticStamp/config/MvpDemoSeedBootstrap.java` — no password default

### Tests
- `src/test/java/metro/ExoticStamp/config/JwtSecretRequiredTest.java`
- `src/test/java/metro/ExoticStamp/config/ProdStartupValidatorTest.java` — **new**
- `src/test/java/metro/ExoticStamp/config/ProdConfigurationFailFastTest.java` — **new**
- `src/test/java/metro/ExoticStamp/config/CorsPropertiesWildcardTest.java` — **new**
- `src/test/java/metro/ExoticStamp/config/ProdSeederExclusionIT.java` — **new** (Docker)
- `src/test/java/metro/ExoticStamp/modules/metro/infrastructure/seeder/DevOnlySeederProfileTest.java` — **new**
- `src/test/java/metro/ExoticStamp/config/ProdSwaggerDisabledTest.java` — CORS/storage/MAIL_FROM props
- `src/test/resources/application.properties` — demo-user-password fixture

### Git hygiene / docs
- `.gitignore`
- `.env.example`
- `uploads/.gitkeep` — **new**
- `docs/deployment/BACKEND_REMEDIATION_PLAN.md` — marked Batch A items
- `docs/deployment/BATCH_A_IMPLEMENTATION_REPORT.md` — this file
- Git index: `git rm -r --cached .m2` (**2086** paths)

### Unchanged (confirmed)
- Flyway migrations
- API contracts / OpenAPI behavior
- NFC / reward / collection business logic (aside from seeder profile)
- S3, rate limiting, Redis fail policy, Failsafe, GitHub Actions, Dockerfile, JaCoCo, dependency upgrades

---

## 4. External secret rotations still required (ops)

These are **operational** actions. This batch does **not** claim they were completed.

1. **SMTP app password** previously embedded as a YAML default in `application-dev.yml` — revoke/rotate at the mail provider; update server `.env` only.
2. **JWT_SECRET** previously defaulted in `application-dev.yml` — generate new ≥32 random bytes (then Base64 if desired); restart all instances; expect all sessions invalid.
3. **DB_PASSWORD** if the weak default was ever used against a shared DB — rotate DB role password; update env.
4. **Redis password** if `change_me` (or similar) was used with `requirepass` — rotate Redis ACL/password; update env.
5. **Historical Git remote exposure** — treat any remote that received old `application-dev.yml` as compromised for those values; rotate as above; consider history purge later (out of Batch A scope).
6. **ADMIN_SEED_PASSWORD / DEMO_USER_PASSWORD** — set strong local-only values in `.env`; do not reuse committed placeholders.

---

## 5. Production validations added (`ProdStartupValidator`, profile `prod`)

Fails startup when:

- JWT secret blank
- DB URL / username / password blank
- Redis host blank
- CORS allowed origins empty
- `FRONTEND_URL` / `BACKEND_URL` (`application.*.current`) blank
- Any of those URL/host values contain `localhost` or `127.0.0.1`
- `storage.local.base-path` is a Windows drive path (`D:/...` or `\\...`)

CORS wildcard + credentials remains enforced only by `CorsProperties` (not duplicated).

---

## 6. Seeders gated

| Component | Gate | Fresh prod reference data |
|-----------|------|---------------------------|
| `MetroLineSeeder` | `@Profile("dev")` | Admin metro APIs (or future intentional Flyway/ops seed) |
| `CollectionBootstrapper` | `@Profile("dev")` | Admin campaign APIs after lines exist |
| `AdminSeedBootstrap` | already `@Profile("dev")` | N/A |
| `MvpDemoSeedBootstrap` | already `@Profile("dev")` | N/A |
| Flyway V8+ RBAC permission seeds | unchanged | Migration-time (legitimate) |

These seeders are **not** immutable production schema; gating with `@Profile("dev")` is appropriate.

---

## 7. Test commands and results

| Command | Result |
|---------|--------|
| `git diff --check` | No conflict-marker errors reported (CRLF warnings only) |
| Targeted Batch A tests | **PASS** |
| `mvn test` | **BUILD SUCCESS** — Tests run: **457**, Failures: **0**, Errors: **0**, Skipped: **4** |
| `mvn verify` | **BUILD SUCCESS** — same Surefire summary; JAR packaged |

**Skipped (honest):** 4 tests — Docker/Testcontainers unavailable (`ProdSwaggerDisabledTest`, `UploadSecurityTest` ×2, `ExoticStampApplicationTests`). `ProdSeederExclusionIT` is also Docker-gated and skips without Docker. Failsafe/`*IT` CI gates remain Batch B.

### Targeted coverage
- Prod missing/invalid config → `ProdStartupValidatorTest`, `ProdConfigurationFailFastTest`
- Valid minimum prod rules → `ProdStartupValidatorTest.validMinimumProdConfiguration_passes`
- Seeder exclusion → `DevOnlySeederProfileTest` (+ `ProdSeederExclusionIT` when Docker present)
- JWT missing → `JwtSecretRequiredTest`
- CORS wildcard → `CorsPropertiesWildcardTest`

---

## 8. Compatibility impact

- **Dev:** Must supply `JWT_SECRET`, `DB_PASSWORD`, `MAIL_*`, `ADMIN_SEED_PASSWORD`, `DEMO_USER_PASSWORD` via `.env` (see `.env.example`). No silent secret defaults.
- **Prod:** Stricter startup; no localhost DB/Redis/URL; no Windows upload paths.
- **API / schema / NFC / rewards / collection write paths:** unchanged.
- **JWT issuer config name** now `exotic-stamp` (was `metricsX` in YAML). Token **validation still does not enforce issuer** (pending R-P1-06); existing tokens remain accepted until secret rotation.

---

## 9. Rollback instructions

1. Revert Batch A commits/files listed above.
2. Do **not** re-introduce real secret defaults into YAML.
3. `.m2` index removal: do not `git add .m2` again; local Maven cache under `~/.m2` or project `.m2` can remain on disk untracked.
4. If prod validator blocks a legitimate topology, fix env values — do not disable the validator without a security review.

---

## 10. Tracked `.m2` path count removed

**2086** paths removed from the backend git index (`git rm -r --cached .m2`).  
Confirm: `git ls-files '.m2/**'` → empty.

---

## 11. Remaining P0 items (not in Batch A)

| ID | Topic |
|----|-------|
| R-P0-01 (ops) | External secret rotations |
| R-P0-04 | Composite rate limiting + Retry-After |
| R-P0-05 | CI + Failsafe + Docker-required ITs |
| R-P0-06 | S3 / production object storage |
| R-P0-07a–d | Graceful shutdown, JVM memory, `.dockerignore`, Caddy |

---

## 12. Explicit non-goals confirmation

S3, rate limiting, Redis outage matrix, reward reconciliation/outbox, DB migrations, voucher uniqueness, idempotency, NFC lifecycle, Maven Failsafe, GitHub Actions, JaCoCo, Dockerfile/Caddy, and dependency upgrades were **not** modified in Batch A.
