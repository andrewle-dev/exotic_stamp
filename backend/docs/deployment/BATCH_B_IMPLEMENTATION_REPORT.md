# Batch B Implementation Report — Production Readiness Remediation

**Date:** 2026-07-23  
**Scope:** R-P0-05, R-P1-10, F-004, F-019 (CI governance, Failsafe ITs, Docker enforcement, JaCoCo gates)  
**Verdict:** **PARTIAL**

---

## 1. Implementation verdict

| Field | Value |
|-------|-------|
| **Verdict** | **PARTIAL** |
| Why not PASS | (1) Batch B: local Testcontainers blocked on Docker API negotiation (fixed in **Batch B.1**). (2) Combined coverage still below gates after B.1. (3) No green GitHub Actions run proven. |
| Why not FAIL | Surefire/Failsafe split works; `mvn test` green; Failsafe discovers ITs; `-Pci` fail-closed; report assertion + GHA workflow in place; thresholds **not** lowered. **Batch B.1** later proved Docker ITs execute locally — see `BATCH_B1_IMPLEMENTATION_REPORT.md`. |

> **Batch B.1 update:** Docker root cause fixed (TC 1.21.4 + API 1.44). Local `verify -Pci` runs Failsafe 29 executed / 0 skipped; assert PASS; coverage gates still fail. R-P0-05 / R-P1-10 remain PARTIAL pending GHA green + coverage.

---

## 2. Git repository and default branch

| Check | Result |
|-------|--------|
| `git rev-parse --show-toplevel` | `D:/Part-time/ExoticStamp/backend` |
| `.git` | Directory (nested backend repo) |
| Remotes | `origin` → `https://github.com/itdept-studio/EXOTIC_PC.git`; `exotic` → `https://github.com/itdept-studio/EXOTIC_STAMP.git` |
| Current branch | `master` |
| Default branch (push trigger) | **`master`** (matches Batch A remotes; workflow push trigger uses `master`) |
| Parent monorepo | Not modified; workflow **not** duplicated to parent |

Workflow path: **`.github/workflows/backend-ci.yml`** (nested backend repo only).

---

## 3. Exact files changed (Batch B deliverables)

### Build / CI
- `pom.xml` — Surefire excludes `*IT`; Failsafe includes `*IT`; JaCoCo prepare/report + `ci` profile coverage gates; Maven Enforcer (Java 21, Maven ≥3.8); property `ci.require-docker`
- `.github/workflows/backend-ci.yml` — **new**
- `scripts/ci/assert_test_reports.py` — **new**
- `scripts/ci/jacoco_summary.py` — **new** (local reporting helper)
- `scripts/ci/test_inventory.py` — **new** (inventory helper)

### Tests (Batch B)
- `src/test/java/metro/ExoticStamp/ExoticStampApplicationIT.java` — renamed from `ExoticStampApplicationTests`
- `src/test/java/metro/ExoticStamp/config/ProdSwaggerDisabledIT.java` — renamed from `*Test`
- `src/test/java/metro/ExoticStamp/config/UploadSecurityIT.java` — renamed from `*Test`
- `src/test/java/metro/ExoticStamp/config/CiDockerAvailabilityIT.java` — **new**
- `src/test/java/metro/ExoticStamp/config/CiDatasourceUrlGuardIT.java` — **new**
- `CollectionPersistenceIT`, `UserStampCacheIntegrationIT` — `disabledWithoutDocker = true` (classification/hardening)

### Docs
- `docs/deployment/BATCH_B_IMPLEMENTATION_REPORT.md` — this file
- `docs/deployment/BACKEND_REMEDIATION_PLAN.md` — Batch B status markers

### Unchanged (confirmed)
- Application business logic, REST/OpenAPI contracts, Flyway migrations, NFC, collection/reward/voucher/idempotency runtime, auth runtime, rate limiting, Redis outage policy, S3, Dockerfile, Caddy, production ENV behavior, Batch A production validators

