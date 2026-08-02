# Exotic Stamp Backend — Environment Matrix (v2)

**Audit version:** 2  
**Revised:** 2026-07-23  
**Companion:** `BACKEND_PRODUCTION_READINESS_AUDIT.md` v2  

**Rules:** Never commit real secrets. **One canonical variable name per property** (no `A / B` aliases). Placeholders only.

---

## How `SPRING_PROFILES_ACTIVE` is selected

| Environment | Selection | Notes |
|-------------|-----------|-------|
| Local IDE | `dev` via process env / run config | Do not commit into `application.yml` |
| Docker Compose `backend` (`full`) | Compose sets `SPRING_PROFILES_ACTIVE=dev` | Local only |
| Tests | Test properties + Testcontainers | Prefer explicit `test` profile later |
| Lightsail production | `prod` via process/systemd/container env only | Never bake into JAR |

---

## Profile files (current names — not renamed in audit)

| File | Role |
|------|------|
| `application.yml` | Shared bindings; no secret defaults |
| `application-dev.yml` | Dev conveniences — **currently contains secret defaults (F-001)** |
| `application-prod.yml` | Prod overlays — secret-free |
| `src/test/resources/application.properties` | Test fixtures |

---

## JWT secret strength (canonical)

| Requirement | Definition |
|-------------|------------|
| Minimum entropy | **At least 32 cryptographically random bytes** |
| Encoding | Those bytes **Base64-encoded** (standard or URL-safe) in `JWT_SECRET` |
| Rejection | Reject decoded material &lt; 32 bytes or malformed Base64; **do not** SHA-256-pad short secrets (F-014) — **enforced in Batch C** |
| Generate | `openssl rand -base64 32` |

## Rate-limit pepper (Batch C)

| Variable | Requirement |
|----------|-------------|
| `RATE_LIMIT_KEY_PEPPER` | Dedicated HMAC pepper for composite rate-limit keys; **required in prod**; independent of `JWT_SECRET`; never log |

Example generation (illustrative, not a real secret):  
`openssl rand -base64 32` → store output as `JWT_SECRET`.

---

## DB SSL (topology-dependent)

| Topology | `DB_SSL_MODE` expectation |
|----------|---------------------------|
| App and PostgreSQL on same private Lightsail/VPC network with firewall | May use `prefer` or `require` per ops policy |
| Database reachable across untrusted network / managed PG with forced TLS | **`require`** or stricter (`verify-full` when CA available) |
| Local Docker bridge | Often disabled / not required |

Embed SSL mode in `DB_URL` **or** bind `DB_SSL_MODE` into JDBC URL construction — pick one mechanism; canonical env name below is `DB_SSL_MODE`.

---

## S3 isolation

| Environment | Canonical bucket variable | Isolation rule |
|-------------|---------------------------|----------------|
| Dev | `AWS_S3_BUCKET` with value dedicated to dev | Separate bucket **or** strict IAM prefix isolation |
| Test | Same variable name, test-specific value in CI/test env | Never share prod bucket |
| Prod | Same variable name, prod-specific value | Separate bucket strongly preferred |

Do **not** reuse one bucket across env without IAM prefix isolation + distinct credentials.

---

## Canonical ENV matrix

Legend: **R** required, **O** optional, **—** unused, **S** secret, **N** non-secret.

