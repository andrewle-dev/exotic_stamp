# 10 — Architecture Diagram: Exotic Stamp / Metro Stamp

> Status: Draft v0.1  
> Owner: Backend Lead / Tech Lead  
> Purpose: Provide architecture diagrams and structural rules for the MVP backend, mobile integration, admin operations, and future monetization/community modules.

---

## 1. Feasibility Check

The current architecture direction is feasible for MVP and scale-up if the team preserves module boundaries.

The correct shape is a Spring-pragmatic DDD backend with clear module ownership:

```text
presentation -> application -> domain <- infrastructure
```

This is sustainable for Exotic Stamp because the app has distinct bounded contexts:

- identity and access;
- metro station operations;
- collection and anti-cheat;
- reward and voucher fulfillment;
- monetization events;
- community and growth.

The architecture will fail if controllers become business services, application services import JPA repositories directly, or reward/ads logic is mixed into collection without clear transactional boundaries.

---

## 2. System Context Diagram

```mermaid
flowchart TB
    User[Mobile User]
    Admin[Admin Operator]
    Partner[Brand Partner / Ops]

    Mobile[Flutter Mobile App]
    AdminWeb[Admin Web]

    API[Exotic Stamp Backend API\nSpring Boot]
    DB[(PostgreSQL)]
    Redis[(Redis)]
    Storage[(Object Storage\nS3/R2/Local)]
    Mail[SMTP / Mail Queue]
    FCM[Push Notification Provider]
    Ads[Ad Network SDKs\nAdMob/AppLovin/Unity]

    NFC[NFC Tags at Metro Stations]
    QR[Dynamic QR Display / Token]

    User --> Mobile
    Mobile --> NFC
    Mobile --> QR
    Mobile --> API
    Admin --> AdminWeb
    AdminWeb --> API
    Partner --> AdminWeb

    API --> DB
    API --> Redis
    API --> Storage
    API --> Mail
    API --> FCM
    Mobile --> Ads
    API --> Ads
```

---

## 3. Backend Layer Diagram

```mermaid
flowchart LR
    subgraph Presentation
        Controllers[Controllers]
        Requests[Request DTOs]
        Responses[Response DTOs]
    end

    subgraph Application
        CommandServices[Command Services]
        QueryServices[Query Services]
        AppMappers[Application Mappers]
        Ports[Outbound Ports]
    end

    subgraph Domain
        DomainModels[Domain Models / Entities]
        DomainServices[Domain Services]
        DomainRepos[Repository Interfaces]
        DomainEvents[Domain Events]
        DomainExceptions[Domain Exceptions]
    end

    subgraph Infrastructure
        JpaRepos[JPA Repositories]
        Adapters[Repository Adapters]
        Cache[Redis Cache Adapters]
        Integrations[Mail / Storage / Push / External]
    end

    Controllers --> Requests
    Controllers --> CommandServices
    Controllers --> QueryServices
    CommandServices --> DomainModels
    CommandServices --> DomainServices
    CommandServices --> DomainRepos
    CommandServices --> AppMappers
    QueryServices --> DomainRepos
    QueryServices --> AppMappers
    DomainServices --> DomainRepos
    Adapters --> DomainRepos
    Adapters --> JpaRepos
    Cache --> Redis[(Redis)]
    Integrations --> Ports
```

### Hard rule

Infrastructure can implement domain ports. Domain must not know infrastructure exists.

---

## 4. Module Diagram

```mermaid
flowchart TB
    Common[common\nresponses/exceptions/base/pagination]
    Infra[infra\nredis/cache/mail/storage/queue]

    Auth[auth]
    User[user]
    RBAC[rbac]
    Metro[metro]
    Collection[collection]
    Reward[reward]
    Monetization[monetization]
    Community[community]

    User --> Common
    Auth --> User
    Auth --> RBAC
    RBAC --> User
    Metro --> RBAC
    Collection --> Auth
    Collection --> Metro
    Reward --> Collection
    Reward --> User
    Monetization --> Metro
    Monetization --> Collection
    Community --> User
    Community --> Collection
    Community --> Reward

    Auth --> Infra
    User --> Infra
    Metro --> Infra
    Collection --> Infra
    Reward --> Infra
    Monetization --> Infra
    Community --> Infra
```

