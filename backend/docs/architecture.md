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

- **Style**: Spring-pragmatic DDD (module-oriented).
- **Layer rule**:
  - `presentation -> application -> domain <- infrastructure`
- **Service split**:
  - Write path: `{Module}CommandService`
  - Read path: `{Module}QueryService`

### 2.1 Hard rules

- `domain` không import `presentation`/`infrastructure`.
- `application` không phụ thuộc trực tiếp `JpaRepository`.
- Adapters trong `infrastructure` là bridge duy nhất từ domain sang persistence/integration.

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

### 4.1 Implemented business code

- `auth`
- `user`
- `rbac`
- `metro` (public + admin APIs for lines/stations, including scan resolve and media upload)
- shared infra (`mail queue`, `cache base`, `redis support`)

### 4.2 Data model ready via Flyway, business code to be completed

- `collection`
- `reward`
- `monetization`
- `community`

---

## 5. Data Architecture

## 5.1 Migration strategy

- Schema managed by Flyway (`V1..V6`).
- Mọi thay đổi schema qua migration mới, không chỉnh tay DB production.

### 5.2 Migration map

- `V1`: mail queue (`mail_jobs`)
- `V2`: metro network (`lines`, `stations`)
- `V3`: collection (`campaigns`, `campaign_stations`, `stamp_designs`, `user_stamps`)
- `V4`: reward (`partners`, `milestones`, `rewards`, `voucher_pool`, `user_rewards`)
- `V5`: monetization (`advertisements`, `ad_impressions`, `affiliate_banners`, `affiliate_banner_clicks`)
- `V6`: community (`referral_codes`, `referrals`, `share_events`, `notifications`)

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

### 9.2 Scan-to-stamp flow (target architecture)

1. Resolve station by NFC/QR key.
2. Validate station/campaign + anti-cheat constraints.
3. Persist `user_stamps`.
4. Evaluate milestone.
5. Issue reward/voucher if qualified.
6. Track monetization event if ad slot involved.

### 9.3 Reward issue flow (target architecture)

- milestone match -> deduplicate by unique constraint -> voucher allocation -> user reward record -> notification.

### 9.4 Monetization tracking flow (target architecture)

- ad/banner selection -> impression/click ingest -> batch aggregate counters.

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

- DDD pragmatic layered module structure.
- Domain repository interface + infrastructure adapter bridge.
- UUID-based user references across modules without direct FK to user table in some domain areas.
- Flyway-first schema governance.
- Queue-based outbound mail delivery.

Khi cần thay đổi các quyết định này, phải có decision note kèm lý do, impact, và migration plan.

---

## 13. API Contract & Swagger Testing

### 13.1 Swagger ownership

- Swagger/OpenAPI config is centralized at `src/main/java/metro/ExoticStamp/config/OpenApiConfig.java`.
- Current primary tags:
  - `Auth`
  - `User`
  - `RBAC`
  - `Lines`
  - `Stations`

### 13.2 Local testing entry points

- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### 13.3 Standard manual test sequence

1. Call `POST /api/v1/auth/login` to get access token.
2. In Swagger UI, click `Authorize` and set `Bearer <access_token>`.
3. Test secured endpoints in this order:
   - `User` APIs (`/api/v1/users/*`)
   - `RBAC` APIs (`/api/v1/roles/*`, `/api/v1/permissions/*`)
   - `Lines` APIs (`/api/v1/lines/*`)
   - `Stations` APIs (`/api/v1/stations/*`)
4. For refresh flow, call `POST /api/v1/auth/refresh` after login (refresh token comes from cookie).

### 13.4 Definition boundaries

- Swagger serves as the contract source for endpoint path, payload shape, and security requirements.
- `docs/architecture.md` is the stable architecture intent and module boundary document.
- Feature-level rollout checklist and temporary implementation notes should stay in module-specific guides, not this architecture file.
