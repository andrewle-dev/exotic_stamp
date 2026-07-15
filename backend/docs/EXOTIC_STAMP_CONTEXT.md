# EXOTIC_STAMP_CONTEXT

> Tài liệu ngữ cảnh tổng hợp cho dự án Exotic Stamp (Metro Stamp).
---

## 1. Tổng quan dự án

- **Tên hệ thống**: Exotic Stamp (Metro Stamp)
- **Mục tiêu sản phẩm**: Biến mỗi chuyến đi Metro thành hành trình sưu tập dấu kỹ thuật số, tăng gắn kết người dùng, tạo vòng lặp viral và mở rộng doanh thu từ partner/ads.
- **Bối cảnh nghiệp vụ**:
  - Người dùng check-in tại ga bằng **NFC/QR**.
  - Hệ thống xác minh vị trí (GPS) và cấp e-stamp.
  - Người dùng hoàn thành bộ sưu tập theo tuyến/campaign để nhận reward.
  - Nền tảng tích hợp quảng cáo, affiliate, brand partnership, referral/community.

---

## 2. Product scope

### 2.1 Tính năng MVP cốt lõi

- Thu thập dấu bằng NFC / QR.
- Stamp Book theo tuyến ga (tiến độ đã thu thập/chưa thu thập).
- Danh sách/chi tiết nhà ga.
- Hệ thống mốc thưởng (milestone) và phần thưởng từ đối tác.
- Chia sẻ mạng xã hội (kích hoạt viral growth).

### 2.2 Hướng mở rộng sau MVP

- Swiper affiliate banner.
- In-app advertising platform (pre-stamp/full-screen/sponsored stamp).
- Referral program.
- Seasonal / event campaigns.

### 2.3 Luồng doanh thu mục tiêu

- Banner quảng cáo trước khi nhận dấu (CPM/CPC).
- Affiliate banner (flat fee / revenue share).
- Brand partnership cho reward/voucher.
- In-app advertising (CPI/CPD/sponsored placement).
- Seasonal campaign theo sự kiện.

---

## 3. Tech stack hiện tại (codebase thật)

- Java 21
- Spring Boot 3.3.5
- Spring Security + JWT (jjwt 0.12.6)
- Spring Data JPA + Flyway
- PostgreSQL
- Redis (cache/session/token)
- Spring Mail (SMTP Gmail)
- OpenAPI/Swagger (`springdoc-openapi-starter-webmvc-ui`)
- Maven

---

## 4. Trạng thái module hiện tại

### 4.1 Đã có code nghiệp vụ rõ ràng

- `auth`
- `user`
- `rbac`
- `metro` (lines/stations, scan resolve, station scan keys, upload)
- `collection` (campaigns, designs, collect runtime)
- `reward` (milestones, vouchers, async evaluation)
- `community` (referral, share, notifications)
- các thành phần `infra` (mail queue, cache nền tảng)

### 4.2 Đã có schema DB (Flyway) nhưng module business chưa hoàn thiện code

- `monetization` (`V5__monetization.sql` only; empty Java package)

Alignment details: `docs/ARCHITECTURE_ALIGNMENT_PLAN.md`.

---

## 5. Database migration map

- `V1__core_identity_rbac.sql`: users + RBAC.
- `V2__metro.sql`: `lines`, `stations` (NFC/QR hot-path indexes).
- `V3__collection.sql`: `campaigns`, `campaign_stations`, `stamp_designs`, `user_stamps`.
- `V4__reward.sql`: `partners`, `milestones`, `rewards`, `voucher_pool`, `user_rewards`.
- `V5__monetization.sql`: `advertisements`, `ad_impressions`, `affiliate_banners`, `affiliate_banner_clicks`.
- `V6__community.sql`: `referral_codes`, `referrals`, `share_events`, `notifications`.
- `V7__mail_jobs.sql`: hàng đợi gửi mail bất đồng bộ + retry.
- `V8`–`V18`: auth seed/token version, collection compliance, integrity, metro constraints, campaigns, collection runtime, rewards, community MVP, `station_scan_keys`.

---

## 6. Architecture Pattern

- Pattern chính: **Spring-pragmatic DDD**.
- Dependency direction (không vi phạm):
  - `presentation -> application -> domain <- infrastructure`
- Quy tắc:
  - `domain` không import `presentation`, `infrastructure`, hoặc `application`.
  - `application` không dùng trực tiếp `JpaRepository` hoặc `modules/*/infrastructure`.
  - JPA `@Entity` trên `domain/model` được phép; tách persistence entity là optional.
  - `infrastructure` là cánh nối kỹ thuật (JPA, Redis, JWT, Mail, queue, external integration).
  - Cross-module qua ports / events.
- CQS/CQRS-lite:
  - Write: `{Module}CommandService`
  - Read: `{Module}QueryService` (`readOnly=true` khi phù hợp)
- ArchUnit: `ArchitectureBoundaryTest` must stay in sync with these rules.
---

## 7.1 Package Structure

