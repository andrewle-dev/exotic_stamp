# IMPLEMENTATION GUARDRAILS - EXOTIC STAMP

> Checklist khi implement từng file.  
> **Official style:** Spring-pragmatic DDD — [`docs/adr/ADR-001-spring-pragmatic-ddd.md`](adr/ADR-001-spring-pragmatic-ddd.md).  
> **Stable overview:** [`docs/architecture.md`](architecture.md).

---

## I. FILE STRUCTURE TEMPLATE (per module)

**Default for new modules.** Grandfathered modules (auth/user/rbac/metro) may keep services/controllers at package root — do **not** mass-migrate.

```text
src/main/java/metro/ExoticStamp/modules/{moduleName}/
├── domain/
│   ├── model/                 # aggregates/enums/VOs — @Entity allowed (project default)
│   ├── repository/            # interfaces only
│   ├── service/               # domain policies/services (no application imports)
│   ├── exception/             # transport-agnostic domain errors
│   └── event/                 # plain immutable events (preferred)
├── application/
│   ├── command/ | query/
│   ├── service/               # CommandService / QueryService
│   ├── support/               # policies needing config/clock/audit
│   ├── mapper/ | view/
│   └── port/                  # outbound ports
├── infrastructure/
│   ├── repository/ | persistence/   # Jpa* + *RepositoryAdapter
│   ├── cache/ | event/ | integration/
└── presentation/
    ├── controller/
    ├── request/ | response/ | dto/
    └── mapper/
```

### Persistence model policy

| Approach | When |
|----------|------|
| **JPA-backed domain model** (`domain/model` + `@Entity`) | **Project default** |
| Separate `*Entity` + mapper | Optional — only when persistence shape diverges (complex integrations) |

---

## II. LAYER DEPENDENCY RULES

### Correct directions

```text
presentation  -->  application  -->  domain
infrastructure -->  domain
infrastructure -->  application ports
```

```text
presentation/controller
    → application service (Command/Query)
        → application command/view/port
        → domain model / repository interface / domain service
infrastructure adapter
    → domain repository interface
    → domain model (when JPA-backed)
```

### Forbidden

```text
❌ domain → application
❌ domain → presentation
❌ domain → infrastructure
❌ application → JpaRepository
❌ application → modules/*/infrastructure
❌ presentation → infrastructure
❌ presentation → domain
❌ module A → module B infrastructure
❌ module A application → module B domain.repository (use a port)
```

### Allowed

```text
✅ application → application.support / mapper / command / port
✅ infrastructure.event → application service (async)
✅ presentation → presentation DTO/mapper
✅ application → shared metro.ExoticStamp.infra.* (prefer ports over time)
✅ Spring annotations on application/infrastructure; pragmatic Spring on domain services
✅ JPA annotations on domain models
```

Enforced by `src/test/java/metro/ExoticStamp/ArchitectureBoundaryTest.java`.

---

## III. DOMAIN MODEL / ENTITY GUIDANCE

```java
@Entity
@Table(name = "stations")
public class Station extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MetroStatus status;

    // ✅ state-local behavior / invariant helpers
    public boolean isActive() {
        return status == MetroStatus.ACTIVE;
    }

    // ❌ multi-aggregate workflow — belongs in application CommandService
    // public void collectStampForUser(...) { ... }
}
```

Rules:

- State-local invariants and predicates **may** live on entities / value objects.
- Cross-aggregate rules belong in **domain policies/services**.
- Orchestration and `@Transactional` boundaries belong in **application services**.
- Avoid `@Transient` computed fields when a service/view can derive them.

---

## IV. REPOSITORY PATTERN

### Domain interface

```java
public interface StationRepository {
    Optional<Station> findById(UUID id);
    Station save(Station station);
}
```

### JPA + thin adapter (default when domain model is the entity)

```java
public interface JpaStationRepository extends JpaRepository<Station, UUID> { }

@Component
@RequiredArgsConstructor
public class StationRepositoryAdapter implements StationRepository {
    private final JpaStationRepository jpa;

    @Override
    public Optional<Station> findById(UUID id) {
        return jpa.findById(id); // pass-through OK — no mapper required
    }

    @Override
    public Station save(Station station) {
        return jpa.save(station);
    }
}
```

Mapper entity↔domain is **not** mandatory unless a separate persistence model exists.

---

## V. APPLICATION SERVICES (Command / Query)

```java
@Service
@RequiredArgsConstructor
public class CollectionCommandService {
    private final UserStampRepository userStampRepository; // domain interface
    private final CollectionPolicyService policyService;   // application support OK
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CollectStampResultView collect(CollectStampCommand cmd) {
        policyService.assertCollectAllowed(cmd.userId(), cmd.stationId(), cmd.campaignId());
        UserStamp saved = userStampRepository.save(/* ... */);
        // publish after commit via helper — application concern
        return /* view */;
    }
}

@Service
@Transactional(readOnly = true)
public class CollectionQueryService { /* reads + cache-aside */ }
```

Do **not** inject write CommandService into a read-only QueryService.

---

## VI. DOMAIN / APPLICATION POLICY

