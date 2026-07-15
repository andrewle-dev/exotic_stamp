# Architecture Alignment — Phase 2 Report

**Date:** 2026-07-11  
**Scope:** Collection module only — remove `domain → application`, extract domain policies, harden after-commit stamp events.  
**Verdict:** **PASS** — Phase 2 complete. ArchUnit green; `./mvnw clean test` green (exit 0). No temporary ArchUnit waivers. No outbox.

---

## 1. Executive summary

Phase 2 closes the confirmed layer inversion and moves collection anti-cheat / eligibility / GPS **decisions** into pure domain policies. Application services still **load** state (repositories, config, views) and orchestrate the transaction. Stamp-collected listeners now use `@TransactionalEventListener(phase = AFTER_COMMIT)` so side effects run only after commit. API contracts, Flyway migrations, unique constraints, and idempotency behavior are unchanged.

---

## 2. Task 1 — Logic classification

| Concern | Former home | Classification | Phase 2 placement |
|---------|-------------|----------------|-------------------|
| Already collected (user+station+campaign) | `CollectionPolicyService` / facade | **Cross-aggregate domain policy** | `CollectionDuplicatePolicy.assertNotAlreadyCollected` (app loads `exists`) |
| Idempotency replay ownership | `CollectionPolicyService` | **Cross-aggregate domain policy** | `CollectionDuplicatePolicy.assertReplayBelongsToUser` |
| Idempotency key lookup / window | `CollectionPolicyService` | **Application orchestration** | stays in `CollectionPolicyService` |
| Campaign–station membership | `CollectionCommandService` | **Cross-aggregate domain policy** | `CollectionEligibilityPolicy.assertCampaignStationEligible` |
| Campaign active + time window | `DefaultCampaignResolver` | **Cross-aggregate domain policy** | `CollectionEligibilityPolicy.assertCampaignCollectable` / `isInWindow` |
| GPS required / invalid / accuracy / radius | `GpsValidationService` | **Cross-aggregate domain policy** | `CollectionGpsPolicy` (app maps station view + earth radius config) |
| Zone radius clamp defaults | GPS helper | **Domain policy** (pure numbers) | `CollectionGpsPolicy.resolveZoneRadius` |
| Station inactive | scan resolver / metro | **Application orchestration** (+ metro domain) | unchanged path via `StationScanResolverPort` |
| Save stamp, catch `DataIntegrityViolationException` | `CollectionCommandService` | **Application orchestration** + **infrastructure** (DB) | unchanged; `uq_user_stamps_collect` is concurrency backstop |
| Cache eviction | `CollectionCommandService` | **Infrastructure concern** via port | unchanged |
| Audit scheduling | helpers / `RbacTransactionCallbacks` | **Application / infrastructure** | unchanged |
| Event publish | `CollectionCommandService` | **Application orchestration** | publish **inside** TX; listeners AFTER_COMMIT |
| Dedup / metrics in listeners | infrastructure listeners | **Infrastructure concern** | unchanged semantics; AFTER_COMMIT + `@Async` |

**Not moved mechanically:** `CollectionPolicyService`, `GpsValidationService`, and `DefaultCampaignResolver` remain application adapters that load data and call domain policies. Repository I/O stays out of domain.

**Entity-local invariants:** none of the former policy methods were pure single-entity mutators; uniqueness is enforced by DB + cross-aggregate checks.

---

## 3. Target design (implemented)

```text
collection/domain/policy/
  CollectionDuplicatePolicy
  CollectionGpsPolicy
  CollectionEligibilityPolicy
```

Rules followed:

- Domain policies import only domain models/exceptions/values — **no application imports**.
- Application loads required state and passes booleans / locations / campaigns into policies.
- Policies throw transport-agnostic domain exceptions (**no `HttpStatus`** in `collection.domain`).
- Deprecated `CollectionDomainService` facade: **absent** (deleted in Phase 1; Phase 2 does not restore it). Grep of `src/` shows no production or test bean dependency.

---

## 4. Files changed (Phase 2)

| Path | Change |
|------|--------|
| `.../domain/policy/CollectionDuplicatePolicy.java` | **Added** |
| `.../domain/policy/CollectionGpsPolicy.java` | **Added** |
| `.../domain/policy/CollectionEligibilityPolicy.java` | **Added** |
| `.../domain/event/StampCollectedEvent.java` | Documented AFTER_COMMIT / immutable payload / crash limitation |
| `.../application/support/CollectionPolicyService.java` | Delegates decisions to `CollectionDuplicatePolicy` |
| `.../application/support/GpsValidationService.java` | Maps views/config → `CollectionGpsPolicy` |
| `.../application/support/DefaultCampaignResolver.java` | Uses `CollectionEligibilityPolicy` |
| `.../application/service/CollectionCommandService.java` | Eligibility policy; publish inside TX with publish-failure metric |
| `.../collection/.../StampCollectedEventListener.java` | `@TransactionalEventListener(AFTER_COMMIT)` + `@Async` |
| `.../reward/.../StampCollectedEventListener.java` | Same AFTER_COMMIT |
| `.../community/.../CommunityIntegrationListener.java` | `onStampCollected` → AFTER_COMMIT |
| `.../domain/policy/CollectionDomainPolicyTest.java` | **Added** domain policy unit tests |
| `.../application/CollectionCommandServiceTest.java` | Campaign ineligible + existing collect/GPS/idempotency/event cases |
| `.../infrastructure/event/StampCollectedEventListenerTest.java` | Event ctor includes `source` |
| `ArchitectureBoundaryTest.java` | `collectionDomainMustNotDependOnApplication` |
| `docs/ARCHITECTURE_ALIGNMENT_PHASE_2_REPORT.md` | This report |

