# Backend Deployment Checklist — Exotic Stamp MVP

> Last updated: 2026-06-25 (M10)  
> Scope: Spring Boot API (`backend/`), profiles `dev` / `prod`

---

## 1. Environment separation

| Profile | File | Purpose |
|---------|------|---------|
| **dev** | `application-dev.yml` | Local/docker demo; Flyway repair; demo seed; verbose logging; Swagger enabled |
| **prod** | `application-prod.yml` | Production; Swagger disabled; no default DB/mail secrets |
| **base** | `application.yml` | Shared defaults; all secrets via env vars |

**Activate:** `SPRING_PROFILES_ACTIVE=prod` (never `dev` in production).

**Staging:** No dedicated `application-staging.yml` today. Use `prod` profile with staging env vars, or add a `staging` profile in a future release.

Reference: `backend/.env.example`

---

## 2. Required environment variables (production)

| Variable | Required | Notes |
|----------|----------|-------|
| `SPRING_PROFILES_ACTIVE` | yes | `prod` |
| `JWT_SECRET` | yes | ≥256 bits; **no default in prod** (`${JWT_SECRET}` only) |
| `DB_URL` | yes | JDBC PostgreSQL URL |
| `DB_USERNAME` | yes | |
| `DB_PASSWORD` | yes | |
| `REDIS_HOST` | yes | Auth OTP, refresh tokens, revocation cache |
| `REDIS_PORT` | yes | Default `6379` |
| `MAIL_USERNAME` | yes* | *Required if OTP/forgot-password enabled |
| `MAIL_PASSWORD` | yes* | |
| `MAIL_FROM` | yes* | |
| `CORS_ALLOWED_ORIGINS` | yes | Comma-separated; **no localhost in prod** |
| `FRONTEND_URL` | yes | Used in mail links |
| `BACKEND_URL` | yes | Public API base |
| `STORAGE_LOCAL_PATH` | if local storage | Default `./uploads` |
| `STORAGE_LOCAL_URL` | if local storage | Public URL prefix, e.g. `https://api.example.com/uploads` |

Optional overrides: see `application.yml` (`application.auth.*`, `collection.*`, `reward.*`).

---

## 3. Secrets audit (M10)

| Check | Status | Notes |
|-------|--------|-------|
| JWT not hardcoded in prod | **PASS** | `jwt.secret: ${JWT_SECRET}` in all profiles |
| DB password not in prod yml | **PASS** | `${DB_PASSWORD}` only in prod |
| `.env.example` has placeholders only | **PASS** | `backend/.env.example` |
| Demo seed only on `dev` | **PASS** | `@Profile("dev")` on `MvpDemoSeedBootstrap`, `AdminSeedBootstrap` |
| Swagger disabled in prod | **PASS** | `springdoc.api-docs.enabled=false`; `ProdSwaggerDisabledTest` |
| CORS restrictive in prod | **PASS** | `CORS_ALLOWED_ORIGINS` required; no dev defaults |
| Dev mail default in yml | **WARN** | `application-dev.yml` has dev mail default — acceptable for local only; do not copy to prod |
| Docker compose dev passwords | **WARN** | `docker-compose.yml` uses `f123` for local stack only |

**Never commit:** `.env`, real `JWT_SECRET`, production DB/mail credentials.

---

## 4. Database migration

```bash
cd backend
export SPRING_PROFILES_ACTIVE=prod
# Ensure DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET, REDIS_* are set

# Migrations run automatically on startup (Flyway enabled)
./mvnw spring-boot:run

# Or package + run JAR
./mvnw clean package -DskipTests
java -jar target/ExoticStamp-0.0.1-SNAPSHOT.jar
```

- Migrations: `backend/src/main/resources/db/migration/V1__*.sql` … `V17__*.sql`
- Prod uses `ddl-auto: validate` (no Hibernate auto-DDL)
- **Rollback:** restore DB snapshot; Flyway does not auto-downgrade — plan forward-fix migrations

---

## 5. Redis

**Required at runtime** for:

- Refresh token storage
- OTP / verification flows
- Token revocation cache

If Redis is unavailable, see `backend/docs/AUTH_REDIS_UNAVAILABLE_BEHAVIOR.md`.

