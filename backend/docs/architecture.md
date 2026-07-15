# ARCHITECTURE - EXOTIC STAMP

> Tài liệu kiến trúc kỹ thuật cho backend Exotic/Metro Stamp.
> Tập trung vào các quyết định ổn định theo thời gian, không đi sâu checklist triển khai hằng ngày.

---

## 1. System Overview

- **Product**: Exotic Stamp (Metro Stamp)
- **Objective**: Gamified metro journey bằng NFC/QR stamping, reward milestones, growth loop và monetization platform.
- **Primary domains**:
  - Identity & access: `auth`, `user`, `rbac`
  - Core product: `metro`, `collection`, `reward`
  - Growth & revenue: `community`, `monetization`

---

## 2. Architecture Style

- **Style:** Spring-pragmatic DDD (module-oriented). Formal decision: [`docs/adr/ADR-001-spring-pragmatic-ddd.md`](adr/ADR-001-spring-pragmatic-ddd.md).
- **Service split:**
  - Write path: `{Module}CommandService`
  - Read path: `{Module}QueryService`

### 2.1 Dependency direction (qualified)

```text
presentation  -->  application  -->  domain
infrastructure -->  domain
infrastructure -->  application ports
```

Meaning:

- Controllers call application services only.
- Application orchestrates use cases and depends on domain models/repositories/ports.
- Infrastructure **implements** domain repositories and application ports; it is not a free dependency for presentation/application.
- Do **not** interpret the older shorthand `domain <- infrastructure` as “infrastructure sits under every layer.”

### 2.2 Hard rules

- `domain` must **not** depend on `application`, `presentation`, or `infrastructure`.
- `application` must **not** depend directly on `JpaRepository` or `modules/*/infrastructure`.
- `presentation` must **not** depend on `infrastructure` or `domain` (use application services + DTOs/views).
- Adapters in `infrastructure` are the bridge to persistence/integration.
- Cross-module: application ports or integration events — not another module’s `infrastructure`.
- JPA `@Entity` on `domain/model` is **allowed**. Separate persistence entities are **optional**.
- Boundaries enforced by `ArchitectureBoundaryTest` (ArchUnit). See also `docs/ARCHITECTURE_ALIGNMENT_PLAN.md`.

---

## 3. Codebase Structure

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

### 3.1 Responsibility map

- `config`: security, cache, async, serialization, OpenAPI.
- `common`: base entity, responses, exceptions, shared models.
- `infra`: shared infra components (cache base, mail, redis, queue).
- `modules/*`: business bounded contexts theo layer DDD.

---

## 4. Current Module Status

### 4.1 Implemented business code (source truth)

| Module | Status | API / capability coverage |
|--------|--------|---------------------------|
| `auth` | Implemented | register/login/refresh/verify/forgot/reset/resend, JWT + revocation |
| `user` | Implemented | profile CRUD-style user APIs |
| `rbac` | Implemented | roles, permissions, assignments |
| `metro` | Implemented | public/admin lines & stations, scan resolve, station scan keys, public asset upload |
| `collection` | Implemented | campaigns, stamp designs, collect runtime, stamp book / progress |
| `reward` | Implemented | admin partners/milestones/vouchers, user rewards, async evaluation on `StampCollectedEvent` |
| `community` | Implemented | referral, share events, notifications |
| shared `infra` | Implemented | mail queue, cache base, redis support |

### 4.2 Schema only (no Java business module yet)

| Module | Status |
|--------|--------|
| `monetization` | Flyway `V5__monetization.sql` only; empty `modules/monetization/` |

### 4.3 Current collection → reward event flow

1. `CollectionCommandService` persists `user_stamps` inside `@Transactional`.
2. After commit, publishes `StampCollectedEvent` (in-process).
3. Reward `StampCollectedEventListener` (`@Async`) evaluates milestones and issues rewards/vouchers.
4. Dedup/lock via Redis; uniqueness via DB (`uq_user_rewards_once`, voucher `SKIP LOCKED`).
5. No transactional outbox yet — publish/listener failure can leave stamp without reward (metrics alert).

---

## 5. Data Architecture

## 5.1 Migration strategy

- Schema managed by Flyway (`V1`..`V18` and later).
- Mọi thay đổi schema qua migration mới, không chỉnh tay DB production.

### 5.2 Migration map (baseline + stages)

- `V1`: core identity + RBAC (`users`, roles/permissions)
- `V2`: metro network (`lines`, `stations`)
- `V3`: collection (`campaigns`, `campaign_stations`, `stamp_designs`, `user_stamps`)
- `V4`: reward (`partners`, `milestones`, `rewards`, `voucher_pool`, `user_rewards`)
- `V5`: monetization schema (`advertisements`, `ad_impressions`, `affiliate_banners`, `affiliate_banner_clicks`)
- `V6`: community (`referral_codes`, `referrals`, `share_events`, `notifications`)
- `V7`: mail queue (`mail_jobs`)
- `V8`+: auth token version/seed, collection compliance, integrity, metro constraints, campaigns, collection runtime, rewards, community MVP, station scan keys (`V18`)

