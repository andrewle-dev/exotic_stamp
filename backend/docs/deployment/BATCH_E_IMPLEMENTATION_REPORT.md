# Batch E Implementation Report — Reward, Voucher, Idempotency, Transaction Integrity

**Date:** 2026-07-24  
**Audit companions:** BACKEND_PRODUCTION_READINESS_AUDIT.md v2, BACKEND_REMEDIATION_PLAN.md v2  
**Remediation IDs:** R-P1-01 / F-009; R-P1-02 / F-008; R-P1-03 / F-016; R-P1-09 / F-024; R-P1-10 / F-019  

---

## 1. Verdict

**PARTIAL** (see `BATCH_E1_IMPLEMENTATION_REPORT.md` for closure of fingerprint, pending-stock, and multi-instance gaps)

| Item | Status |
|------|--------|
| R-P1-01 Reliable reward completion (Option B) | **DONE** (async listener retained + scheduled/admin reconcile + TX policy fix) |
| R-P1-02 Voucher uniqueness both directions | **DONE** (V22 partial unique on user_rewards.voucher_pool_id) |
| R-P1-03 Collection idempotency alignment | **DONE** (map uq_user_stamps_user_idempotency → idempotent replay; documented) |
| R-P1-09 Soft-delete-aware default campaign unique | **DONE** (V22 + existsActiveDefaultByLineId + clear isDefault on soft-delete) |
| R-P1-10 Meaningful reward/collection coverage | **PARTIAL** (new unit/ITs added; JaCoCo gates still not claimed green — thresholds not lowered) |

---

## 2. Option comparison (R-P1-01)

| Option | Description | MVP choice |
|--------|-------------|------------|
| **A** Sync issue in collect TX | Couples collect latency to voucher allocation; stronger delivery | Rejected for latency/coupling |
| **B** Async AFTER_COMMIT + idempotent reconcile | Keeps current listener; recovers missed events via cron/admin | **Selected** |
| **C** Transactional outbox | Durable delivery / multi-worker | Deferred until ops pain justifies complexity |

Failure window addressed:

`	ext
stamp TX committed → AFTER_COMMIT @Async listener never completes → reward missing
→ RewardReconcileService finds (user, campaign) with met milestones and no user_rewards
→ RewardEvaluationService.handleStampCollected (idempotent via uq_user_rewards_once)
`

---

## 3. Reward path sequence (inventory)

| Step | TX / thread | DB / lock | Failure / retry / recovery |
|------|-------------|-----------|----------------------------|
| Collect save stamp | Write TX (request) | Insert user_stamps; unique uq_user_stamps_collect, uq_user_stamps_user_idempotency | Idempotency race → replay existing; station race → already collected |
| Publish StampCollectedEvent | Same write TX | — | Publish failure metered; stamp still committed |
| Commit | — | — | Listener scheduled AFTER_COMMIT |
| Listener | @Async | Redis dedup + processing lock | Retry with backoff; miss → reconcile |
| Evaluate milestones | Write TX (async) | Count stamps; insert user_rewards (uq_user_rewards_once) | Duplicate unique → skip |
| Allocate voucher | Same write TX | FOR UPDATE SKIP LOCKED; link oucher_pool + user_rewards.voucher_pool_id (uq_user_rewards_voucher_pool_id, uq_vp_assigned_user_reward) | Empty → PENDING_STOCK; link race → release + PENDING_STOCK |
| Side effects | afterCommit | Audit / RewardIssuedEvent | Best-effort |
| Reconcile | Scheduled / admin | Candidate SQL + same evaluate path | Idempotent |

Events use: ApplicationEventPublisher inside collect TX → @TransactionalEventListener(AFTER_COMMIT) + @Async (no outbox).

---

## 4. Transaction annotation fix

RewardIssuancePolicyService previously had class-level @Transactional(readOnly = true). Confirmed hazard: joining the outer write TX in RewardEvaluationService could mark the shared connection read-only. **Removed** all class-level transactional annotations; policy methods participate in the caller TX without forcing read-only.

---

## 5. Exact files changed (primary)

### Flyway
- db/migration/V22__reward_voucher_and_campaign_integrity.sql

### Reward
- RewardIssuancePolicyService.java (no read-only TX)
- RewardEvaluationService.java (voucher link unique race)
- RewardReconcileService.java; RewardReconcileCandidatePort.java; RewardReconcileCandidateAdapter.java
- RewardProperties.java, pplication.yml (reconcile cron/lookback/batch)
- AdminRewardController.java + RewardReconcileResponse.java

### Collection / campaign
- CollectionCommandService.java (idempotency unique race)
- CollectionPolicyService.java (alignment docs)
- JpaCampaignRepository.java, CampaignRepositoryAdapter.java
- CampaignCommandService.java (clear default on soft-delete)

### Tests / CI / docs
- FlywayV22MigrationIT, RewardReconcileIT, unit tests
- scripts/ci/expected_integration_tests.txt
- This report; remediation plan status updates

---

## 6. Constraints after V22

| Constraint | Purpose |
|------------|---------|
| uq_user_rewards_once (user_id, milestone_id) | One reward per user/milestone |
| uq_user_rewards_voucher_pool_id (partial) | One reward per voucher pool id |
| uq_vp_assigned_user_reward (V16) | One voucher assignment per reward |
| uq_user_stamps_collect | One stamp per user/station/campaign |
| uq_user_stamps_user_idempotency | Permanent per-user idempotency key |
| uq_campaigns_default_per_line WHERE deleted_at IS NULL | Soft-delete-aware default |

---

## 7. Verification notes

- Targeted ITs: FlywayV22MigrationIT, RewardReconcileIT PASS
- Full mvn clean verify -Pci + jacoco scripts recorded in §8 when run
- JaCoCo thresholds **not** lowered; R-P1-10 remains PARTIAL until gates pass

---

## 8. CI / coverage snapshot

| Metric | Value |
|--------|-------|
| Surefire | **531** / 0 fail / 0 skip |
| Failsafe | **40** / 0 fail / 0 skip |
| Manifest assert (--strict) | **PASS** (17/17 executed) |
| Overall line / branch | **54.05% / 40.75%** |
| auth line / branch | **59.95% / 53.62%** |
| collection line / branch | **64.67% / 47.42%** |
| reward line / branch | **37.57% / 26.47%** |
| JaCoCo CI gates (60%/50%) | **FAIL** (expected; thresholds not lowered) |
| Latest Flyway | **V22** |

Reward line coverage improved vs Batch B baseline (~36% → ~37.6%) with meaningful reconcile/evaluation/idempotency tests; gates remain open.