| Variable | Dev | Test | Prod | S/N | Example placeholder | Owning configuration | Missing behavior | Source |
|----------|-----|------|------|-----|---------------------|----------------------|------------------|--------|
| `SPRING_PROFILES_ACTIVE` | R | O | R | N | `prod` | Spring Environment | Wrong profile → wrong config | Orchestrator |
| `SERVER_PORT` | O | O | O | N | `8080` | `server.port` | Defaults 8080 | Env |
| `APP_ENV` | O | — | O | N | `production` | Not bound today | N/A until added | Ops tag |
| `BACKEND_URL` | O | — | R | N | `https://api.example.com` | `application.backend.current` | Bad links/cookies | Lightsail env |
| `FRONTEND_URL` | O | — | R | N | `https://admin.example.com` | `application.frontend.current` | CORS/cookie mismatch | Lightsail env |
| `MOBILE_DEEP_LINK_BASE_URL` | O | — | O | N | `exoticstamp://` | Not first-class today | Client-side only until bound | Future property |
| `DB_URL` | R | R | R | N | `jdbc:postgresql://db:5432/exotic_stamp` | `spring.datasource.url` | Startup failure | Secret store + env |
| `DB_USERNAME` | R | R | R | S/N | `exotic_app` | `spring.datasource.username` | Startup failure | Secret store |
| `DB_PASSWORD` | R | R | R | S | `replace-me` | `spring.datasource.password` | Startup failure | Secret store |
| `DB_SSL_MODE` | O | O | topology | N | `require` | JDBC URL / future binder | Weak TLS if mis-set | Ops decision |
| `DB_MAX_POOL_SIZE` | O | O | R | N | `10` | Hikari (not bound yet) | Defaults | Introduced via Hikari props |
| `DB_MIN_IDLE` | O | O | O | N | `2` | Hikari | Defaults | Env |
| `DB_CONNECTION_TIMEOUT_MS` | O | O | O | N | `3000` | Hikari | Defaults | Env |
| `REDIS_HOST` | R | R | R | N | `127.0.0.1` | `spring.data.redis.host` | Connect fail; controls degrade | Env |
| `REDIS_PORT` | O | O | R | N | `6379` | `spring.data.redis.port` | Default 6379 | Env |
| `REDIS_PASSWORD` | R | O | R | S | `replace-me` | `spring.data.redis.password` | Empty allowed in base yml | Secret store |
| `REDIS_SSL` | — | — | O | N | `true` | Not first-class | Needed for managed Redis TLS | Future |
| `REDIS_DATABASE` | O | O | O | N | `0` | Spring Data Redis | Index collision | Env |
| `REDIS_TIMEOUT_MS` | O | O | O | N | `3000` | yml `timeout: 3s` | Slow fail | Config |
| `JWT_SECRET` | R | R | R | S | Base64 of ≥32 random bytes | `JwtProperties.secret` | Dev unsafe default today | Secret store |
| `JWT_ACCESS_TTL` | O | O | O | N | `15m` | `jwt.access-token-ttl` | yml default | Config |
| `JWT_REFRESH_TTL` | O | O | O | N | `7d` | `jwt.refresh-token-ttl` | yml default | Config |
| `JWT_ISSUER` | O | O | R | N | `exotic-stamp` | `JwtProperties.issuer` | Today `metricsX`; not verified on parse | Config |
| `JWT_AUDIENCE` | — | — | O | N | `exotic-stamp-api` | Not implemented | N/A | Future |
| `AUTH_COOKIE_SECURE_ALWAYS` | O | O | R | N | `true` | `application.auth.cookie.secure-always` | Prod already true in yml | Profile/env |
| `AUTH_COOKIE_SAME_SITE` | O | O | R | N | `Lax` or `None` | `application.auth.cookie.same-site` | Vercel cross-site may need `None` | Env |
| `AUTH_COOKIE_DOMAIN` | O | — | O | N | `.example.com` | `application.auth.cookie.domain` | Host-only if empty | Env |
| `AUTH_COOKIE_PATH` | O | O | O | N | `/api/v1/auth` | cookie path | OK default | Config |
| `AUTH_COOKIE_NAME` | O | O | O | N | `refresh_token` | cookie name | OK default | Config |
| `CORS_ALLOWED_ORIGINS` | O | O | R | N | `https://admin.example.com` | `CorsProperties` | Prod must set; no `*` with credentials | Env |
| `CORS_ALLOWED_METHODS` | O | O | O | N | `GET,POST,PUT,PATCH,DELETE,OPTIONS,HEAD` | CorsProperties | Defaults | Env |
| `CORS_ALLOWED_HEADERS` | O | O | O | N | `*` | CorsProperties | Defaults | Env |
| `CORS_ALLOW_CREDENTIALS` | O | O | O | N | `true` | CorsProperties | Must not pair with wildcard origin | Env |
| `CORS_MAX_AGE_SECONDS` | O | O | O | N | `3600` | CorsProperties | Defaults | Env |
| `MAIL_HOST` | O | — | R | N | `smtp.example.com` | `spring.mail.host` | Base hardcodes Gmail today | Env |
| `MAIL_PORT` | O | — | R | N | `587` | `spring.mail.port` | Default 587 | Env |
| `MAIL_USERNAME` | R | R | R | S/N | `noreply@example.com` | `spring.mail.username` | Dev default present | Env |
| `MAIL_PASSWORD` | R | R | R | S | `replace-me` | `spring.mail.password` | Dev unsafe default | Secret store |
| `MAIL_FROM` | R | R | R | N | `noreply@example.com` | `application.mail.from` | Bad From | Env |
| `MAIL_DISPLAY_NAME` | O | O | O | N | `Exotic Travelers` | mail props | Default OK | Env |
| `MAIL_REPLY_TO` | O | O | O | N | `support@example.com` | mail props | Optional | Env |
| `MAIL_LIST_UNSUBSCRIBE` | O | — | O | N | `<mailto:unsub@example.com>` | mail props | Optional | Env |
| `MAIL_LOGO_URL` | O | O | R | N | `https://cdn.example.com/logo.png` | `application.mail.logo-url` | Prefer S3/CDN object | CDN URL |
| `AWS_REGION` | O | O | R | N | `ap-southeast-1` | `storage.s3.region` | Required when `STORAGE_PROVIDER=s3` | Env |
| `AWS_S3_BUCKET` | O | O | R | N | `exotic-stamp-prod` | `storage.s3.bucket` | Env-specific bucket | Env |
| `STORAGE_PUBLIC_BASE_URL` | O | O | R | N | `https://cdn.example.com` | `storage.public-base-url` | Public URL = base + `/` + object_key | Env |
| `AWS_S3_ENDPOINT` | O | O | O | N | LocalStack URL | `storage.s3.endpoint` | Dev/test emulation only | Env |
| `AWS_S3_PATH_STYLE` | O | O | O | N | `false` | `storage.s3.path-style-access` | LocalStack may need true | Env |
| `STORAGE_PROVIDER` | O | O | R | N | `s3` | `StorageProperties.provider` | Prod rejects `local`; default `local` for dev | Env |
| `AWS_ACCESS_KEY_ID` | O | O | O | S | (only if static keys chosen) | DefaultCredentialsProvider chain | Prefer Lightsail role / temp creds | **Validate for Lightsail** |
| `AWS_SECRET_ACCESS_KEY` | O | O | O | S | (only if static keys chosen) | DefaultCredentialsProvider chain | Prefer non-static if supported | **Validate for Lightsail** |
| `STORAGE_LOCAL_PATH` | R if local | R if local | avoid | N | `/var/app/uploads` | `storage.local.base-path` | Dev/test only; prod forbids local provider | Env |
| `STORAGE_LOCAL_URL` | O | O | avoid | N | `http://localhost:8080/uploads` | `storage.local.base-url` | Legacy local only | Env |
| `STORAGE_FILE_MAX_SIZE_MB` | O | O | O | N | `5` | `storage.file.max-size-mb` | Default 5 | Env |
| `STORAGE_MULTIPART_MAX_FILE_SIZE` | O | O | O | N | `5MB` | multipart | Default | Env |
| `STORAGE_MULTIPART_MAX_REQUEST_SIZE` | O | O | O | N | `6MB` | multipart | Default | Env |
| `STORAGE_ALLOWED_MIME_TYPES` | O | O | O | N | Bound via yml list today | `storage.file.allowed-types` | Default jpeg/png/webp | Config/env list |
| `ADMIN_SEED_PASSWORD` | R for seed | — | — | S | `changeme-dev-only` | `@Profile("dev")` bootstrap | Dev only | Local env |
| `DEMO_USER_PASSWORD` | O | — | — | S | `changeme-demo-only` | `@Profile("dev")` | Dev only | Local env |
| `APPLICATION_SECURITY_TOKEN_REVOCATION_FAIL_OPEN_ON_DB_ERROR` | O | O | must be false | N | `false` | `TokenRevocationProperties` | Dev overrides true | Prod fail-closed |
| `MOBILE_ANDROID_MIN_VERSION` | O | O | O | N | `0.1.0` | `MobileAppConfigProperties` | Defaults | Env |
| `MOBILE_ANDROID_LATEST_VERSION` | O | O | O | N | `0.1.0` | mobile props | Defaults | Env |
| `MOBILE_ANDROID_FORCE_UPDATE` | O | O | O | N | `false` | mobile props | Defaults | Env |
| `MOBILE_ANDROID_STORE_URL` | O | — | R before store | N | Play Store URL | mobile props | Empty until launch | Env |
| `MOBILE_IOS_MIN_VERSION` | O | O | O | N | `0.1.0` | mobile props | Defaults | Env |
| `MOBILE_IOS_LATEST_VERSION` | O | O | O | N | `0.1.0` | mobile props | Defaults | Env |
| `MOBILE_IOS_FORCE_UPDATE` | O | O | O | N | `false` | mobile props | Defaults | Env |
| `MOBILE_IOS_STORE_URL` | O | — | R before store | N | App Store URL | mobile props | Empty until launch | Env |
| `MOBILE_MAINTENANCE_ENABLED` | O | O | O | N | `false` | mobile props | Defaults | Env |
| `MOBILE_MAINTENANCE_MESSAGE` | O | O | O | N | `Under maintenance` | mobile props | Optional | Env |

