# Batch E.1 Implementation Report — Reward Reconciliation & Idempotency Integrity Closure

**Date:** 2026-07-24  
**Companions:** Audit v2, Remediation Plan v2, `BATCH_E_IMPLEMENTATION_REPORT.md`  

---

## 1. Verdict

**PARTIAL**

| Item | Status |
|------|--------|
| R-P1-01 missing-reward + pending-stock reconcile | **DONE** (DB advisory lock + distinct pending-stock fulfill) |
| R-P1-03 fingerprint conflict semantics | **DONE** (`IDEMPOTENCY_CONFLICT` for different logical payload) |
| R-P1-09 V22 default predicate | **VERIFIED** (equivalent; soft-delete preserves `isDefault`) |
| Data preflight | **DONE** (`BATCH_E_DATA_PREFLIGHT.sql`, read-only) |
| Multi-instance reconcile safety | **DONE** (PostgreSQL advisory lock + SKIP LOCKED pending claim) |
| Admin reconcile hardening | **DONE** (ADMIN role, batch clamp, dry-run, RECONCILE_BUSY) |
| Side-effect idempotency | **DONE** (issued-only events; notification unique dedup) |
| R-P1-10 coverage gates | **PARTIAL in E.1**; **DONE in Batch E.2** (see BATCH_E2_IMPLEMENTATION_REPORT.md) |

---

## 2. Batch E claim-verification table

| Batch E claim | Source evidence | Test evidence | Result |
|---------------|-----------------|---------------|--------|
| V22 partial unique on `user_rewards.voucher_pool_id` | `V22` lines 17–19 | `FlywayV22MigrationIT` | **VERIFIED** |
| V22 default unique soft-delete-aware | `WHERE is_default = TRUE AND line_id IS NOT NULL AND deleted_at IS NULL` | `FlywayV23MigrationIT` campaign cases (index from V22) | **VERIFIED** (equiv. to required predicate; `line_id IS NOT NULL` is safe/equivalent for per-line uniqueness) |
| Idempotency unique race → replay | `CollectionCommandService.resolveIdempotencyUniqueRace` | unit + `CollectionIdempotencyIT` | **VERIFIED** (E); **strengthened** in E.1 with fingerprint |
| Same key different payload conflicts | E had **no** fingerprint | E.1 `IdempotencyConflictException` + IT | Batch E claim was **PARTIAL/FALSE**; **closed in E.1** |
| PENDING_STOCK reconciled | Batch E only missing-reward path | E.1 `fulfillPendingStock` + metrics | Batch E **PARTIAL**; **closed in E.1** |
| Multi-instance safe | JVM `AtomicBoolean` only | E.1 `pg_try_advisory_lock` | Batch E **PARTIAL**; **closed in E.1** |
| Notification idempotent | Always emitted incl. PENDING_STOCK | E.1 issued-only + `uq_notifications_user_type_ref` | Batch E **PARTIAL**; **closed in E.1** |
| Admin reconcile protected/bounded | `@PreAuthorize ADMIN` unbounded | E.1 batch max + dry-run + busy 409 | Batch E **PARTIAL**; **closed in E.1** |

---

## 3. Exact files changed (primary)

- `V23__idempotency_fingerprint_and_reconcile.sql`
- `docs/deployment/BATCH_E_DATA_PREFLIGHT.sql`
- Collection: fingerprint, policy, command, `UserStamp.idempotencyFingerprint`, soft-delete preserve default
- Reward: reconcile service/lock/candidates, pending-stock fulfill, admin reconcile API, properties
- Community: notification unique-violation swallow
- Tests: `CollectionIdempotencyIT`, `FlywayV23MigrationIT`, unit updates
- Docs/CI: this report, remediation plan, expected IT manifest

---

## 4. V22 predicate evidence

Required:

```sql
ON campaigns(line_id)
WHERE is_default = true AND deleted_at IS NULL;
```

V22 actual:

```sql
ON campaigns(line_id)
WHERE is_default = TRUE
  AND line_id IS NOT NULL
  AND deleted_at IS NULL;
```

**Decision:** V22 is equivalent for the per-line invariant. Extra `line_id IS NOT NULL` excludes null line_ids from the index (NULL line_ids are not “per line”). **No V23 index rewrite.** Soft-delete **preserves** historical `isDefault` (cleared in Batch E; reverted in E.1).