### Dependency intent

- `auth`, `user`, `rbac` provide identity/security foundation.
- `metro` provides station and scan-key truth.
- `collection` owns stamp issuance and anti-cheat.
- `reward` reacts to collection and owns milestone/voucher rules.
- `monetization` observes user/ad interactions but must not own collection truth.
- `community` handles share/referral/notifications after the core loop works.

---

## 5. Scan-to-Stamp Sequence Diagram

```mermaid
sequenceDiagram
    actor User
    participant Mobile as Flutter App
    participant API as Backend API
    participant Auth as Auth Filter
    participant Metro as Metro Service
    participant Collection as Collection Command Service
    participant DB as PostgreSQL
    participant Redis as Redis
    participant Reward as Reward Service

    User->>Mobile: Tap NFC / scan QR
    Mobile->>API: POST /collection/stamps/collect
    API->>Auth: Validate access token
    Auth-->>API: UserPrincipal
    API->>Collection: collect(command)
    Collection->>Metro: Resolve scan key
    Metro->>DB: Find active station / key
    DB-->>Metro: Station
    Collection->>Collection: Validate GPS + campaign + duplicate
    Collection->>DB: Insert user_stamps
    DB-->>Collection: Created stamp or unique conflict
    Collection->>Redis: Evict stamp book cache
    Collection->>Reward: Evaluate milestone after safe commit / idempotent event
    Reward->>DB: Insert user_rewards if eligible
    API-->>Mobile: Stamp + progress + reward summary
```

---

## 6. Reward Issue Sequence Diagram

```mermaid
sequenceDiagram
    participant Collection as Collection Event
    participant Reward as Reward Command Service
    participant DB as PostgreSQL
    participant Voucher as Voucher Allocator
    participant Notify as Notification Service

    Collection->>Reward: StampCollected(userId, campaignId, stationId)
    Reward->>DB: Count eligible user stamps
    Reward->>DB: Find active milestones
    Reward->>DB: Check existing user_rewards
    alt Reward already issued
        Reward-->>Collection: No-op
    else Reward not issued
        alt Voucher reward
            Reward->>Voucher: Allocate one code atomically
            Voucher->>DB: Lock/update available voucher
            DB-->>Voucher: Voucher code or none
        end
        Reward->>DB: Insert user_rewards
        Reward->>Notify: Create notification
    end
```

---

## 7. Admin Operations Diagram

```mermaid
flowchart TB
    Admin[Admin User]
    AdminWeb[Admin Web]
    Auth[Auth/RBAC]
    MetroAdmin[Metro Admin APIs]
    RewardAdmin[Reward Admin APIs]
    AssetUpload[Public Asset Upload]
    DB[(PostgreSQL)]
    Storage[(Object Storage)]
    Cache[(Redis Cache)]

    Admin --> AdminWeb
    AdminWeb --> Auth
    Auth --> MetroAdmin
    Auth --> RewardAdmin
    Auth --> AssetUpload
    MetroAdmin --> DB
    RewardAdmin --> DB
    AssetUpload --> Storage
    MetroAdmin --> Cache
    RewardAdmin --> Cache
```

### Admin write rule

Every admin write that affects mobile runtime must invalidate the relevant cache:

- station list;
- station detail;
- scan-key lookup;
- campaign config;
- stamp book public metadata;
- reward/milestone config.

---

## 8. Deployment Architecture — MVP

