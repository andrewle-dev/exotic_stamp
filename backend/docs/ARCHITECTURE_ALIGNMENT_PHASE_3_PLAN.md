# Architecture Alignment — Phase 3 Plan

**Date:** 2026-07-11  
**Scope:** Post-collection side-effect reliability (reward-critical path). Design only — **no production code in this document’s task**.  
**Source basis:** Phase 2 report, current `src/` listeners/services/schemas/tests, product docs.

---

## 1. Verdict (read this first)

| Question | Answer |
|----------|--------|
| Safe to retain in-process listeners alone? | **No** — not production-ready for reward outcomes |
| Reconciliation required? | **Yes** — primary durable recovery for MVP |
| Transactional outbox required? | **Not for MVP** — optional later if lag/ops demand event-level at-least-once |

**Chosen architecture:** keep AFTER_COMMIT + `@Async` listeners as the **fast path**, and add a **deterministic reward reconciliation job** as the **durable recovery path**.

**Requirement class for reward issuance after stamp collection:**

> **B — eventually repaired** (outcome must not stay permanently missing).  
> Not **C** (best-effort only).  
> Not **A** (event-delivery at-least-once) as an MVP mandate — because evaluation is **state-derived and re-runnable**.

Do **not** call the current event flow production-ready: failure recovery is **not durable** today.

---

## 2. Current-state diagnosis

### Happy path (Phase 2)

```text
CollectionCommandService.collect (@Transactional)
  → save UserStamp
  → publish StampCollectedEvent (in-process, inside TX)
  → commit
  → @TransactionalEventListener(AFTER_COMMIT) + @Async listeners
       → reward: RewardEvaluationService.handleStampCollected(userId, campaignId)
       → community: completePendingReferral(userId)
       → collection: metrics/log
```

### What already works

| Mechanism | Evidence |
|-----------|----------|
| After-commit delivery intent | Listeners use `TransactionPhase.AFTER_COMMIT` |
| Immutable event payload | `StampCollectedEvent` IDs/primitives only |
| Duplicate reward prevention | `uq_user_rewards_once (user_id, milestone_id)` + evaluation skip |
| Voucher concurrency | `FOR UPDATE SKIP LOCKED` in `JpaVoucherPoolRepository` |
| In-process retry | Reward listener: `stampCollectedEventMaxAttempts` (default 3) + backoff |
| Orphan observability | `reward.stamp_collected.orphan`, `collection.stamp_collected.publish_failed` |
| Deterministic re-eval | `handleStampCollected` recomputes stamp count vs milestones (`MilestoneDomainService`) |
| Precedent durable queue | `mail_jobs` + `MailWorker` (SKIP LOCKED poller) — **not** used for rewards |

### What does **not** work for production reward reliability

1. **No durable work record** for “this stamp needs reward evaluation.”
2. **`publishEvent` is caught** in `CollectionCommandService` → stamp can commit with **zero** downstream registration.
3. **Process crash / deploy / executor loss** after commit → event never runs; no redelivery.
4. Listener catch after in-memory retries → **metric + log only**; comment says “re-eval manually” — **no automated repair**.
5. Redis dedup is **best-effort / fail-open** — helpful against duplicates, **not** a durability store.
6. `PENDING_STOCK` is a committed business state with **no restock allocator** — separate from event loss, but related to “user expected a voucher.”

---

## 3. Requirement classification (source-grounded)

### Product / docs

| Source | Signal |
|--------|--------|
| `docs/EXOTIC_STAMP_CONTEXT.md` | Core loop: complete collection → receive reward / partner voucher |
| `docs/DEMO_SEED.md` | After collect 1/3/5 stamps, check `GET /api/v1/rewards/my` |
| `docs/working_pipeline.md` | Reward async; “may lag if listener fails — no outbox yet” |
| `docs/architecture.md` | Explicit orphan-stamp risk; metrics alert |
| ADR-001 / alignment plan | Outbox deferred unless at-least-once **outcome** required |

### Code / schema / tests