### Retired alias listing (v1 → v2)

Do not document dual names. Mapping for operators migrating docs:

| v1 ambiguous label | Canonical v2 name |
|--------------------|-------------------|
| `APP_BASE_URL` | `BACKEND_URL` |
| `FRONTEND_BASE_URL` | `FRONTEND_URL` |
| `COOKIE_SECURE` | `AUTH_COOKIE_SECURE_ALWAYS` |
| `COOKIE_SAME_SITE` | `AUTH_COOKIE_SAME_SITE` |
| `COOKIE_DOMAIN` | `AUTH_COOKIE_DOMAIN` |
| `COOKIE_PATH` | `AUTH_COOKIE_PATH` |
| `UPLOAD_MAX_FILE_SIZE` | `STORAGE_FILE_MAX_SIZE_MB` / multipart vars |
| `UPLOAD_ALLOWED_MIME_TYPES` | `STORAGE_ALLOWED_MIME_TYPES` |
| `AWS_S3_PUBLIC_BASE_URL` / `CDN_BASE_URL` | `STORAGE_PUBLIC_BASE_URL` |
| `DB_HOST`+`DB_PORT`+`DB_NAME` | use `DB_URL` |

---

## Lightsail + Amazon S3 credentials (decision required)

| Option | Notes |
|--------|-------|
| Static IAM user access keys in server secret store | Common on Lightsail VMs; rotate; least privilege |
| Lightsail Containers / other AWS identity integration | May differ from EC2 instance profiles — **validate docs for your Lightsail product** |
| EC2-style instance profile | **Do not assume available** on Lightsail |

