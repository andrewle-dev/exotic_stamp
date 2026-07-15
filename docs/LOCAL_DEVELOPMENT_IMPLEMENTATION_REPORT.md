# Local Development Environment — Implementation Report

**Date:** 2026-07-13  
**Machine:** Windows 10/11, Docker Desktop, Java 21.0.7, Flutter 3.41.0

## Files changed / created

| File | Action |
|------|--------|
| `docs/LOCAL_DEVELOPMENT_ENVIRONMENT_AUDIT.md` | Created (Phase 0) |
| `docs/LOCAL_DEVELOPMENT_ENVIRONMENT.md` | Created |
| `docs/LOCAL_DEVELOPMENT_IMPLEMENTATION_REPORT.md` | Created (this file) |
| `docker-compose.yml` | Replaced/normalized (`name: exoticstamp`, network, volumes, Redis auth/AOF, `full` profile backend) |
| `.env.example` | Created |
| `.env` | Created (did not overwrite — was missing); adapted to existing Postgres volume + port 5433 |
| `.gitignore` | Updated (allow `.vscode/tasks.json` + `launch.json`; keep `.env` ignored; uploads ignore) |
| `.vscode/tasks.json` | Created |
| `.vscode/launch.json` | Created |
| `backend/Dockerfile` | Created (Java 21 multi-stage, non-root) |
| `backend/src/main/resources/application.yml` | `server.port`, `open-in-view=false`, Redis password/timeout, actuator exposure |
| `backend/src/main/resources/application-dev.yml` | Redis password/timeout defaults |
| `backend/src/main/resources/application-prod.yml` | Redis password/timeout |
| `backend/src/main/java/.../SecurityConfig.java` | Permit `/actuator/health` (+ info) for probes |
| `backend/.env.example` | Redis password + `SERVER_PORT` |
| `backend/pom.xml` | Fixed leading stray `` ` `` that made POM unparseable (blocked Maven) |
| `web/.env` | Created from example for local Vite |
| `infra/docker/backend.Dockerfile` | Left in place (legacy); root Compose now uses `backend/Dockerfile` |

## Commands run

```text
docker ps -a / compose ls / volume ls / network ls / inspect (audit)
docker compose config
docker stop/rm exotic-stamp-redis   # orphan, no -v (Phase 7 Option C)
docker compose up -d
docker compose ps / logs
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run          # host mode
# remapped Postgres host port 5432 → 5433 after detecting native PostgreSQL on 5432
docker compose up -d
.\mvnw.cmd spring-boot:run          # against localhost:5433
npm install / npm run dev           # web
flutter doctor / flutter pub get
docker compose --profile full build backend
```

**Not run:** `docker compose down -v`, `docker volume rm`, `docker system prune`, Flyway repair.

## Container status (after standardization)

| Container | Image | Ports | Health |
|-----------|-------|-------|--------|
| `exotic-stamp-postgres` | `postgres:16-alpine` | `5433→5432` | healthy |
| `exotic-stamp-redis` | `redis:7-alpine` | `6379→6379` | healthy |
| `exotic-stamp-backend` | not started | — | image **built**; runtime skipped while host backend held 8080 |

Network: `exotic-stamp-network`  
Volumes: `exoticstamp_postgres_data`, `exotic-stamp-redis-data` (uploads volume created on first `full` up)

## Health status

- Postgres: healthy (`pg_isready`)
- Redis: healthy (`REDISCLI_AUTH` + `PING`)
- Backend actuator: `{"status":"UP"}`
- Compose project in Desktop: `exoticstamp` with grouped services

## Test results

| Metric | Value |
|--------|-------|
| Tests run | 375 |
| Failures | 0 |
| Errors | 0 |
| Skipped | 4 |
| Result | BUILD SUCCESS |

## Backend startup result

- Java 21.0.7
- Profile `dev`
- JDBC `jdbc:postgresql://localhost:5433/exotic_stamp` → **PostgreSQL 16.11** (Docker)
- Flyway applied V1–V18 successfully on Docker volume DB `exotic_stamp` (was empty; `exotic_stamp_flyway_test` on same volume untouched with 28 tables)
- Hibernate validate OK
- Redis password auth OK
- Tomcat `:8080`
- LocalStorage: `D:\Part-time\ExoticStamp\backend\uploads`
- No IntelliJ dependency

## Swagger / OpenAPI

| URL | Result |
|-----|--------|
| `http://localhost:8080/swagger-ui/index.html` | 200 |
| `http://localhost:8080/v3/api-docs` | 200 |
| `http://localhost:8080/actuator/health` | UP |

## Web startup result

- Package manager: **npm** (`package-lock.json`)
- `npm install` OK
- `npm run dev` → Vite `http://localhost:5173/`
- `VITE_API_BASE_URL=http://localhost:8080` (no Docker hostname `backend`)

## Mobile tooling

- `flutter doctor`: no issues
- `flutter pub get`: OK
- API host mapping already documented in `api_config.dart` (`10.0.2.2` for Android emulator)

## Unresolved / manual notes

1. **Native PostgreSQL** still listens on host `5432`. Docker Postgres intentionally uses **`5433`**. Do not point host backend at `5432` on this machine or you hit PG 18 instead of Compose PG 16.
2. First mistaken start against native PG 18 may have applied Flyway there; leave that alone. Canonical local DB is Docker volume `exoticstamp_postgres_data` via port **5433**.
3. Local `.env` uses existing volume credentials (`postgres` / `f123`), not greenfield `exotic_stamp` / `change_me`.
4. Orphan Redis container was stopped/removed (no `-v`) so Compose could own `exotic-stamp-redis`.
5. Backend Docker **runtime** not co-started with host backend (port 8080 exclusivity). Image build verified.
6. Collation version warnings from Alpine PG 16 against older libc locales on the volume — non-blocking.

## Final verdict

| Area | Verdict |
|------|---------|
| INFRA | **READY** |
| BACKEND HOST MODE | **READY** |
| BACKEND DOCKER MODE | **READY** (image builds; run with `--profile full` when host `:8080` is free) |
| WEB | **READY** |
| MOBILE TOOLING | **READY** |

## Exact start / stop

```powershell
# Start infra
docker compose up -d

# Host backend
cd backend
$env:SPRING_PROFILES_ACTIVE="dev"
# ensure DB_URL uses localhost:5433 when native PG owns 5432
.\mvnw.cmd spring-boot:run

# Stop infra (keep data)
docker compose down
```