| Source | Signal |
|--------|--------|
| Milestone evaluation | Pure function of stamp count + active milestones − existing `user_rewards` |
| `uq_user_rewards_once` | Duplicate delivery is **safe** for issuance |
| `RewardConcurrencyIT` | Concurrent duplicate events → one reward; last voucher → one ISSUED + one PENDING_STOCK |
| Listener orphan metric + “re-eval manually” | Team already treats missing reward as **ops defect**, not acceptable silent drop |
| No admin re-eval API / job today | Gap between intent and automation |

### Classification decision

| Side effect | Class | Rationale |
|-------------|-------|-----------|
| **Reward evaluation / issuance** | **B — eventually repaired** | Product-critical; evaluation is idempotent & state-derived; permanent miss is unacceptable |
| Community referral complete-on-collect | **C — best-effort** (acceptable) | Also completed on `EmailVerifiedEvent`; stamp is secondary trigger |
| Collection stamp-collected metrics | **C — best-effort** | Observability only |
| RewardIssued → community notification | **C / soft B** | UX; not stamp integrity; out of Phase 3 critical path unless product elevates |

**Primary question answer:** reward issuance must be **B (eventually repaired)**, not best-effort. Event-level **A** is a stronger delivery guarantee than MVP needs **if** reconciliation exists and is correct.

---

## 4. Listener inventory

### 4.1 `collection...StampCollectedEventListener`

| Field | Detail |
|-------|--------|
| Side effect | Log + Micrometer `collection.stamp_collected`; Redis claim-first dedup |
| Business criticality | **Low** (ops/telemetry) |
| Idempotency key | `eventId` via `StampCollectedDedupPort` |
| Transaction boundary | AFTER_COMMIT; handler not `@Transactional` |
| Retry behavior | None beyond single attempt |
| Failure behavior | Catch → log; stamp unaffected |
| Durability | **None** |
| Duplicate risk | Low (Redis SETNX); fail-open may double-count metrics |
| Data-loss risk | Metric undercount only |

### 4.2 `reward...StampCollectedEventListener` (`rewardStampCollectedEventListener`)

| Field | Detail |
|-------|--------|
| Side effect | `RewardEvaluationService.handleStampCollected(userId, campaignId)` → may insert `user_rewards`, allocate voucher |
| Business criticality | **High** |
| Idempotency key | Redis `eventId` done/lock **plus** DB `uq_user_rewards_once` |
| Transaction boundary | AFTER_COMMIT async → new TX inside evaluation `@Transactional` |
| Retry behavior | In-process loop (default 3 × ~200ms); **not durable** |
| Failure behavior | Catch → `reward.stamp_collected.orphan`; stamp stays; **no auto repair** |
| Durability | **None** (in-memory event + Redis markers) |
| Duplicate risk | Safe for rewards (DB unique); Redis fail-open can re-enter evaluation |
| Data-loss risk | **High** for “eligible milestone never issued” after crash / publish catch / exhausted retries |

### 4.3 `community...CommunityIntegrationListener.onStampCollected`

| Field | Detail |
|-------|--------|
| Side effect | `ReferralCommandService.completePendingReferral(userId)` |
| Business criticality | **Medium-low** (referral completion also on email verify) |
| Idempotency key | Referral row status (`PENDING` → `COMPLETED`); natural idempotency |
| Transaction boundary | AFTER_COMMIT async → service `@Transactional` |
| Retry behavior | None |
| Failure behavior | Catch → log |
| Durability | **None** |
| Duplicate risk | Low (status guard) |
| Data-loss risk | Pending referral may stay pending until email-verify path or manual fix |

---

## 5. Critical review (confirmed failure modes)

### 1. Catching `publishEvent` → committed stamp, no recovery record

**Confirmed.** `CollectionCommandService` wraps `eventPublisher.publishEvent(...)` in try/catch, increments `collection.stamp_collected.publish_failed`, and still returns success. There is **no** outbox/job row. Without reconciliation, this is a **permanent miss** path.

### 2. AFTER_COMMIT + `@Async` can lose work