**Not changed:** Flyway SQL behavior, REST contracts, unique constraint definitions.

---

## 5. Data integrity preservation

| Invariant | Evidence |
|-----------|----------|
| Unique collect `(user_id, station_id, campaign_id)` | DB `uq_user_stamps_collect` (V3); app pre-check via `CollectionDuplicatePolicy`; race → `DataIntegrityViolationException` → `StampAlreadyCollectedException` (`CollectionCommandService` + `collect_dataIntegrity_mapsToStampAlreadyCollected`) |
| Idempotency key replay | `CollectionPolicyService.resolveIdempotentReplay` + ownership policy; `collect_idempotencyKey_returnsExisting` |
| Duplicate collection rejected | `collect_duplicate_throwsConflict` |
| GPS validation | `CollectionGpsPolicy` + `GpsValidationService`; `collect_gpsMissing_fails` / `collect_gpsOutsideRadius_fails` / domain policy tests |
| Inactive station | `collect_inactiveStation_rejected` |
| Campaign ineligible | `CollectionEligibilityPolicy` + `collect_campaignStationIneligible_rejected` |
| Cache invalidation | still `cachePort.evictAllForUserCollection` after successful save |
| Transaction boundary | `@Transactional` on `collect`; event listeners AFTER_COMMIT |

**Concurrency note:** Application-level `exists` checks are **not** treated as sufficient under race; the unique constraint remains the final defense.

---

## 6. Event consistency

```text
collect TX:
  save UserStamp
  publish StampCollectedEvent (immutable IDs/primitives)
  schedule audit afterCommit
→ commit
→ @TransactionalEventListener(AFTER_COMMIT) + @Async listeners
```

| Requirement | Status |
|-------------|--------|
| Deliver only after successful commit | Listeners use `TransactionPhase.AFTER_COMMIT` |
| `@Async` safe payload | Event carries UUIDs / `LocalDateTime` / `CollectMethod` only — no lazy JPA |
| Listener idempotent | Collection + reward dedup ports |
| Listener failure must not roll back stamp | try/catch in listeners; orphan metric on reward exhaust |
| Publish failure must not corrupt stamp | try/catch around `publishEvent`; `collection.stamp_collected.publish_failed`; `collect_eventPublishFailure_stillSucceeds` |
| Process crash after commit before listener | **Documented limitation** on `StampCollectedEvent` — no outbox in this phase |

---

## 7. ArchUnit and required tests

| # | Requirement | Coverage |
|---|-------------|----------|
| 1 | Domain has no application imports | `domainMustNotDependOnApplication` + `collectionDomainMustNotDependOnApplication` |
| 2 | Valid collection succeeds | `collect_newStamp_success` |
| 3 | Duplicate rejected | `collect_duplicate_throwsConflict` |
| 4 | Concurrent duplicate → DB constraint | `collect_dataIntegrity_mapsToStampAlreadyCollected` |
| 5 | Invalid GPS rejected | GPS command + `CollectionDomainPolicyTest` |
| 6 | Inactive station rejected | `collect_inactiveStation_rejected` |
| 7 | Campaign ineligible rejected | `collect_campaignStationIneligible_rejected` + domain policy test |
| 8 | Idempotent replay | `collect_idempotencyKey_returnsExisting` |
| 9 | Event after commit | AFTER_COMMIT listeners; publish after save inside TX |
| 10 | Reward gets immutable committed data | Reward listener uses `getUserId()` / `getCampaignId()` / `getEventId()` only |
| 11 | Publish/listener failure ≠ stamp corruption | `collect_eventPublishFailure_stillSucceeds`; listener exception swallowed + orphan metric |
| 12 | No deprecated facade remaining | No `CollectionDomainService` in `src/`; no Spring bean / test wiring |

**Temporary ArchUnit waiver for `CollectionDomainService`:** none (class deleted; no exclusion).

---

## 8. Test results

```text
./mvnw -q clean test   → PASS (exit 0)
```

---

## 9. Behavior-preservation evidence (API / DB)

- Collect orchestration order unchanged: idempotency → resolve station → campaign → eligibility → design → GPS → duplicate assert → save → cache → event → audit callback.
- Same domain exception types surface to the existing global exception handler (no new HTTP mapping required).
- Migrations not edited for behavior; V12 comment still mentions historical `CollectionDomainService` name only (checksum-safe; not a runtime dependency).
- Unique + idempotency indexes/constraints unchanged.

---

## 10. Remaining work (out of Phase 2)

1. Optional plain-record domain events (stop extending `ApplicationEvent`) when safe.
2. Outbox / re-eval job if product requires durable at-least-once reward delivery.
3. Optional ports for shared `infra.mail` / `infra.storage` (other modules).
4. Broader package cleanup — explicitly **not** done here.

---

## 11. Non-goals confirmed

- No unrelated package moves  
- No entity rewrite / separate `*Entity` layer  
- No outbox implementation  
- No monetization Java work  
- No API or DB contract changes  
