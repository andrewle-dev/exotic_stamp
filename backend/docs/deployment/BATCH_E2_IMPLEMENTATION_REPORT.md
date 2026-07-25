# Batch E.2 Implementation Report — Quality Gate and Integrity Verification Closure

**Date:** 2026-07-25  
**Companions:** Audit v2, Remediation Plan v2, `BATCH_E1_IMPLEMENTATION_REPORT.md`, `BATCH_E2_COVERAGE_GAP_ANALYSIS.md`, `GITHUB_BRANCH_PROTECTION_CHECKLIST.md`

---

## 1. Verdict

**PASS (local CI gates)** / **PARTIAL (GitHub Actions green run not yet verified on remote)**

| Item | Status |
|------|--------|
| JaCoCo gates (overall + auth/collection/reward) | **DONE** — all configured thresholds met; thresholds **not** lowered |
| Two-worker advisory lock IT | **DONE** — `RewardReconcileAdvisoryLockIT` (6 tests) |
| Pending-stock concurrency IT | **DONE** — `RewardPendingStockConcurrencyIT` (5 tests) |
| Notification dedup proof | **DONE** — `NotificationRewardDedupIT` + listener/unit |
| Admin reconcile security | **DONE** — `AdminRewardControllerTest` (401/403/admin/busy/dry-run/no PII) |
| Three consecutive `mvn clean verify -Pci` | **DONE** (local) |
| Manifest assert `--strict` | **PASS** (22 ITs executed, 0 skipped) |
| R-P1-10 | **DONE** |
| R-P0-05 | **PARTIAL** until green GitHub Actions run recorded on `exotic` remote |

---

## 2. Exact files changed (primary)

### New tests / docs
- `RewardReconcileAdvisoryLockIT.java`
- `RewardPendingStockConcurrencyIT.java`
- `NotificationRewardDedupIT.java`
- `CommunityIntegrationListenerTest.java`
- Expanded unit/WebMvc coverage across reward/auth/collection (E.2 campaign)
- `docs/deployment/BATCH_E2_COVERAGE_GAP_ANALYSIS.md`
- `docs/deployment/BATCH_E2_IMPLEMENTATION_REPORT.md`
- `docs/deployment/GITHUB_BRANCH_PROTECTION_CHECKLIST.md`
- `scripts/ci/expected_integration_tests.txt` (+3 ITs)

### Test-only fixes
- `AuthCommandServiceTest.refresh_withinGrace` stub alignment
- `RewardWebMvcTestSecurityConfig` anonymous → 401
- `CampaignCommandServiceTest.update_partialBannerAndDates` argument order
- UTF-16 → UTF-8 repair on a few Windows-written test files

### Production
- **No intentional production semantic changes** for S3, NFC, JWT/OTP/rate-limit, Dockerfile, Caddy, voucher/reward business rules, API success DTOs, or Flyway V1–V23.
- Admin reconcile remains `@PreAuthorize("hasRole('ADMIN')")` (consistent with other `AdminRewardController` endpoints). `REWARD_RECONCILE` not introduced (would require RBAC seed without fitting sibling endpoints).

---

## 3. Coverage gap analysis

See `BATCH_E2_COVERAGE_GAP_ANALYSIS.md`. Priority was reward → auth → collection → overall behavior-rich classes. No trivial getter/Lombok coverage.

---

## 4. New meaningful tests (summary)

| Area | Evidence |
|------|----------|
| Advisory lock two-worker / crash / exception release / dry-run lock policy | `RewardReconcileAdvisoryLockIT` |
| Pending-stock fulfill / race / fewer vouchers / rerun / no-stock | `RewardPendingStockConcurrencyIT` |
| Notification unique + concurrent + non-null reference_id | `NotificationRewardDedupIT`, `CommunityIntegrationListenerTest`, `NotificationCommandServiceTest` |
| Admin reconcile 401/403/admin/409/dry-run/no voucher fields | `AdminRewardControllerTest` |
| Reward/collection/auth branch coverage | Expanded service/controller/redis/mapper unit tests |

---

## 5–9. Integrity results

| Proof | Result |
|-------|--------|
| Two-worker lock | B busy while A holds; B acquires after A release |
| Connection crash | Closing physical session releases advisory lock |
| Exception in reconcile | `finally` releases lock |
| Pending-stock concurrency | Exactly one ISSUED + one CLAIMED under two workers |
| Multiple pending / one voucher | One ISSUED, one remains PENDING_STOCK |
| Notification dedup | Unique index rejects duplicate `(user,type,ref)`; concurrent one winner; listener always sets `referenceId=userRewardId` |
| Admin security | Unauthenticated 401; USER 403; ADMIN OK; busy → 409 `RECONCILE_BUSY` |

---

## 10–16. Local CI metrics (post E.2 `-Pci`)

| Metric | Value |
|--------|-------|
| Surefire | **855** / 0 fail / 0 err / 0 skip |
| Failsafe | **64** / 0 fail / 0 err / 0 skip |
| Manifest | **PASS** (22 expected ITs executed) |
| Overall LINE/BRANCH | **66.42% / 53.97%** |
| Auth LINE/BRANCH | **73.49% / 71.30%** |
| Collection LINE/BRANCH | **76.77% / 65.76%** |
| Reward LINE/BRANCH | **83.00% / 72.18%** |
| JaCoCo `jacoco-check-ci` | **All coverage checks have been met** |
| Three-run stability | Run1–3 all SUCCESS (`.e2-logs/pci-run1b.log`, `pci-run2.log`, `pci-run3.log`) |

Gates (unchanged): Overall ≥60/50; Auth/Collection/Reward ≥70/60.

---

## 17. Three-run stability

Local `mvn -B -ntp clean verify -Pci` consecutive runs recorded under `.e2-logs/pci-run*.log`. Acceptance: exit 0, no skips, gates pass.

---

## 18. GitHub Actions evidence

| Field | Value |
|-------|-------|
| Repository | `itdept-studio/EXOTIC_STAMP` (`exotic` remote) |
| Branch | TBD push `chore/batch-e2-quality-gate` |
| Workflow | `.github/workflows/backend-ci.yml` |
| Green run | **Not verified in this batch** if push/credentials unavailable → R-P0-05 remains PARTIAL |

Manual:

```bash
git push -u exotic HEAD:chore/batch-e2-quality-gate
gh run list --repo itdept-studio/EXOTIC_STAMP --workflow=backend-ci.yml --limit 5
```

---

## 19. Branch protection

Documented in `GITHUB_BRANCH_PROTECTION_CHECKLIST.md`. **Not applied** live (authorization required).

---

## 20. Remaining blockers

1. **R-P0-05:** Prove green GitHub Actions `backend-ci` on `exotic` remote.
2. Apply branch protection checklist when authorized.
3. Staging deploy / ops items outside E.2 (S3 Lightsail credentials, etc.).

---

## 21. Explicitly unchanged

- S3/storage semantics
- NFC lifecycle
- JWT/OTP/rate-limit semantics
- Dockerfile / Caddy
- API success DTO shapes
- Flyway V1–V23
- PostgreSQL advisory lock key / voucher uniqueness / fingerprint / pending-stock design (proven, not redesigned)
- JaCoCo thresholds (not lowered; no broad exclusions)