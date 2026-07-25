# Backend Current State

## Stack

- Language: Java 21
- Build tool: Maven
- Spring Boot parent: `3.3.5`
- JaCoCo: `0.8.12`
- JWT library: `jjwt 0.12.6`
- Springdoc UI: `2.6.0`
- Testcontainers: `1.21.4`
- JTE: `3.1.12`

## Module / Package Shape

Primary backend modules under `backend/src/main/java/metro/ExoticStamp/modules`:

- `auth`
- `collection`
- `community`
- `metro`
- `mobile`
- `rbac`
- `reward`
- `user`
- legacy/other: `exception`, `monetization`

Architecture evidence:

- Separate `application`, `domain`, `infrastructure`, `presentation` layers are used broadly.
- ArchUnit test exists: `ArchitectureBoundaryTest`.

## Profiles And Runtime Config

- Default active profile in `application.yml`: `dev`
- Profile-specific files:
  - `application.yml`
  - `application-dev.yml`
  - `application-prod.yml`
- `prod` startup uses `ProdStartupValidator` fail-fast checks for:
  - JWT secret validity
  - DB/Redis presence
  - non-empty CORS allowlist
  - frontend/backend public URLs
  - rate-limit pepper and Redis backend
  - `STORAGE_PROVIDER=s3`
  - S3 bucket/region/public base URL
  - localhost rejection for prod endpoints

## Security Filter Chain

`SecurityConfig` confirms:

- Stateless session policy
- JWT filter in Spring Security chain
- Cookie-origin filter before auth
- Optional rate-limit filter after JWT filter
- Public endpoints include auth login/register/refresh/password flows, `/uploads/public/**`, and health/info
- Public read APIs:
  - `GET /api/v1/metro/lines/**`
  - `GET /api/v1/metro/stations/**`
  - `GET /api/v1/campaigns/**`
  - `GET /api/v1/partners/**`
  - `GET /api/v1/stations/*/campaigns`
  - `GET /api/v1/mobile/app-config`
  - `POST /api/v1/metro/scan/resolve`
- Swagger/OpenAPI is allowed only when `springdoc.api-docs.enabled=true` and profile is not `prod`

## Auth / JWT / Refresh / OTP / Rate Limit

- Access token in bearer auth header
- Refresh token cookie path: `/api/v1/auth`
- Default cookie settings in base config:
  - `same-site: Lax`
  - `secure: false`
  - `secure-always: false`
- Prod profile forces `secure-always: true`
- OTP config exists for:
  - forgot password
  - email verification
- Rate limiting exists with Redis backend for:
  - login
  - register
  - otp issue
  - otp verify
  - refresh
  - scan resolve
  - collect

## CORS

- Backed by `CorsProperties`
- Default allowlist in source:
  - `http://localhost:3000`
  - `http://localhost:5173`
- Wildcard + credentials is explicitly rejected by code

## Swagger / OpenAPI

- Springdoc UI dependency present
- Non-prod allows Swagger docs
- `application-prod.yml` disables:
  - `springdoc.api-docs.enabled`
  - `springdoc.swagger-ui.enabled`
- Caddy staging example also returns `404` for Swagger paths

## Database / Flyway

- Primary runtime DB target is PostgreSQL
- MySQL driver still present as runtime dependency, but active config is PostgreSQL
- Flyway migrations present through `V23__idempotency_fingerprint_and_reconcile.sql`
- Latest Flyway version in repo: **V23**
- Dev profile has `baseline-on-migrate: true` and `repair-on-migrate: true`
- JPA `ddl-auto: validate` in dev and prod

## Redis Usage And Outage Behavior

- Redis used for auth/session/rate-limit/cache behaviors
- `application.security.token-revocation.fail-open-on-db-error`:
  - base config: `false`
  - dev profile: `true`
- Health readiness group includes `redis`

## Storage

- Config intends dual providers:
  - `local`
  - `s3`
- Current active default: `local`
- Prod requires `s3`
- Public upload endpoint exists: `/api/v1/admin/uploads/public`
- File validation config includes max size, dimensions, pixels, and allowed types
- Stored-asset lifecycle evidence:
  - `V21__stored_assets.sql`
  - `infra.storage.asset` package
  - orphan cleanup job tests

## S3 Status

- S3 settings exist in config
- S3 SDK dependencies are commented out in `pom.xml`
- S3 implementation classes are checked in as `*.disabled`
- This means production intent exists, but active compiled S3 path is incomplete in the current source snapshot

## Reward / Voucher / Idempotency / Reconcile

- Migrations `V22` and `V23` add reward, voucher, integrity, idempotency, and reconcile support
- Reward reconcile API exists at `POST /api/v1/admin/rewards/reconcile`
- Collection idempotency window configured at `1h`
- Multiple reward concurrency / reconcile integration tests exist

## Health / Liveness / Readiness

- Actuator exposure: `health,info`
- `liveness` group: `livenessState`
- `readiness` group: `readinessState,db,redis`
- Prod health group `storage` intends `s3Storage`
- Docker healthcheck uses `/actuator/health/readiness`

## Container / Compose / Proxy

- `backend/Dockerfile` is multi-stage and production-oriented
- Root `docker-compose.yml` is local/dev oriented
- `backend/docker-compose.prod-like.yml` is a prod-like stack with Postgres, Redis, backend, Caddy, optional LocalStack
- Caddy examples exist:
  - `backend/infra/caddy/Caddyfile.example`
  - `backend/infra/caddy/Caddyfile.local.example`
  - `backend/infra/caddy/Caddyfile.staging.example`

## CI / Tests

- GitHub Actions workflow exists at `backend/.github/workflows/backend-ci.yml`
- Surefire excludes `*IT`
- Failsafe runs `*IT`
- CI profile applies JaCoCo gates:
  - bundle line >= `0.60`
  - bundle branch >= `0.50`
  - auth/collection/reward package gates at `0.70` line / `0.60` branch

## Admin API Controllers

Admin-facing controllers confirmed:

- `CampaignAdminController`
- `StampDesignAdminController`
- `AdminMetroLineController`
- `AdminMetroStationController`
- `AdminStationScanKeyController`
- `AdminPublicUploadController`
- `AdminRewardController`
- `AdminRewardMilestoneController`
- `AdminRewardVoucherController`
- `AdminPartnerController`
- `CollectionAdminController`
- `RoleController`
- `PermissionController`
- `UserController` (admin endpoints mixed with self-service endpoints)

## Backend Summary

- Admin backend coverage is broad enough for a staged admin release.
- The biggest production gaps are governance and deployment closure, not missing core admin CRUD endpoints.
