# ADR-001: Spring-pragmatic DDD

- **Status:** Accepted
- **Date:** 2026-07-11
- **Deciders:** ExoticStamp backend maintainers
- **Related:** `docs/architecture.md`, `docs/IMPLEMENTATION_GUARDRAILS.md`, `ArchitectureBoundaryTest`

---

## Context

ExoticStamp is a Spring Boot modular monolith. Early docs mixed textbook hexagonal guidance (separate persistence entities + pure domain models) with a codebase that already uses JPA-annotated models under `domain/model`, thin repository adapters, and Command/Query application services.

We need a single, enforceable architecture decision that:

- matches how the code is actually written and reviewed
- keeps layer boundaries testable with ArchUnit
- avoids a costly rewrite into full persistence/domain separation

## Decision

The project officially uses **Spring-pragmatic DDD**:

```text
presentation -> application -> domain
infrastructure -> domain / application ports
```

Infrastructure is **not** “below every layer.” It depends inward on domain interfaces and application ports; presentation and application must not depend on infrastructure implementations.

### Allowed framework coupling

| Layer | Allowed | Not allowed |
|-------|---------|-------------|
| domain | JPA annotations on models; minimal Spring stereotypes (`@Component` on domain services) when pragmatic | `application`, `presentation`, `infrastructure`, web/HTTP types |
| application | Spring `@Service`, `@Transactional`, ports, commands/views | `JpaRepository`, `modules/*/infrastructure` |
| infrastructure | Spring Data, Redis, JWT, mail adapters, `@EventListener` | — |
| presentation | Spring MVC, validation, OpenAPI, DTO mapping | `infrastructure`, `domain` (use application views/services) |

### JPA-backed domain models

- **Default:** domain aggregates live in `domain/model` and may be annotated with `@Entity` / `@Table` / `@Column`.
- **Optional:** separate persistence `*Entity` + mapper only for high-complexity integrations where the persistence shape diverges from the domain model.
- Thin pass-through `*RepositoryAdapter` is acceptable when the domain model is the JPA entity.

### Dependency rules (hard)

1. `domain` ↛ `application` / `presentation` / `infrastructure`
2. `application` ↛ `JpaRepository` and ↛ `modules/*/infrastructure`
3. `presentation` ↛ `infrastructure` and ↛ `domain`
4. Cross-module access uses **application ports** or **integration events**, not another module’s `infrastructure` or foreign `domain.repository` (prefer ports)

Enforced by `ArchitectureBoundaryTest`.

### Repository pattern

- Domain: repository **interface only**
- Infrastructure: `Jpa*` + `*RepositoryAdapter` implementing the domain interface
- Application: depends on domain repository interfaces / ports only

### Application orchestration

- Command services own write transactions (`@Transactional`)
- Query services own read paths (`readOnly = true` when appropriate)
- Application wires policies, ports, cache eviction, and after-commit event publish

### Domain behavior

- State-local invariants and predicates may live on entities / value objects
- Cross-aggregate rules belong in domain policies / domain services
- Multi-step workflows and transaction boundaries belong in application services

### Event strategy

- Prefer **plain immutable** domain event types (records/classes with IDs and primitives)
- Publishing is an **application** concern (e.g. after-commit via `ApplicationEventPublisher`)
- Listening/adapters live in **infrastructure** (`@EventListener`, `@Async`)
- Extending Spring `ApplicationEvent` is a **legacy pragmatic choice**, not the preferred template for new events
- No mandatory transactional outbox in this ADR; document orphan-risk if using in-process events

### Cross-module ports

- Module A must not import Module B’s `infrastructure`
- Prefer ports in Module B’s `application.port` implemented by B’s infrastructure
- Integration events may cross modules when payload is stable and ID-based

## Consequences

### Positive

- Docs match code; less “pure DDD” pressure that never lands
- ArchUnit can fail PRs that invert layers
- Faster feature delivery without dual-model mapping tax

### Negative / risks

- Domain models carry persistence annotations (framework leak)
- Some domain types still couple to Spring (e.g. `User`/`UserDetails`, legacy `ApplicationEvent` subclasses) — accepted and documented, migrate opportunistically
- In-process events without outbox can leave side effects undelivered after commit

## Rejected alternative: full persistence/domain separation

Rejected for now because:

- high rewrite cost across all modules
- current adapters are already thin pass-throughs
- product velocity and Flyway-first schema do not require dual models today

Revisit only with an explicit ADR amendment and migration plan.

## Migration policy

- **New modules / new files:** follow canonical packages (`application/service`, `presentation/controller`, `infrastructure/repository`, `domain/model`) and this ADR
- **Grandfathered layouts** (auth/user/rbac/metro root-level services/controllers): keep local layout; do not mass-move
- **Legacy Spring `ApplicationEvent` domain events:** leave until a dedicated event cleanup phase
- **Cross-module infra leaks:** fix when touched or when ArchUnit fails; prefer domain repository / port

## Non-goals

- Rewriting all entities into separate persistence models
- Repo-wide package renames
- Introducing transactional outbox as part of accepting this ADR
- Removing Spring Security from `User` in the same change set
- Implementing monetization business code