| Scenario | Loses reward work? |
|----------|--------------------|
| Process crash after commit, before/during async | **Yes** |
| Deploy/restart mid-flight | **Yes** |
| Executor rejects / pool saturated | **Yes** (default `@EnableAsync` only; no custom rejection→persist policy) |
| Listener throws after retries | **Yes** (orphan metric only) |
| Transient DB/Redis outage during listener | Retries help briefly; then **Yes** if exhausted |
| Redis down | Dedup fail-open → evaluation may still run if listener invoked; does **not** create durability |

### 3. Listener catch = retry or only logs/metrics?

Reward: **bounded in-process retry**, then **logs + orphan metric**. **No** durable retry queue.  
Collection/community: **logs only**.

### 4. Duplicate event delivery safety

**Yes for issuance.** `RewardConcurrencyIT.duplicateEvent_sameUser_onlyOneUserReward` + `uq_user_rewards_once` + evaluation skip on `RewardAlreadyIssuedException` / unique violation.

### 5. Voucher allocation concurrency

**SKIP LOCKED** (`lockNextAvailableForMilestone`). IT proves last voucher → one `ISSUED`, one `PENDING_STOCK`.

### 6. Unique constraint vs partial allocation retry

Within one evaluation TX: save `UserReward` then allocate; failure before commit rolls back both.  
If voucher missing: row commits as `PENDING_STOCK` — **not** a transient failure; **no** automatic restock job today. Re-running `handleStampCollected` **will not** upgrade `PENDING_STOCK` (milestone already in `rewardedMilestoneIds`). Restock repair is a **separate** Phase 3+/ops concern.

### 7. Permanent stamp without reward after transient failure

**Yes, possible today** (publish catch, crash, exhausted retries, deploy). Metrics exist; automation does not.

### 8. Async before committed data visible?

On single primary DB: AFTER_COMMIT then async → stamp **should** be visible to `countDistinctStations...`.  
Risk only with exotic read-replica lag (not present in current design). **Not** the primary failure mode.

---

## 6. Option comparison

| | Option 1: AFTER_COMMIT + reconciliation job | Option 2: Transactional outbox + poller | Option 3: Sync reward in collect TX | Option 4: Spring Retry on listeners only |
|--|---------------------------------------------|-----------------------------------------|--------------------------------------|------------------------------------------|
| Consistency guarantee | **Outcome eventually correct** (bounded lag) | **Event at-least-once** + same idempotent consumer | Strong sync consistency; couples collect↔reward | Transient only; **no crash durability** |
| Code complexity | Low–medium (job + query + reuse evaluation) | Medium–high (table, writer, poller, status machine) | Low code, high coupling | Low |
| Operational complexity | Cron + lag SLO + orphan alert | Job table monitoring (like mail) | Collect latency/lock contention | Alerts still fire; manual repair |
| Duplicate handling | Existing DB unique | Outbox dedup key + DB unique | Same TX natural | Same as today |
| Process-crash behavior | Job repairs | Outbox row survives | N/A if committed together; collect fails if reward fails | **Lost** |
| DB load | Periodic scan / watermark query | Continuous poll + writes | Extra locks on collect path | None extra |
| MVP suitability | **Best fit** | Overkill if reconcile lag OK | Poor UX (collect blocked / rolled back by voucher issues) | Insufficient |
| Scale migration | Add outbox later for lower lag / multi-consumer | Already event-pipeline shaped | Hard to reverse | Dead end |

**Kafka / external brokers:** not required by source evidence; mail already uses DB poller pattern in-process.

---

## 7. Chosen architecture (MVP)

### Pattern

```text
Fast path (unchanged shape):
  collect commit → AFTER_COMMIT async → RewardEvaluationService

Durable path (new):
  scheduled RewardReconciliationJob
    → find (userId, campaignId) eligible for missing milestone rewards
    → call same RewardEvaluationService.handleStampCollected
    → metrics: repaired / scanned / lag
```

### Why not outbox first

