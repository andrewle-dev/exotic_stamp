# Batch B.1 Implementation Report — CI Closure, Testcontainers, Coverage Hardening

**Date:** 2026-07-23  
**Scope:** Continuation of Batch B (R-P0-05, R-P1-10) — not a runtime-feature batch  
**Verdict:** **PARTIAL**

---

## 1. Verdict

| Field | Value |
|-------|-------|
| **Verdict** | **PARTIAL** |
| Why not PASS | (1) JaCoCo gates still fail under `mvn verify -Pci` (overall line **53.21%** / branch **40.28%**; auth/collection/reward also below 70%/60%). Raising gates would require large additional test suites and/or production testability changes — not done in this batch. (2) GitHub Actions green run **not** proven from this environment (push/credentials not completed). |
| Why not FAIL | Docker/Testcontainers root cause fixed in-repo; Failsafe executes **29** ITs with **0** skipped under `-Pci` when Docker is up; expected IT manifest assert **PASS**; PostgreSQL + Redis container ITs execute; combined JaCoCo agent append works; workflow hardened; production runtime code **not** modified for coverage. |

---

## 2. Docker / Testcontainers root cause

| Item | Detail |
|------|--------|
| Symptom | Testcontainers `NpipeSocketClientProviderStrategy` / empty engine info; ITs skipped or Docker “unavailable” despite working `docker` CLI |
| Root cause | Docker Engine **29** (Desktop) requires API **≥1.44**; Boot BOM Testcontainers **1.19.8** / docker-java defaulted to API **1.32** → negotiation failure |
| Repo-safe fix | `pom.xml` property `testcontainers.version=1.21.4`; Surefire/Failsafe `api.version=1.44`; `src/test/resources/docker-java.properties` `api.version=1.44` |
| Not used as permanent fix | Machine-local `DOCKER_HOST`, Ryuk disable, converting ITs to unit tests |
| Transient machine issue | Docker Desktop engine stopped mid-batch (`npipe` missing); restarted Desktop; re-ran `-Pci` successfully |

---

## 3. Machine-only changes (not committed)

| Change | Notes |
|--------|-------|
| Temporary `DOCKER_HOST` experiments | Ruled out; not the root cause |
| Docker Desktop restart | Operator action after engine crash during long verify |

---

## 4. Repository changes (authorized scope)

### Build / CI
- `pom.xml` — Testcontainers 1.21.4 override; `api.version=1.44` on Surefire/Failsafe
- `.github/workflows/backend-ci.yml` — push triggers `main` + `master`; assert `if: always()`; JaCoCo critical package + summary diagnostics; artifacts unchanged
- `scripts/ci/expected_integration_tests.txt` — **new** manifest
- `scripts/ci/assert_test_reports.py` — manifest-driven executed/skipped/missing checks; `--strict` / `ci.require-docker`
- `scripts/ci/jacoco_critical_packages.py` — **new**; fails if a critical group matches **zero** classes

### Test-only
- `src/test/resources/docker-java.properties` — API 1.44
- `metro.ExoticStamp.support.IntegrationTestSupport` — shared DynamicProperty helpers (incl. loopback→`127.0.0.2` for prod validator)
- `ExcludeRateLimitFilterInitializer` — **test-only** workaround for SecurityConfig filter-order defect (see blockers)
- SpringBoot ITs: prod/dev secrets, JTE `gg.jte.development-mode` for prod profile, seed/schema fixes
- Flyway V12/V13/V15 ITs: assert migration **applied**, not “latest version”
- Reward ITs: schema-aligned seeds (V13+ status columns), unique codes, JDBC flush/commit visibility
- `RedisLuaRateLimiterIT`: start Lettuce connection factory
- `CollectionPersistenceIT`: earlier isolation/`stringtype`/`EntityScan` fixes retained

### Docs
- This report; updates to `BACKEND_REMEDIATION_PLAN.md` and `BATCH_B_IMPLEMENTATION_REPORT.md`

### Explicitly unchanged
- Application source, Flyway migrations, runtime SecurityConfig/JWT, collection/reward business logic, NFC, S3, rate-limit policies, Redis outage policy, Dockerfile, Caddy, OpenAPI

---

## 5. Default branch evidence

| Check | Result |
|-------|--------|
| `git ls-remote --symref exotic HEAD` | `ref: refs/heads/main` → `f63d7d0…` |
| `origin` (`EXOTIC_PC`) | No usable HEAD (`Repository not found` in earlier Phase 0) |
| Local branch | `master` |
| Workflow push triggers | **`main` and `master`** (remote evidence = `main`; local history also uses `master`) |

---

## 6. Exact files changed (B.1)

See git status for full list. Primary paths:

- `pom.xml`
- `.github/workflows/backend-ci.yml`
- `scripts/ci/*`
- `src/test/**`
- `docs/deployment/BATCH_B1_IMPLEMENTATION_REPORT.md`
- `docs/deployment/BATCH_B_IMPLEMENTATION_REPORT.md`
- `docs/deployment/BACKEND_REMEDIATION_PLAN.md`

---

## 7–9. Surefire / Failsafe counts (`mvn -B -ntp clean verify -Pci`)

| Suite | Executed | Failures | Errors | Skipped |
|-------|----------|----------|--------|---------|
| Surefire | **502** | 0 | 0 | 0 |
| Failsafe | **29** | 0 | 0 | **0** |

(Without `-Pci`, `CiDatasourceUrlGuardIT` may skip when datasource URL unset; under `-Pci` it executes.)

---

## 10. Expected IT manifest results

`python scripts/ci/assert_test_reports.py --strict` → **ASSERTION OK**  
executed=12, skipped=0, missing=0 (all manifest classes)