### 5.3 Key invariants

- Anti-cheat collect: unique `(user_id, station_id, campaign_id)` with `NULLS NOT DISTINCT`.
- One reward per milestone per user: unique `(user_id, milestone_id)`.
- Referral uniqueness: một referred user chỉ được refer một lần.
- High-volume tracking tables indexed theo thời gian và foreign key logic.

---

## 6. Security Architecture

- Stateless JWT security (Spring Security filter chain).
- Public auth endpoints configured in `SecurityConfig`.
- Role-based authorization with RBAC + `@PreAuthorize` on sensitive endpoints.
- Access/refresh token separation with persistence + redis validation/revocation support.

---

## 7. Caching & State

- Redis used for:
  - token/otp/verify state
  - cache-aside for read-heavy flows
- Core cache policy:
  - read: cache miss -> source -> cache put
  - write: data write -> cache evict/invalidate
- Important TTL (from config):
  - user cache ~30m
  - realtime cache ~70s

---

## 8. Messaging & Async

- Mail delivery via queue-based model (`mail_jobs`) thay vì sync-only send.
- Retry policy + rate limit + stuck job recovery.
- Async processing used for non-blocking side effects (mail/event handling).

---

## 9. Core Runtime Flows

### 9.1 Auth flow

- register/login/refresh/verify/forgot/reset/resend.
- audit log + token lifecycle + redis integration.

### 9.2 Scan-to-stamp flow (implemented)

1. Resolve station by NFC/QR payload (legacy station columns and/or `station_scan_keys`).
2. Validate station/campaign + GPS + anti-cheat / idempotency.
3. Persist `user_stamps`.
4. After commit: publish `StampCollectedEvent` (async listeners).
5. Reward module evaluates milestone and issues reward/voucher if qualified.
6. Monetization tracking remains **target** until the Java module exists.

### 9.3 Reward issue flow (target architecture)

- milestone match -> deduplicate by unique constraint -> voucher allocation -> user reward record -> notification.

### 9.4 Monetization tracking flow (target — schema only)

- ad/banner selection -> impression/click ingest -> batch aggregate counters.
- Not implemented in Java yet; see `docs/ARCHITECTURE_ALIGNMENT_PLAN.md`.

---

## 10. Scalability Considerations

- Hot paths:
  - station lookup by NFC/QR
  - stamp collection writes
  - impression/click ingestion
- Principles:
  - index-first design for hot queries
  - append-heavy table strategy for event logs
  - batch aggregation for denormalized counters
  - partitioning consideration for impression tables when volume grows

---

## 11. Engineering Conventions

- Naming conventions follow `{Entity}Controller`, `{Entity}CommandService`, `Jpa{Entity}Repository`, `{Entity}RepositoryAdapter`, etc.
- Commit convention:
  - `{type}({module}): {description}`
  - types: `feat | fix | refactor | test | chore | docs`
- Review guardrails:
  - giữ đúng dependency direction
  - transaction boundaries đúng layer
  - không expose sensitive fields qua DTO
  - không hardcode rule/TTL quan trọng

---

## 12. ADR-lite Decisions (Do Not Change Casually)

Canonical ADR: [`docs/adr/ADR-001-spring-pragmatic-ddd.md`](adr/ADR-001-spring-pragmatic-ddd.md).

Summary:

- Spring-pragmatic layered module structure (JPA-backed domain models allowed).
- Domain repository interface + infrastructure adapter bridge (pass-through OK).
- UUID-based user references across modules without direct FK to user table in some domain areas.
- Flyway-first schema governance.
- Queue-based outbound mail delivery.
- Collection→reward uses after-commit in-process events (no transactional outbox yet).

Khi cần thay đổi các quyết định này, phải có decision note kèm lý do, impact, và migration plan.

---

## 13. API Contract & Swagger Testing

### 13.1 Swagger ownership

- Swagger/OpenAPI config is centralized at `src/main/java/metro/ExoticStamp/config/OpenApiConfig.java`.
- Current primary tags include:
  - `Auth`, `User`, `RBAC`
  - `Lines`, `Stations` (and scan-key / upload admin APIs)
  - Collection / Reward / Community tags as controllers are registered in OpenAPI

### 13.2 Local testing entry points

- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### 13.3 Standard manual test sequence

1. Call `POST /api/v1/auth/login` to get access token.
2. In Swagger UI, click `Authorize` and set `Bearer <access_token>`.
3. Test secured endpoints in this order:
   - `User` APIs (`/api/v1/users/*`)
   - `RBAC` APIs (`/api/v1/roles/*`, `/api/v1/permissions/*`)
   - `Lines` / `Stations` / scan-key APIs
   - Collection collect / stamp-book APIs
   - Reward / community APIs as needed
4. For refresh flow, call `POST /api/v1/auth/refresh` after login (refresh token comes from cookie).

### 13.4 Definition boundaries

- Swagger serves as the contract source for endpoint path, payload shape, and security requirements.
- `docs/architecture.md` is the stable architecture intent and module boundary document.
- Feature-level rollout checklist and temporary implementation notes should stay in module-specific guides, not this architecture file.