1. Evaluation is **deterministic from `user_stamps` + milestones + `user_rewards`** — the stamp row **is** the durable fact.
2. Smallest design that prevents **permanent** missing rewards.
3. Codebase already has mail outbox-like jobs; copying that for rewards adds schema/ops before proving lag is a problem.
4. Outbox remains the **scale upgrade** if reconcile lag or multi-side-effect fan-out becomes painful.

### Publish failure policy (mandatory)

| Policy | Decision |
|--------|----------|
| Catch `publishEvent` and continue **without** durable recovery | **Forbidden** |
| With reconciliation deployed | Catch **allowed** (collect UX preserved); publish_failed metric retained; job must cover the gap |
| Prefer long-term | Persist nothing extra if reconcile scans stamps; optionally fail-soft publish remains |

**Do not** fail the collect transaction solely because in-process publish failed — stamp collection is the primary user action; repair reward asynchronously.

**Do not** keep “catch and continue” in production **until** reconciliation is live (or an outbox row is written in the same TX).

### What stays best-effort

- Collection metrics listener  
- Community referral-on-collect (email-verify remains primary completion path)  
- `RewardIssuedEvent` → notification (separate backlog if product elevates)

### Optional follow-on (same phase or Phase 3.1)

- **PENDING_STOCK restock allocator:** when vouchers are uploaded, allocate to oldest `PENDING_STOCK` rows (not solved by stamp reconciliation alone).

---

## 8. Schema changes

### MVP (preferred): **no new tables**

Reconciliation query from existing tables:

- `user_stamps` (user_id, campaign_id, collected_at, …)
- `milestones` (campaign_id, stamps_required, status, …)
- `user_rewards` (user_id, milestone_id, …)

Watermark options (pick one at implement time):

1. Configured lookback window (e.g. last N hours of stamps) + full catch-up cron nightly, or  
2. Small `reward_reconciliation_state` singleton row (`last_scanned_at`) — optional, not required for v1.

### Later (if moving to Option 2)

New `stamp_collected_outbox` (or generic `domain_outbox`) with status/retry — mirror `mail_jobs` semantics. **Not** in MVP recommendation.

### Explicitly out of scope for schema

- Changing `uq_user_stamps_collect` / `uq_user_rewards_once`  
- Event store / Kafka topics  

---

## 9. Exact files expected to change (implementation phase)

| Area | Likely files |
|------|----------------|
| Job | `reward/application/service/RewardReconciliationService.java` (new), `RewardReconciliationJob.java` (new `@Scheduled`) |
| Query port | `reward/application/port/...` + adapter querying stamp/milestone gaps |
| Config | `RewardProperties` — cron, batch size, lookback |
| Metrics | counters/timers for scanned, repaired, still_missing, lag |
| Docs | `architecture.md`, `working_pipeline.md` — remove “manual only” orphan story |
| Publish policy comment | `CollectionCommandService` — document dependence on reconciliation |
| Tests | see §12 |
| Optional admin | `POST /api/v1/admin/rewards/re-evaluate` (ops) — nice-to-have |

**Non-goals for that PR:** Kafka, rewriting listeners to sync, community outbox, monetization.

---

## 10. Retry and idempotency design

| Layer | Behavior |
|-------|----------|
| Fast path | Keep current in-process retries (3) for transient blips |
| Durable path | Reconciliation re-invokes `handleStampCollected` |
| Issuance idempotency | `uq_user_rewards_once` + pre-check |
| Voucher idempotency | SKIP LOCKED + `uq_vp_assigned_user_reward` |
| Event Redis dedup | Keep as optimization; **must not** block reconciliation (reconcile must not depend on `eventId`) |
| Poison stamp/user | Cap per-run attempts; metric `reward.reconciliation.poison`; alert; do not infinite-loop hot row |
| Worker crash after reward commit | Safe: unique constraint; next reconcile no-ops |

---

## 11. Observability