---

## 4. Test inventory and classification

Regenerate: `python scripts/ci/test_inventory.py`  
**Totals:** 117 test classes inventoried; **15** `*IT`; **13** Docker/Testcontainers-backed.

| Category | Lifecycle | Notes |
|----------|-----------|-------|
| Unit tests | Surefire (`mvn test`) | Majority of `*Test` |
| Spring slice (`@WebMvcTest` etc.) | Surefire | Controllers/security slices |
| Architecture | Surefire | `ArchitectureBoundaryTest` |
| Non-Docker Spring | Surefire | Fail-fast / property tests without TC |
| Testcontainers / integration | Failsafe (`mvn verify`) | App context, cache, reward flow |
| Migration ITs | Failsafe | `FlywayV12`–`V16` `*IT` |
| Concurrency ITs | Failsafe | `RewardConcurrencyIT` |
| Security ITs | Failsafe | `UploadSecurityIT`, prod swagger IT |
| CI guards | Failsafe | `CiDockerAvailabilityIT`, `CiDatasourceUrlGuardIT` |

Not every `@SpringBootTest` is an IT: only Docker/Testcontainers / Failsafe-classified classes use `*IT`.

### Representative inventory rows

| class | suffix | category | Docker | current → target | renamed |
|-------|--------|----------|--------|------------------|---------|
| `ArchitectureBoundaryTest` | Test | Architecture | no | surefire | unchanged |
| `AuthCommandServiceTest` | Test | Unit | no | surefire | unchanged |
| `CollectionControllerTest` | Test | Spring slice | no | surefire | unchanged |
| `JwtSecretRequiredTest` | Test | Spring Boot non-Docker | no | surefire | unchanged |
| `ProdStartupValidatorTest` | Test | Unit | no | surefire | unchanged |
| `CiDockerAvailabilityIT` | IT | CI Docker gate | yes* | failsafe | **new** |
| `CiDatasourceUrlGuardIT` | IT | CI URL guard | no† | failsafe | **new** |
| `ExoticStampApplicationIT` | IT | Testcontainers | yes | failsafe | **renamed** |
| `ProdSwaggerDisabledIT` | IT | Security IT | yes | failsafe | **renamed** |
| `UploadSecurityIT` | IT | Security IT | yes | failsafe | **renamed** |
| `ProdSeederExclusionIT` | IT | Testcontainers | yes | failsafe | unchanged (Batch A) |
| `FlywayV12..V16MigrationIT` | IT | Migration | yes | failsafe | unchanged |
| `CollectionPersistenceIT` | IT | Testcontainers | yes | failsafe | suffix already IT |
| `UserStampCacheIntegrationIT` | IT | Testcontainers | yes | failsafe | suffix already IT |
| `RewardStampCollectedFlowIT` | IT | Testcontainers | yes | failsafe | unchanged |
| `RewardConcurrencyIT` | IT | Concurrency | yes | failsafe | unchanged |

\*Uses Testcontainers Docker client factory (not a container).  
†Enabled only when `ci.require-docker=true`; no container.

---

## 5. Tests renamed `*Test` → `*IT`

| From | To |
|------|-----|
| `ExoticStampApplicationTests` | `ExoticStampApplicationIT` |
| `ProdSwaggerDisabledTest` | `ProdSwaggerDisabledIT` |
| `UploadSecurityTest` | `UploadSecurityIT` |

Unit/WebMvc slice tests were **not** renamed.

---

## 6. Surefire configuration

- Excludes: `**/*IT.java`, `**/*IT.class`, `**/*IntegrationTest.java`, `**/*IntegrationTest.class`
- `argLine=${surefireArgLine}` (JaCoCo agent; empty property defaults removed so agent can set the property)
- `testFailureIgnore` **not** set
- Result locally: **453** tests, 0 fail, 0 skip, **BUILD SUCCESS**

---

## 7. Failsafe configuration