- Domain service under `domain/service`: may use domain repositories only — **never** import `application`.
- Policies needing `@ConfigurationProperties`, clocks, or audit side-effects: `application/support`.

```java
@Component
@RequiredArgsConstructor
public class CollectionPolicyService {
    private final UserStampRepository userStampRepository;

    public void assertCollectAllowed(UUID userId, UUID stationId, UUID campaignId) {
        if (userStampRepository.existsByUserIdAndStationIdAndCampaignId(userId, stationId, campaignId)) {
            throw new StampAlreadyCollectedException(stationId);
        }
    }
}
```

---

## VII. CONTROLLER PATTERN

```java
@RestController
@RequestMapping("/api/v1/collection")
@RequiredArgsConstructor
public class CollectionRuntimeController {
    private final CollectionCommandService commandService;
    private final CollectionPresentationMapper mapper;

    @PostMapping("/collect")
    public ResponseEntity<ApiResponse<CollectStampResponse>> collect(
            @Valid @RequestBody CollectStampRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        var result = commandService.collect(mapper.toCommand(request, user.getId()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(mapper.toResponse(result)));
    }
}
```

Presentation depends on **application** + presentation DTOs only.

---

## VIII. EXCEPTIONS (transport-agnostic domain)

Domain exceptions must **not** store `HttpStatus` or other web types.

```java
// domain
public class StampAlreadyCollectedException extends DomainException {
    public StampAlreadyCollectedException(UUID stationId) {
        super("Stamp already collected for station " + stationId);
    }
}

// common
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
```

HTTP status / error-code mapping belongs in `GlobalExceptionHandler` (presentation/common web layer), keyed by exception type.

---

## IX. EVENTS

### Preferred domain event (new code)

```java
public record StampCollectedEvent(
        UUID eventId,
        UUID stampId,
        UUID userId,
        UUID stationId,
        UUID lineId,
        UUID campaignId,
        LocalDateTime collectedAt
) {}
```

### Publishing / listening

- Publish from **application** after successful commit.
- Listen in **infrastructure** (`@EventListener`, `@Async`).
- Spring `ApplicationEvent` inheritance on domain events is **legacy pragmatic** — do not use as the template for new events.

---

## X. CACHING

- Read: miss → source → put.
- Write: persist → evict/invalidate via port or cache adapter from application orchestration.
- TTL from config (`@Value` / `@ConfigurationProperties`), not hardcoded magic numbers in business logic.

---

## XI. CROSS-MODULE RULES

| Allowed | Forbidden |
|---------|-----------|
| A.application → B.application.port | A.* → B.infrastructure |
| A.infrastructure listens to B domain event (ID payload) | A.application → B.domain.repository |
| Shared `common` / `infra` utilities | Circular module infrastructure coupling |

---

## XII. PACKAGE CONVENTION (no mass move)

| Concern | Canonical (new) | Grandfathered |
|---------|-----------------|---------------|
| Application services | `application/service/` | auth/user/rbac/metro may use `application/` root |
| Controllers | `presentation/controller/` | older modules may use `presentation/` root |
| Domain models | `domain/model` + `@Entity` | same |
| Infra repos | `infrastructure/repository` or `persistence` | rbac may be flat under `infrastructure/` |

Incremental rule: new files in a grandfathered module may follow that module’s existing layout; **new modules** follow canonical.

---

## XIII. TRANSACTION BOUNDARIES

- Write methods: `@Transactional`
- Read services: `@Transactional(readOnly = true)` when appropriate
- Domain validation called from command service shares the same transaction
- Prefer after-commit publish for integration events

---

## XIV. TESTING CHECKLIST

- Unit: command/query + domain policy happy/error paths
- ArchUnit: `ArchitectureBoundaryTest` must pass
- Integration: uniqueness / concurrency for collect & reward where relevant
- Do not assert HTTP status inside domain unit tests

---

## XV. QUICK REFERENCE — COMMON MISTAKES

| Mistake | Wrong | Right |
|---------|-------|-------|
| JPA in application | inject `JpaRepository` | inject domain repository / port |
| Domain imports application | domain facade → policy in application | delete facade; call policy from application |
| HttpStatus on domain exception | `ex.httpStatus = CONFLICT` | map in `GlobalExceptionHandler` |
| Mandatory Entity/Domain split | always `StationEntity` | JPA-backed `Station` default |
| Business workflow on entity | `entity.collectEverything()` | CommandService orchestration |
| Foreign module infra | auth → `user.infrastructure.Jpa*` | `UserRepository` / `UserAccountPort` |
| Mandatory `ApplicationEvent` | domain extends Spring event | plain immutable event + app publish |

---

## XVI. CODE REVIEW CHECKLIST

- [ ] No forbidden layer / cross-module infrastructure imports
- [ ] Write/read transaction annotations correct
- [ ] Invariants in entity/policy; orchestration in application
- [ ] Repository interface in domain; adapter in infrastructure
- [ ] DTOs do not expose secrets
- [ ] Cache eviction after writes where applicable
- [ ] Domain exceptions transport-agnostic
- [ ] ArchUnit green
- [ ] OpenAPI annotations clear

---

Done. Prefer this file + ADR-001 over older textbook hexagonal snippets.