| Signal | Purpose |
|--------|---------|
| `reward.stamp_collected.orphan` | Fast-path exhausted (existing) |
| `collection.stamp_collected.publish_failed` | Publish catch (existing) |
| `reward.reconciliation.scanned` | Job activity |
| `reward.reconciliation.repaired` | Proof of durable recovery |
| `reward.reconciliation.still_missing` | Gap after pass → page |
| `reward.reconciliation.lag_seconds` | SLO (e.g. p99 repair &lt; 5–15 min) |
| Alert | `still_missing &gt; 0` for sustained window, or orphan without subsequent repair |

---

## 12. Test plan

| # | Scenario | Expectation |
|---|----------|-------------|
| 1 | Collect TX rollback | No stamp; reconcile finds nothing; no reward |
| 2 | Collect commit | Fast path registers work **or** stamp visible to reconcile; reward issued exactly once for milestone |
| 3 | Process restart before async runs | Reconcile issues missing reward |
| 4 | Duplicate event/work delivery | Single `user_rewards` row |
| 5 | Reward service transient failure then retry | Fast-path retry **or** reconcile repairs |
| 6 | Voucher allocation partial failure (TX abort) | No orphan CLAIMED without user_reward; retry safe |
| 7 | Two users, last voucher | One ISSUED, one PENDING_STOCK (existing IT) |
| 8 | Same user duplicate event | One reward (existing IT) |
| 9 | Crash after reward commit before “mark complete” | No duplicate reward; Redis mark optional |
| 10 | Poison exceeds retry limit | Metric; stamp intact; bounded reconcile skips/alerts |
| 11 | Reconcile detects stamp without reward | Issues reward via same evaluation service |
| 12 | Executor rejection / publish catch | Stamp committed; reconcile repairs (no permanent miss) |

---

## 13. Rollout plan

1. Ship reconciliation job **behind enabled flag**, dry-run metric-only mode (detect gaps, do not repair).
2. Validate gap rate in staging/prod shadow.
3. Enable repair; keep AFTER_COMMIT listeners.
4. Update runbooks: orphan metric → expect auto-repair within SLO; escalate if `still_missing`.
5. Only then treat publish catch as acceptable long-term.
6. (Later) Consider outbox if lag SLO fails or fan-out grows.

---

## 14. Rollback plan

1. Disable reconcile flag → system returns to Phase 2 behavior (known orphan risk).
2. Listeners unchanged → no collect-path rollback needed.
3. If a bad reconcile build over-issued: rely on `uq_user_rewards_once` (should prevent); voucher mistakes need ops playbook (release/claim audit).

---

## 15. Non-goals

- External message brokers / Kafka  
- Making community/metrics durable in the same MVP slice  
- Synchronous reward inside collect TX  
- Full transactional outbox as mandatory MVP  
- Rewriting Spring `ApplicationEvent` → plain records (orthogonal)  
- Automatic PENDING_STOCK restock (track separately; call out in ops)  
- Changing collect API contracts or stamp uniqueness rules  

---

## 16. Final verdict box

```text
SAFE TO RETAIN IN-PROCESS LISTENERS ALONE?     NO
RECONCILIATION REQUIRED?                       YES  (MVP durable recovery)
TRANSACTIONAL OUTBOX REQUIRED?                 NO   (MVP); YES as scale upgrade if needed

REWARD REQUIREMENT CLASS:                      B — eventually repaired
CURRENT FLOW PRODUCTION-READY?                 NO  (until durable recovery ships)
PUBLISH CATCH WITHOUT DURABLE RECOVERY?        FORBIDDEN
PUBLISH CATCH WITH RECONCILIATION LIVE?        ALLOWED (prefer keep collect success)
```

---

## 17. Implementation readiness checklist (for the coding phase)

- [ ] `RewardReconciliationService` + scheduled job  
- [ ] Gap query (stamp-derived, not eventId-derived)  
- [ ] Metrics + alerts  
- [ ] Tests 1–12 above (unit + IT)  
- [ ] Docs update: orphan → auto-repair SLO  
- [ ] Feature flag + rollout  
- [ ] Explicit decision log: PENDING_STOCK restock deferred or included  

**End of Phase 3 plan.**
