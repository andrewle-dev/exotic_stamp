# Environment And Secret Matrix

## Canonical Variable Inventory

Names only; no values are included.

### Backend

- `SPRING_PROFILES_ACTIVE`
- `SERVER_PORT`
- `SERVER_SHUTDOWN`
- `SERVER_SHUTDOWN_TIMEOUT`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_PASSWORD`
- `JWT_SECRET`
- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MAIL_FROM`
- `MAIL_DISPLAY_NAME`
- `MAIL_REPLY_TO`
- `MAIL_LIST_UNSUBSCRIBE`
- `MAIL_LOGO_URL`
- `CORS_ALLOWED_ORIGINS`
- `CORS_ALLOWED_METHODS`
- `CORS_ALLOWED_HEADERS`
- `CORS_ALLOW_CREDENTIALS`
- `CORS_MAX_AGE_SECONDS`
- `RATE_LIMIT_KEY_PEPPER`
- `AUTH_COOKIE_DOMAIN`
- `AUTH_COOKIE_SAME_SITE`
- `FRONTEND_URL`
- `BACKEND_URL`
- `MOBILE_ANDROID_MIN_VERSION`
- `MOBILE_ANDROID_LATEST_VERSION`
- `MOBILE_ANDROID_FORCE_UPDATE`
- `MOBILE_ANDROID_STORE_URL`
- `MOBILE_IOS_MIN_VERSION`
- `MOBILE_IOS_LATEST_VERSION`
- `MOBILE_IOS_FORCE_UPDATE`
- `MOBILE_IOS_STORE_URL`
- `MOBILE_MAINTENANCE_ENABLED`
- `MOBILE_MAINTENANCE_MESSAGE`
- `STORAGE_PROVIDER`
- `STORAGE_PUBLIC_BASE_URL`
- `STORAGE_LOCAL_PATH`
- `STORAGE_LOCAL_URL`
- `STORAGE_FILE_MAX_SIZE_MB`
- `STORAGE_MULTIPART_MAX_FILE_SIZE`
- `STORAGE_MULTIPART_MAX_REQUEST_SIZE`
- `STORAGE_ORPHAN_RETENTION`
- `STORAGE_CLEANUP_BATCH_SIZE`
- `STORAGE_CLEANUP_MAX_DURATION`
- `STORAGE_CLEANUP_DRY_RUN`
- `STORAGE_CLEANUP_CRON`
- `AWS_S3_BUCKET`
- `AWS_REGION`
- `AWS_S3_ENDPOINT`
- `AWS_S3_PATH_STYLE`
- `AWS_S3_CONNECT_TIMEOUT`
- `AWS_S3_API_CALL_TIMEOUT`
- `AWS_S3_API_CALL_ATTEMPT_TIMEOUT`
- `AWS_S3_MAX_RETRIES`
- `AWS_S3_PRESIGN_EXPIRY`
- `ADMIN_SEED_PASSWORD`
- `DEMO_USER_PASSWORD`

### Web Admin

- `VITE_API_BASE_URL`
- `VITE_APP_ENV`
- `VITE_APP_VERSION`
- `VITE_BUILD_DATE`

### Mobile

- `API_HOST`
- `API_PORT`
- `ENABLE_QR_FLOW`
- `USE_MOCK_DATA`

## Source / CI / Staging / Production Matrix

| Variable group | Local source | CI source | Staging source | Production source | Notes |
|---|---|---|---|---|---|
| Backend DB / Redis / JWT | `.env`, `infra/environments/*.env`, compose env, YAML fallbacks | GitHub Actions secrets/vars required | AWS/Lightsail env or compose env file | AWS/Lightsail env or secret store | production must override all unsafe fallbacks |
| Backend mail | YAML + local env | CI optional unless tests require | staging env | production env | current source contains unacceptable defaults |
| Backend CORS / URLs | YAML defaults + local env | CI optional | staging env | production env | Vercel deployment requires explicit allowlist |
| Backend storage / S3 | YAML defaults + local env | CI optional | staging env | production env | prod validator requires S3 |
| Web admin API/env | `.env` / `.env.example` locally | CI build env | Vercel env | Vercel env | no committed Vercel config found |
| Mobile API host/port | dart-define or runtime debug override | mobile CI config | pre-release build config | store build config | no flavor matrix found |

## Required / Secret / Validation Notes

- Secrets:
  - `DB_PASSWORD`
  - `REDIS_PASSWORD`
  - `JWT_SECRET`
  - `MAIL_PASSWORD`
  - `RATE_LIMIT_KEY_PEPPER`
  - S3 credentials if used outside instance role
- Required in prod by code:
  - `DB_URL`
  - `DB_USERNAME`
  - `DB_PASSWORD`
  - `REDIS_HOST`
  - `CORS_ALLOWED_ORIGINS`
  - `FRONTEND_URL`
  - `BACKEND_URL`
  - `JWT_SECRET`
  - `RATE_LIMIT_KEY_PEPPER`
  - `STORAGE_PROVIDER=s3`
  - `AWS_S3_BUCKET`
  - `AWS_REGION`
  - `STORAGE_PUBLIC_BASE_URL`

## Unsafe Fallbacks Confirmed In Source

- `application.yml` contains source defaults for:
  - DB URL/username/password
  - mail username/password
  - JWT secret
  - localhost CORS origins
  - local storage path/base URL
- `application-dev.yml` also contains dev credential-like defaults
- `docker-compose.yml` injects local-only defaults for JWT and admin seed password

## Naming Consistency Notes

- Backend uses Spring-style env names consistently for most runtime values.
- Root `docker-compose.yml` uses `POSTGRES_*` names and maps them into backend `DB_*`.
- Web uses `VITE_*` naming consistently.
- Mobile relies mainly on `API_HOST` / `API_PORT`, not a broader flavor/env matrix.

## Secret Exposure Findings

- Secret-like values are embedded directly in tracked backend YAML files.
- Environment templates and local env docs are tracked in source.
- Root `.env` and `web/.env` exist locally and are ignored, which is correct from a Git perspective.