Local: `docker compose up redis` or `REDIS_HOST=localhost`.

---

## 6. File upload / public assets

| Setting | Default | Description |
|---------|---------|-------------|
| `storage.provider` | `local` | Local filesystem storage |
| `storage.local.base-path` | `./uploads` | Writable directory on server |
| `storage.local.base-url` | env `STORAGE_LOCAL_URL` | URL prefix for clients |
| Public HTTP path | `/uploads/**` | Served by `StaticFileController` |
| Security | `/uploads/public/**` | Permitted without auth in `SecurityConfig` |

**Production notes:**

- Mount persistent volume at `STORAGE_LOCAL_PATH`
- Serve `/uploads` via reverse proxy or CDN in front of API
- For multi-instance deployments, replace `local` with shared object storage (future)

---

## 7. Mail

SMTP via Spring Mail (`spring.mail.*`). Dev defaults in `application-dev.yml`; prod requires explicit `MAIL_*` env vars.

Mail queue worker runs in-process (see `application.mail.queue.*`).

---

## 8. HTTPS / reverse proxy

The JAR listens on **8080** (HTTP). Production should terminate TLS at:

- Nginx / Traefik / cloud load balancer
- Set `BACKEND_URL=https://api.yourdomain.com`
- Forward `X-Forwarded-Proto` if needed for redirect URLs

Mobile MVP uses HTTP on LAN for dev; production mobile must use HTTPS API base (future `API_USE_HTTPS` / flavor).

---

## 9. Logging

| Profile | Level |
|---------|-------|
| dev | `DEBUG` security/web |
| prod | `WARN` security, `INFO` web |
| base | `INFO` |

Structured logging: `logback-spring.xml`. Do not log JWT, refresh tokens, OTP, or voucher secrets.

---

## 10. Health / smoke endpoints

| Endpoint | Auth | Use |
|----------|------|-----|
| `GET /swagger-ui/index.html` | — | Dev only; **404/disabled in prod** |
| `GET /v3/api-docs` | — | Dev only; disabled in prod |
| `POST /api/v1/auth/login` | public | Smoke: credentials |
| `GET /api/v1/users/me` | Bearer | Smoke: session |
| `GET /api/v1/metro/lines` | public | Smoke: metro catalog |
| `GET /api/v1/campaigns/active` | public | Smoke: campaign |
| `POST /api/v1/metro/scan/resolve` | public | Smoke: scan payload |
| `GET /api/v1/collection/stamp-book` | Bearer | Smoke: collection |

**Actuator:** not enabled in MVP — use API smoke tests above.

---

## 11. Docker deployment (local / demo)

```bash
# From repo root
docker compose up --build
# API: http://localhost:8080
# Postgres: localhost:5433 (mapped)
```

Image: `infra/docker/backend.Dockerfile` → `ExoticStamp-0.0.1-SNAPSHOT.jar`

---

## 12. Swagger production policy

| Environment | Swagger UI | OpenAPI JSON |
|-------------|------------|--------------|
| dev | enabled | enabled |
| prod | **disabled** | **disabled** |

Contract files for mobile: `backend/docs/api/openapi.json`, `MOBILE_API_CONTRACT.md` (export from dev when API changes).

---

## 13. Pre-deploy checklist

- [ ] `SPRING_PROFILES_ACTIVE=prod`
- [ ] Strong `JWT_SECRET` (32+ random chars)
- [ ] PostgreSQL migrated (Flyway clean on empty DB)
- [ ] Redis reachable
- [ ] `CORS_ALLOWED_ORIGINS` set to known app origins only
- [ ] Mail configured (if auth email flows needed)
- [ ] Upload volume mounted and backed up
- [ ] TLS termination in front of API
- [ ] Demo seed **not** running (`dev` profile off)
- [ ] `./mvnw clean test` green in CI
- [ ] Smoke tests pass against staging/prod URL

---

## 14. Rollback

1. Stop new instances
2. Restore previous JAR/Docker image tag
3. Restore PostgreSQL snapshot if schema/data migration failed
4. Redis can be flushed for auth emergencies (users re-login)
5. Re-run smoke tests on rolled-back version