```mermaid
flowchart TB
    Internet[Internet]
    Nginx[Nginx / Reverse Proxy\nHTTPS + rate limiting]
    Backend[Spring Boot API\nDocker container]
    Postgres[(PostgreSQL)]
    Redis[(Redis)]
    Storage[(Object Storage or Mounted Volume)]
    Logs[Logs / Monitoring]

    Internet --> Nginx
    Nginx --> Backend
    Backend --> Postgres
    Backend --> Redis
    Backend --> Storage
    Backend --> Logs
```

### MVP deployment rule

Do not expose the Spring Boot port directly to the public internet. Route through reverse proxy with HTTPS and rate limiting.

---

## 9. Package Structure

```text
src/main/java/metro/ExoticStamp/
├── config/
├── common/
├── infra/
└── modules/
    ├── auth/
    ├── user/
    ├── rbac/
    ├── metro/
    ├── collection/
    ├── reward/
    ├── monetization/
    └── community/
```

Each module should follow:

```text
modules/{module}/
├── domain/
├── application/
├── infrastructure/
└── presentation/
```

---

## 10. Data Integrity Rules by Architecture Layer

| Layer | Responsibility | Must Not Do |
|---|---|---|
| Presentation | Validate request shape, auth principal, call application service | Business rules, JPA access, token parsing manually |
| Application | Orchestrate use case, transaction boundary, cache eviction, event publishing | Direct `JpaRepository` usage, HTTP response shaping |
| Domain | Business rule validation, domain model, repository interface | Infrastructure integration, DTO imports |
| Infrastructure | JPA/Redis/storage/mail adapters | Own business decisions |
| Database | Final invariant enforcement | Replace service/domain validation entirely |

---

## 11. Security Architecture

```text
Client Request
↓
Nginx / Gateway controls
↓
Spring Security Filter Chain
↓
JWT validation
↓
Access token revocation check
↓
User principal resolution
↓
@PreAuthorize permission check
↓
Controller
```

### Security non-negotiables

- No public admin endpoints.
- No sensitive fields in DTO response.
- No JWT fallback secret in code.
- No Swagger exposed in production unless explicitly protected.
- Access token revocation policy must be tested.
- File upload must validate type, size, path, and public/private scope.

---

## 12. Scalability Hot Paths

| Hot Path | Risk | Required Design |
|---|---|---|
| Scan key lookup | High read frequency | Indexed lookup + short TTL cache |
| Stamp collection insert | Write race | Unique constraint + transaction handling |
| Stamp book query | Repeated mobile read | Cache-aside + invalidation on collect/admin change |
| Reward issue | Duplicate async event | Idempotent insert + unique reward constraint |
| Impression ingest | High write volume | Append-only table + batch aggregation |
| Affiliate click ingest | Fraud / volume | Append-only + anti-spam/rate-limit |

---

## 13. Edge Cases / Failure Modes

1. **Controller bypasses application service**  
   Leads to untracked writes, missing cache invalidation, missing audit logs.

2. **Application imports JPA repository directly**  
   Breaks dependency rule and makes module testing brittle.

3. **Reward listener runs before stamp transaction commit**  
   Reward count may not see the latest stamp. Use transaction-aware event handling or idempotent retry.

4. **Redis unavailable during scan**  
   QR token validation and cache may fail. Define fail-open/fail-safe separately for cache versus security tokens.

5. **Public asset upload stores executable content**  
   File upload must restrict MIME, extension, size, and storage path.

6. **Ad impression endpoint becomes a write amplifier**  
   Needs rate limiting, payload validation, and later batch ingestion strategy.

---

## 14. Architecture Acceptance Gate

This document is accepted only when:

- every module follows `presentation -> application -> domain <- infrastructure`;
- write paths are transactionally owned by command services;
- read paths use query services with `readOnly=true` when appropriate;
- infrastructure adapters implement ports/repository interfaces;
- hot paths have DB indexes and cache policy;
- no MVP feature depends on Phase 2 monetization/community modules;
- deployment does not expose backend directly without reverse proxy/security controls.