**Action:** Confirm the supported credential mechanism for the chosen Lightsail SKU before implementing S3. Document the chosen mechanism in ops runbooks.

---

## Prod fail-fast checklist (recommended)

Fail startup when `prod` and any of:

- `JWT_SECRET` missing or &lt; 32 decoded bytes  
- `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` missing  
- `REDIS_HOST` missing  
- `CORS_ALLOWED_ORIGINS` empty or `*` with credentials  
- `FRONTEND_URL` / `BACKEND_URL` missing  
- ~~`STORAGE_PROVIDER=s3` while client unimplemented~~ **Resolved Batch D**
- ~~`STORAGE_PROVIDER=local` without explicit single-node acceptance flag~~ **Resolved Batch D** (prod rejects local)
- `STORAGE_PUBLIC_BASE_URL` missing when serving public objects from S3/CDN  


---

## Batch F.0 delivery note

Spring Boot does **not** auto-load `.env` files. Staging/production injection paths:

| Mechanism | File / source |
|-----------|----------------|
| Docker Compose | `env_file: .env.prod-like` / `.env.staging` (gitignored) |
| systemd | `EnvironmentFile=/etc/exotic-stamp/staging.env` |
| Process env | Lightsail container env / secret inject |

See `STAGING_ENVIRONMENT_CHECKLIST.md` and `SECRET_ROTATION_AND_DELIVERY_RUNBOOK.md`.
Ops scripts also use `DB_HOST` / `DB_PORT` / `DB_NAME` / `APP_ENV` (not all are Spring-bound).