```text
src/main/java/metro/ExoticStamp/
├── ExoticStampApplication.java
├── config/
│   ├── AppConfig.java
│   ├── AsyncConfig.java
│   ├── CacheConfig.java
│   ├── CacheProperties.java
│   ├── DatabaseConfig.java
│   ├── JacksonConfig.java
│   ├── OpenApiConfig.java
│   ├── SecurityConfig.java
│   └── WebConfig.java
├── common/
│   ├── entity/
│   ├── exceptions/
│   ├── model/
│   ├── response/
│   └── utils/
├── infra/
│   ├── cache/
│   ├── mail/
│   │   ├── queue/
│   │   └── template/
│   ├── redis/
│   └── ...
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

## 7.2 Overview Package Structure

> Removed obsolete copy-paste from another codebase (`fbnetwork/metricsX`).  
> Canonical tree is §7.1 (`metro.ExoticStamp`). Shared infra lives under `infra/`, not a root `infrastructure/` package.

Grandfathered vs canonical layouts:

| Concern | Canonical (new modules) | Grandfathered |
|---------|-------------------------|---------------|
| Application services | `application/service/` | auth/user/rbac/metro may use `application/` root |
| Controllers | `presentation/controller/` | older modules may use `presentation/` root |
| Domain models | `domain/model` with `@Entity` | same |
| Monetization | schema only until product prioritizes | empty `modules/monetization/` |

---

## 8. Module: user

- Mô hình: `User`, `UserStatus`.
- Layer chính:
  - `application`: `UserCommandService`, `UserQueryService`, commands/queries/mapper/port.
  - `domain`: `UserRepository`, `UserDomainService`, exceptions, events.
  - `infrastructure`: `JpaUserRepository`, `UserRepositoryAdapter`, `UserCacheRepository`, messaging.
  - `presentation`: `UserController`, request/response DTO.
- Rule quan trọng:
  - Validate unique email/username/phone tại domain service.
  - Sau update/delete phải `cache evict`.

---

## 9. Module: auth

- Trách nhiệm:
  - login/register/verify email/forgot password/reset password/refresh/logout.
- Các mảng chính:
  - JWT provider + auth filter.
  - Access token persistence + refresh token redis.
  - OTP + verify token repository.
  - Audit log.
  - Mail queue + template (verify/otp).
- Endpoint public theo `SecurityConfig`:
  - `/api/v1/auth/login`
  - `/api/v1/auth/register`
  - `/api/v1/auth/refresh`
  - `/api/v1/auth/verify-email`
  - `/api/v1/auth/forgot-password`
  - `/api/v1/auth/resend-otp`
  - `/api/v1/auth/reset-password`
  - `/api/v1/auth/resend-verification`

---

## 10. Module: rbac

- Mô hình: `Role`, `Permission`, `UserRole`, `RolePermission`, enums `RoleName`, `PermissionName`.
- Dòng chính:
  - `RoleCommandService`: assign/revoke role.
  - `RoleQueryService`: query role/permission theo user.
  - `RoleController` dùng `@PreAuthorize` cho endpoint nhạy cảm.
- Repository pattern:
  - Domain interface trong `domain/repository`.
  - `Jpa...Repository` + `...Adapter` trong `infrastructure`.

---

## 11. Shared Infrastructure

- `infra/cache/BaseCacheRepository<T>`:
  - Generic cache-aside cho các module.
- `infra/mail`:
  - `MailService`, `MailSenderPort`, SMTP adapter.
  - Queue subsystem: `mail_jobs`, processor/worker/retry/rate-limit.
- `infra/redis`:
  - Verify token storage, refresh token, OTP support.
- `common`:
  - `ApiResponse`, `ErrorResponse`, `GlobalExceptionHandler`, `BaseEntity`, pagination models.

---

## 12. Naming Conventions

- Controller: `{Entity}Controller`
- Command service: `{Entity}CommandService`
- Query service: `{Entity}QueryService`
- Domain repository: `{Entity}Repository`
- JPA repository: `Jpa{Entity}Repository`
- Adapter: `{Entity}RepositoryAdapter`
- Command object: `{Action}{Entity}Command`
- Query object: `Get{Entity}Query` / `Search{Entity}Query`
- Request DTO: `{Action}{Entity}Request`
- Response DTO: `{Entity}Response`
- Mapper: `{Entity}AppMapper`
- Exception: `{Entity}NotFoundException`, `{Field}AlreadyTakenException`

Quy ước biến:
- Dùng tên rõ ngữ nghĩa (`userResponse`, `salesTransactions`, `deviceFingerprint`).
- Tránh tên chung chung (`data`, `temp`, `res`).

---

## 13. Git Commit Convention

Format:
`{type}({module}): {description}`

Loại commit:
- `feat`
- `fix`
- `refactor`
- `test`
- `chore`
- `docs`

Ví dụ:
- `feat(collection): add stamp collect command flow`
- `fix(auth): handle invalid refresh token reuse`
- `refactor(reward): extract voucher allocation service`

---

## 14. AI Review Guidelines

Khi AI review/generate code cho Exotic Stamp, luôn check:

- Layer dependency có đúng chiều không.
- Service write có `@Transactional`.
- Service read có `readOnly=true` (khi phù hợp).
- Write flow có cache evict/invalidate.
- DTO không lộ dữ liệu nhạy cảm.
- Validation đủ 3 tầng:
  - Request DTO (`@Valid`)
  - Domain validation (entity/domain service)
  - DB constraint (unique/check/fk)
- Không hardcode TTL/rule quan trọng.
- Các hot-path (`scan`, `reward issue`, `ad tracking`) phải có index và fallback strategy.