---

## 5. V23 migration

Adds:

- `user_stamps.idempotency_fingerprint VARCHAR(64)` nullable (legacy)
- `uq_notifications_user_type_ref` partial unique
- `idx_user_rewards_pending_stock`

---

## 6. Data-preflight

File: `docs/deployment/BATCH_E_DATA_PREFLIGHT.sql` (10 read-only audits). Operators must resolve duplicates before applying V22/V23 in shared envs.

---

## 7. Idempotency fingerprint design

Canonical: `COLLECT|{userId}|{stationId}|{campaignId}|{scanType}` → SHA-256 hex.  
Excludes timestamps, correlation IDs, GPS, device metadata, raw NFC/QR payload.

---

## 8–10. Replay / conflict / legacy

| Case | Behavior |
|------|----------|
| Same key + same fingerprint | Replay success |
| Same key + different fingerprint | `409 IDEMPOTENCY_CONFLICT` |
| Legacy null fingerprint | Replay only if station+campaign match; else conflict |
| Concurrent same key/same op | One row; loser races → replay via unique mapping |
| Different users same key | Allowed (per-user unique) |

---

## 11–12. Reconciliation

- **Missing reward:** candidate SQL → `handleStampCollected` (no duplicate rows via `uq_user_rewards_once`)
- **Pending stock:** `claimPendingStockRewardIds` FOR UPDATE SKIP LOCKED → `fulfillPendingStock` (updates existing row only)
- Metrics: `missing_repaired`, `pending_fulfilled`, `still_no_stock`, `failed`, `busy`

---

## 13. Multi-instance coordination

PostgreSQL session advisory lock (`RewardReconcileAdvisoryLockAdapter`) held on a borrowed connection for the run; unlock on completion. Crashed JVM releases with TCP drop. Unique constraints remain final guards.

---

## 14. Admin endpoint security

`POST /api/v1/admin/rewards/reconcile?batchSize=&dryRun=`

- `@PreAuthorize("hasRole('ADMIN')")`
- batch clamped by `reconcile-max-batch-size`
- concurrent → `409 RECONCILE_BUSY`
- audit log counts + adminId (no voucher codes)

---

## 15. Notification/email idempotency

- `RewardIssuedEvent` only after status `ISSUED` (including pending-stock fulfillment)
- Notification insert unique on `(user_id, type, reference_id)` — duplicates suppressed
- Email subsystem unchanged; mail failure does not roll back reward TX

---

## 16. Voucher concurrency evidence

Retained from Batch E: `RewardConcurrencyIT`, `FlywayV22MigrationIT` dual-link rejection. Allocation still `FOR UPDATE SKIP LOCKED`.

---

## 17. Surefire / Failsafe

| Suite | Count |
|-------|-------|
| Surefire | **536** / 0 fail / 0 skip |
| Failsafe | **50** / 0 fail / 0 skip |
| Manifest | **PASS** 19/19 |

---

## 18. Coverage before/after

| Metric | Batch E | Batch E.1 |
|--------|---------|-----------|
| Overall line/branch | 54.05% / 40.75% | **53.91% / 40.74%** |
| collection | 64.67% / 47.42% | **64.73% / 47.53%** |
| reward | 37.57% / 26.47% | **38.29% / 28.00%** |
| JaCoCo CI gates | FAIL | **FAIL** (not lowered) |

Slight overall dip from new uncovered lines (admin/lock paths); reward improved.

---

## 19. Remaining blockers

1. JaCoCo gates (R-P1-10) still fail.
2. Admin WebMvc security tests for reconcile not expanded (role check relies on existing ADMIN pattern + exception handler).
3. Shared-env V22/V23 apply still requires operator preflight.
4. Two-worker advisory-lock IT not added as dedicated class (lock unit-tested; pending SKIP LOCKED covered in adapter SQL + reconcile IT path).

---

## 20. Explicitly unchanged

S3/storage, NFC lifecycle, JWT/OTP/rate-limit semantics, Dockerfile, Caddy, API success DTO shapes, JaCoCo thresholds, Flyway V1–V22 content.


---

**Follow-up:** Batch E.2 closed R-P1-10 locally (all JaCoCo gates PASS). See BATCH_E2_IMPLEMENTATION_REPORT.md.