- Includes: `**/*IT.java`, `**/*IntegrationTest.java`
- Goals: `integration-test`, `verify`
- `systemPropertyVariables.ci.require-docker=${ci.require-docker}`
- `forkCount=1`, serial execution
- Reports: `target/failsafe-reports`
- Result locally (no usable TC Docker): **28** tests discovered, **28 skipped** (or 1 failure + 26 skipped under `-Pci`)

---

## 8. Integration-test counts

| Command | Surefire | Failsafe | Notes |
|---------|----------|----------|-------|
| `mvn clean test` | 453 / 0 skip | n/a | ITs excluded |
| `mvn clean verify` | 453 | 28 / **28 skipped** | TC Docker unavailable |
| `mvn clean verify -Pci` | 453 | 28 / **1 failure** (`CiDockerAvailabilityIT`) + 26 skipped | Fail-closed as designed |

---

## 9. Docker enforcement design

1. Maven profile `ci` sets `ci.require-docker=true`.
2. Failsafe passes that system property into ITs.
3. `CiDockerAvailabilityIT`:
   - if `ci.require-docker=true` → **assert** Testcontainers Docker available (fail, never skip)
   - else → `Assumptions.assumeTrue` (honest local skip)
4. Existing `@Testcontainers(disabledWithoutDocker = true)` retained for local unit-friendly `mvn test` / non-CI verify.
5. GHA runs `docker info` then `mvn -B -ntp clean verify -Pci`.
6. `scripts/ci/assert_test_reports.py` fails on Docker-related skips and zero Failsafe tests.

---

## 10. Test-report assertion behavior

Script: `scripts/ci/assert_test_reports.py`

Fails when:
- No Failsafe reports / Failsafe tests == 0
- Any Surefire/Failsafe failures or errors
- Skipped cases whose messages match Docker/Testcontainers absence markers
- Critical ITs missing: `CiDockerAvailabilityIT`, `ProdSeederExclusionIT`

Local result after `mvn verify` (no TC Docker): **exit 1** (Docker skips detected) — correct for CI; developers without Docker should not treat this script as a local soft check.

---

## 11. JaCoCo

| Item | Value |
|------|-------|
| Agent data | Single `target/jacoco.exec` with `append=true` for unit + IT (avoids overwrite) |
| Report | `target/site/jacoco/index.html`, `jacoco.xml` |
| Gate binding | **`ci` profile only** (`jacoco-check-ci`) — local `mvn verify` still generates report |
| Overall (unit-only this machine) | **LINE 47.68%** (3929/8240); **BRANCH 34.40%** (870/2529) |
| auth aggregate | LINE **57.48%**; BRANCH **52.68%** |
| collection aggregate | LINE **61.29%**; BRANCH **44.67%** |
| reward aggregate | LINE **29.97%**; BRANCH **20.00%** |
| Gate result | **FAIL** vs required overall ≥60%/≥50% and critical ≥70%/≥60% |
| Exclusions | **Only** `metro/ExoticStamp/ExoticStampApplication.class` (trivial launcher) |
| Thresholds | **Not lowered** |

Critical-package rules use multi-level `*` includes (JaCoCo does not support `**`).

Closing the gap without production changes requires Docker-enabled ITs (cache/repos/reward flow) plus additional focused unit tests — deferred; leave CI failing until met.

---

## 12. GitHub Actions workflow summary

Path: `.github/workflows/backend-ci.yml`

- Triggers: `pull_request`, `push` to `master`, `workflow_dispatch`
- `permissions: contents: read`
- Concurrency cancel-in-progress per ref
- Ubuntu, JDK 21 Temurin, Maven cache, 45m timeout
- `docker info` then `mvn -B -ntp clean verify -Pci` then assert script
- Artifacts (always): Surefire, Failsafe, JaCoCo; JAR on success only
- No secrets, no `.env`, no `.m2` upload, no deploy/AWS/SSH
- Actions: `actions/checkout@v4`, `setup-java@v4`, `upload-artifact@v4` (stable major tags; SHA pin follow-up)

