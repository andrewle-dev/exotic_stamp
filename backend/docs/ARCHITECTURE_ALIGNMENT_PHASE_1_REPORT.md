# Architecture Alignment — Phase 1 Report

**Date:** 2026-07-11  
**Scope:** Documentation, ADR, ArchUnit enforcement only (no package moves, no entity rewrite, no API/DB contract changes).  
**Verdict:** **PASS** — Phase 1 complete. ArchUnit green; `mvn clean test` green. No temporary ArchUnit waivers.

---

## 1. Files changed

| Path | Change |
|------|--------|
| [`docs/adr/ADR-001-spring-pragmatic-ddd.md`](adr/ADR-001-spring-pragmatic-ddd.md) | **Added** — official Spring-pragmatic DDD decision |
| [`docs/architecture.md`](architecture.md) | Module status table, qualified dependency diagram, hard rule `domain ↛ application`, event flow, API coverage, ADR link |
| [`docs/IMPLEMENTATION_GUARDRAILS.md`](IMPLEMENTATION_GUARDRAILS.md) | **Rewritten** to match pragmatic defaults |
| [`src/test/java/metro/ExoticStamp/ArchitectureBoundaryTest.java`](../src/test/java/metro/ExoticStamp/ArchitectureBoundaryTest.java) | Expanded ArchUnit rules |
| [`src/main/java/.../UserDetailsServiceImpl.java`](../src/main/java/metro/ExoticStamp/modules/auth/infrastructure/security/UserDetailsServiceImpl.java) | Use `UserRepository` instead of `JpaUserRepository` (required for cross-module infra rule; behavior unchanged) |
| [`docs/ARCHITECTURE_ALIGNMENT_PHASE_1_REPORT.md`](ARCHITECTURE_ALIGNMENT_PHASE_1_REPORT.md) | This report |

---

## 2. ADR summary (ADR-001)

- **Decision:** Spring-pragmatic DDD with JPA-backed `domain/model` as default.
- **Hard rules:** domain ↛ application/presentation/infrastructure; application ↛ JpaRepository / module infrastructure; presentation ↛ infrastructure/domain.
- **Rejected:** mandatory full persistence/domain entity split.
- **Events:** prefer plain immutable types; Spring `ApplicationEvent` is legacy pragmatic, not the new template.
- **Non-goals:** mass package moves, outbox mandate, monetization implementation.

---

## 3. Corrected docs

### `architecture.md`

- Clarified dependency direction (infrastructure depends **inward**, not “under every layer”).
- Explicit hard rule: **domain must not depend on application**.
- Module status from source: collection/reward/community **implemented**; monetization **schema only**.
- Documented current collection → reward after-commit async event flow.
- API coverage table for implemented modules.

### `IMPLEMENTATION_GUARDRAILS.md`

- JPA-backed domain model = default; separate `*Entity` optional.
- Entity behavior allowed for state-local invariants; orchestration in application.
- Domain exceptions transport-agnostic (no `HttpStatus` on domain types).
- Thin pass-through adapters OK; mapper not mandatory.
- Domain events: prefer plain immutable records; Spring publish/listen outside domain template.
- Prohibited: `domain → application`; cross-module infrastructure access.
- Package convention without mass migration.

---

## 4. ArchUnit rules enforced

| Rule | Method |
|------|--------|
| domain ↛ presentation | `domainMustNotDependOnPresentation` |
| domain ↛ infrastructure | `domainMustNotDependOnInfrastructure` |
| domain ↛ application | `domainMustNotDependOnApplication` |
| application ↛ presentation | `applicationMustNotDependOnPresentation` |
| application ↛ `JpaRepository` | `applicationMustNotUseJpaRepositoryDirectly` |
| application ↛ `modules..infrastructure` | `applicationMustNotDependOnModuleInfrastructure` |
| presentation ↛ infrastructure | `presentationMustNotDependOnInfrastructure` |
| presentation ↛ domain | `presentationMustNotDependOnDomain` (strict; no approved shared types yet) |
| auth.application ↛ user.domain.repository | `authApplicationMustNotDependOnUserDomainRepository` |
| module A ↛ module B.infrastructure | `modulesMustNotDependOnOtherModulesInfrastructure` |

Package patterns are precise (`metro.ExoticStamp.modules.{module}.infrastructure..`). Shared `metro.ExoticStamp.infra..` is **not** treated as module infrastructure.

---

## 5. Existing violations exposed / fixed

| Class | Issue | Resolution |
|-------|-------|------------|
| `UserDetailsServiceImpl` | auth.infrastructure → `user.infrastructure.JpaUserRepository` | Switched to `user.domain.repository.UserRepository` (same lookups; no API change) |

No remaining ArchUnit failures. **No temporary waivers.**

### Known pragmatic debt (documented, not waived in ArchUnit)

| Item | Notes | Follow-up |
|------|-------|-----------|
| `StampCollectedEvent extends ApplicationEvent` | Legacy pragmatic coupling | Phase 2+ event cleanup (prefer plain record) |
| `User implements UserDetails` | Domain/security coupling | Opportunistic; not Phase 1 |
| application → shared `infra.mail` / `infra.storage` | Allowed soft coupling | Optional ports later |
| In-process events without outbox | Orphan stamp risk | Product decision / Phase 2+ |

---

## 6. Temporary waivers

**None.**

---

## 7. Test results

```text
./mvnw -q "-Dtest=ArchitectureBoundaryTest" test   → PASS
./mvnw -q clean test                               → PASS (exit 0)
```

Note: a non-clean run briefly hit a stale `CollectionDomainServiceTest` classfile from a prior deletion; `mvn clean test` is green.

---

## 8. Remaining Phase 2 work (out of Phase 1 scope)

Suggested next phase (not done here):

1. Opportunistic plain domain events (stop extending `ApplicationEvent` for new events; migrate `StampCollectedEvent` when safe).
2. Optional ports for shared `infra.mail` / `infra.storage`.
3. Orphan-reward re-eval job / outbox if product requires at-least-once side effects.
4. Fill remaining high-value controller tests (voucher admin, share, stamp design, scan keys, permissions).
5. Monetization Java module when product prioritizes.
6. Broader cross-module `domain.repository` ArchUnit (beyond auth→user) if more leaks appear.

---

## 9. Verdict

**Phase 1 complete.**

- Architecture is officially Spring-pragmatic DDD (ADR-001).
- Docs match source truth and ArchUnit.
- Layer and cross-module infrastructure rules are enforced without silent exclusions.
- Only runtime touch: `UserDetailsServiceImpl` dependency swap required for the new cross-module infrastructure rule — no business behavior or API contract change.