---

## 11. PostgreSQL and Redis container evidence

| IT | Evidence |
|----|----------|
| `FlywayV16MigrationIT` / `CollectionPersistenceIT` | PostgreSQL Testcontainers executed PASS |
| `UserStampCacheIntegrationIT` | Redis Testcontainers executed PASS |
| `RedisLuaRateLimiterIT` | Redis Testcontainers executed PASS |
| `RewardStampCollectedFlowIT` / `RewardConcurrencyIT` | PostgreSQL Testcontainers executed PASS |

---

## 12. JaCoCo unit-only vs combined

| Mode | Overall LINE | Overall BRANCH |
|------|--------------|----------------|
| Historical unit-heavy (Batch B era) | ~47–48% | ~34% |
| Combined after Failsafe (`verify -Pci`) | **53.21%** (4735/8899) | **40.28%** (1197/2972) |

IT-only path example: `UserStampCampaignCountAdapter.countDistinctStations…` and reward DataJpaTest seeds are exercised on Failsafe only (not Surefire). Combined `jacoco.exec` uses `append=true` on Failsafe agent; report phase after Failsafe verify.

---

## 13. Critical package matched class counts

| Group | Classes | LINE | BRANCH |
|-------|---------|------|--------|
| auth | 109 | 60.03% (763/1271) | 53.62% (185/345) |
| collection | 202 | 64.54% (1192/1847) | 46.47% (257/553) |
| reward | 144 | 35.52% (454/1278) | 25.22% (116/460) |

Zero-class match: **none** (`jacoco_critical_packages.py` exit 0).

---

## 14–17. Coverage vs gates

| Gate | Required | Actual | Result |
|------|----------|--------|--------|
| Overall LINE | ≥60% | 53.21% | **FAIL** |
| Overall BRANCH | ≥50% | 40.28% | **FAIL** |
| Auth LINE/BRANCH | ≥70%/60% | 60.03%/53.62% | **FAIL** |
| Collection LINE/BRANCH | ≥70%/60% | 64.54%/46.47% | **FAIL** |
| Reward LINE/BRANCH | ≥70%/60% | 35.52%/25.22% | **FAIL** |

Thresholds **not** lowered. No broad exclusions added.

---

## 18. New tests / behaviors covered (test-only)

- Docker API compatibility enabling previously skipped ITs
- Prod context ITs with non-localhost Testcontainers hosts + JTE dev mode for prod profile tests
- Reward/collection seed SQL aligned to current Flyway schema
- Failsafe manifest enforcement

---

## 19. Coverage exclusions

Unchanged: trivial launcher `ExoticStampApplication` only (documented in JaCoCo check).

---

## 20. Local command results

| Command | Result |
|---------|--------|
| `git diff --check` | Warnings only (CRLF); no whitespace errors reported as fatal |
| `mvn -B -ntp clean test` | **PASS** (502) |
| `mvn -B -ntp clean verify` | **PASS** (Failsafe 29, 1 skip for datasource guard outside CI) |
| `mvn -B -ntp clean verify -Pci` | Tests **PASS**; JaCoCo **check FAIL** (coverage) |
| `python scripts/ci/assert_test_reports.py --strict` | **PASS** |
| `python scripts/ci/jacoco_summary.py` / `jacoco_critical_packages.py` | Ran; critical groups non-zero |

---

## 21. GitHub Actions run evidence

| Item | Value |
|------|-------|
| Branch pushed | `chore/batch-b1-ci-closure` → `exotic` (`EXOTIC_STAMP`) |
| Commit SHA | `efcff8f54b02be21b4b38cc65db052fd8bc05b7b` |
| PR link (suggested) | https://github.com/itdept-studio/EXOTIC_STAMP/pull/new/chore/batch-b1-ci-closure |
| Workflow observation | `gh` CLI **not installed** on this machine — run status **not** verified here |
| Expected CI outcome | Tests should pass; **JaCoCo check will fail** (same as local `-Pci`) → not a green gate until coverage rises |

```bash
# Observe run (install GitHub CLI first):
gh run list --workflow=backend-ci.yml --repo itdept-studio/EXOTIC_STAMP --branch chore/batch-b1-ci-closure
gh run watch --repo itdept-studio/EXOTIC_STAMP
```

---

## 22. Remaining blockers

1. **Coverage gates** — reward/auth/collection/overall below thresholds without production changes or a large dedicated test campaign.
2. **GHA green run** — not executed/verified here.
3. **Production defect (resolved in Batch C):** `SecurityConfig` previously registered `RateLimitFilter` with `addFilterBefore(..., JwtAuthFilter.class)` *before* `JwtAuthFilter` was on the chain → `JwtAuthFilter does not have a registered order`. Batch B.1 used `ExcludeRateLimitFilterInitializer` (test-only). **Fixed in Batch C:** JWT registered first; rate limit via `addFilterAfter(..., JwtAuthFilter.class)`; workaround removed. See `BATCH_C_IMPLEMENTATION_REPORT.md`.
4. Reward `RewardIssuancePolicyService` class-level `@Transactional(readOnly=true)` can mark outer write TX read-only when joined (test visibility/event quirks); review in a later batch.

---

## 23. Rollback

```bash
git revert <batch-b1-commit>
# or reset branch to pre-B.1 SHA
```

No production schema or runtime behavior to roll back (test/CI/docs only).

---

## R-P0-05 / R-P1-10

| Item | Status after B.1 |
|------|------------------|
| R-P0-05 | **PARTIAL** — local Failsafe+Docker+assert proven; GHA green **not** proven |
| R-P1-10 | **PARTIAL** — gates still fail (especially reward) |