---

## 13. Local command results

| Command | Result |
|---------|--------|
| `git diff --check` (scoped Batch B paths) | Clean (exit 0) |
| `mvn -B -ntp clean test` | **SUCCESS** — 453 tests |
| `mvn -B -ntp clean verify` | **SUCCESS** — Failsafe 28 skipped; JaCoCo report generated |
| `mvn -B -ntp clean verify -Pci` | **FAILURE** — `CiDockerAvailabilityIT` (expected) |
| `python scripts/ci/assert_test_reports.py` | **FAILURE** — Docker skips (expected without TC Docker) |

---

## 14. Docker-dependent checks not executable locally

- Non-zero **executed** (non-skipped) Failsafe ITs
- Combined unit+IT JaCoCo meeting gates
- Green GitHub Actions `backend-ci` run
- End-to-end proof that assert script passes after real IT execution

**Cause:** Testcontainers `DockerClientFactory.isDockerAvailable()` returns false (npipe/BadRequest) while Docker CLI works. GHA Ubuntu is the authoritative IT runner.

---

## 15. Data-integrity / isolation checks

| Check | Status |
|-------|--------|
| ITs use PostgreSQL Testcontainers | Yes (isolated images `postgres:16-alpine`) |
| Redis Testcontainers where needed | Yes (`UserStampCacheIntegrationIT`, app ITs) |
| No production/dev JDBC in IT DynamicPropertySource | Yes — container-derived URLs |
| Parent `.env` / prod secrets not used by workflow | Yes |
| Flyway on clean TC DB | Yes in migration/app ITs |
| CI URL guard | `CiDatasourceUrlGuardIT` under `-Pci` rejects cloud/prod-like hosts; allows TC/localhost |
| Container cleanup | Testcontainers Ryuk / JUnit lifecycle (standard) |

---

## 16. Compatibility / developer workflow impact

| Workflow | Impact |
|----------|--------|
| `mvn test` | Unchanged for day-to-day unit/slice work; ITs excluded |
| `mvn verify` | Runs Failsafe; may skip Docker ITs locally; report generated; coverage gate **not** enforced |
| `mvn verify -Pci` | Requires working Testcontainers Docker + coverage gates (CI) |
| IDE | Prefer running `*Test` for fast feedback; `*IT` need Docker |

---

## 17. Rollback instructions

1. Revert `pom.xml` Batch B plugin/profile blocks.
2. Delete `.github/workflows/backend-ci.yml` and `scripts/ci/*` Batch B scripts.
3. Rename `*IT` classes back if needed (`ExoticStampApplicationIT` → `ExoticStampApplicationTests`, etc.).
4. Remove `CiDockerAvailabilityIT` / `CiDatasourceUrlGuardIT`.
5. Restore remediation plan markers for R-P0-05 / R-P1-10.

No production runtime code to roll back.

---

## 18. Remaining P0/P1 items

### This batch (incomplete)
| ID | Status | Remaining |
|----|--------|-----------|
| **R-P0-05** | **PARTIAL** | Prove green GHA with non-zero executed ITs + assert script PASS |
| **R-P1-10** | **PARTIAL** | Raise coverage to gates (esp. reward); Redis-down / voucher gaps still open per plan |
| **F-004 / F-019** | **PARTIAL** | Governance tooling landed; release-ready only after green CI + coverage |

### Still open (other batches / prior)
- R-P0-01 ops secret rotation
- R-P0-04 rate limits
- R-P0-06 S3
- R-P0-07a–d shutdown/JVM/dockerignore/Caddy
- R-P1-01 reward reconcile/outbox
- R-P1-06 JWT harden
- R-P1-07 Redis matrix
- Other P1 items in remediation plan

---

## Confirmation

Runtime business logic, Flyway migrations, NFC behavior, S3 implementation, rate limiting, Redis outage policy, reward processing semantics, and API contracts were **not** modified in Batch B.
