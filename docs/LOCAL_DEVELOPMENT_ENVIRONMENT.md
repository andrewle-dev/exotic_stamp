# Local Development Environment

**Last verified:** 2026-07-13 (Windows + Docker Desktop + Cursor)

## 1. Final architecture

```
┌──────────────────────── Cursor / host ────────────────────────┐
│  Spring Boot (dev) :8080   Vite web :5173   Flutter (device) │
└───────────────┬──────────────────┬────────────────────────────┘
                │ localhost:5433   │ localhost:6379
┌───────────────▼──────────────────▼────────────────────────────┐
│  Docker Compose project: exoticstamp                          │
│  ├── exotic-stamp-postgres (postgres:16-alpine)               │
│  ├── exotic-stamp-redis    (redis:7-alpine, AOF + password)   │
│  └── exotic-stamp-backend  (profile `full` only)              │
└───────────────────────────────────────────────────────────────┘
```

Default `docker compose up -d` starts **only** PostgreSQL and Redis.  
Web and Flutter always run on the host. Backend normally runs on the host; optional Docker backend uses `--profile full`.

## 2. Folder structure

```
/
├── backend/                 Spring Boot 3.3 / Java 21
├── mobile/                  Flutter
├── web/                     React + Vite admin
├── docker-compose.yml       Project name: exoticstamp
├── .env                     Local secrets (gitignored)
├── .env.example             Safe template
├── .gitignore
├── .vscode/tasks.json
├── .vscode/launch.json
└── docs/
    ├── LOCAL_DEVELOPMENT_ENVIRONMENT.md
    ├── LOCAL_DEVELOPMENT_ENVIRONMENT_AUDIT.md
    └── LOCAL_DEVELOPMENT_IMPLEMENTATION_REPORT.md
```

## 3. Required tools

| Tool | Purpose |
|------|---------|
| Docker Desktop | PostgreSQL + Redis (+ optional backend) |
| Java 21 | Host backend |
| Maven wrapper (`backend/mvnw.cmd`) | Build / run / test |
| Node.js + npm | Admin web (`package-lock.json`) |
| Flutter SDK | Mobile |
| Cursor / VS Code + Extension Pack for Java | Debug without IntelliJ |

## 4. Docker Compose project layout

Docker Desktop group:

```
exoticstamp
├── exotic-stamp-postgres
└── exotic-stamp-redis
(+ exotic-stamp-backend when --profile full)
```

| Resource | Name |
|----------|------|
| Project | `exoticstamp` |
| Network | `exotic-stamp-network` |
| Postgres volume | `exoticstamp_postgres_data` (preserved existing data volume) |
| Redis volume | `exotic-stamp-redis-data` |
| Uploads volume (Docker backend only) | `exotic-stamp-uploads` |

> **Volume note:** The plan name `exotic-stamp-postgres-data` was **not** used for Postgres because volume `exoticstamp_postgres_data` already held project databases. Renaming would create an empty DB.

## 5. Host vs Docker networking

| Caller | PostgreSQL host | Redis host | Backend URL |
|--------|-----------------|------------|-------------|
| Backend on Windows host | `localhost` + `POSTGRES_PORT` | `localhost` | self `:8080` |
| Backend in Compose `full` | `postgres` (service DNS) | `redis` | container `:8080` |
| Browser (web) | n/a | n/a | `http://localhost:8080` |
| Android emulator | n/a | n/a | `http://10.0.2.2:8080` |
| iOS simulator | n/a | n/a | `http://localhost:8080` |
| Physical device | n/a | n/a | `http://<LAN-IP>:8080` |

**Never** use hostname `postgres` / `redis` from host-mode Spring Boot.  
**Never** use `localhost` for DB/Redis from the Docker backend container.

## 6. Environment variables

Root `.env` (Compose + host backend tasks):

| Variable | Role |
|----------|------|
| `COMPOSE_PROJECT_NAME` | Must be `exoticstamp` |
| `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` / `POSTGRES_PORT` | Compose Postgres + must match initialized volume |
| `REDIS_PASSWORD` / `REDIS_PORT` | Compose Redis + Spring |
| `BACKEND_PORT` | Host publish for Docker backend |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | Host Spring datasource |
| `REDIS_HOST` | Host Spring Redis |
| `STORAGE_*` | Local uploads |
| `JWT_SECRET` | Required at runtime |

Spring also reads `SERVER_PORT`, `STORAGE_PROVIDER`, `STORAGE_LOCAL_PATH`, `STORAGE_LOCAL_URL`.

## 7. How to create `.env`

```powershell
Copy-Item .env.example .env
```

If volume `exoticstamp_postgres_data` already exists, **keep the user/password that initialized it**. On this machine that is `postgres` / `f123`, not the greenfield `exotic_stamp` / `change_me` from `.env.example`.

If a **native** Windows PostgreSQL already owns host port `5432`, set:

```env
POSTGRES_PORT=5433
DB_URL=jdbc:postgresql://localhost:5433/exotic_stamp
```

(Current verified local `.env` uses `5433` for this reason.)

## 8. Start infrastructure

From repo root:

```powershell
docker compose up -d
docker compose ps
```

Or Cursor task: **Docker: Start Infrastructure**, then **Docker: Wait Until Healthy**.

## 9. Stop infrastructure

```powershell
docker compose down
```

**Never** run `docker compose down -v` — that deletes development volumes.

## 10. View logs

```powershell
docker compose logs -f --tail=100 postgres redis
```

## 11. Run backend from Cursor terminal

```powershell
cd backend
# Load root .env into the session, or rely on task envFile
$env:SPRING_PROFILES_ACTIVE="dev"
.\mvnw.cmd spring-boot:run
```

Cursor task: **Backend: Run Dev** (starts infra, waits healthy, then runs Maven).

## 12. Debug backend with breakpoints

1. Run task **Docker: Ensure Healthy Infrastructure** (or compound launch).
2. Launch **Debug ExoticStamp Backend** (`launch.json`).
3. Compound **Infra + Debug Backend** runs the ensure-healthy preLaunchTask then Java debug.

Main class: `metro.ExoticStamp.ExoticStampApplication`  
Working directory: `backend`  
Profile: `dev` via env (not hardcoded in `application.yml`).

## 13. Run backend in Docker (`full` profile)

Stop any host process on `8080` first.

```powershell
docker compose --profile full up -d --build
```

Backend joins `exotic-stamp-network`, uses `postgres` / `redis` hostnames, mounts `exotic-stamp-uploads` at `/app/uploads`, runs as non-root user `app`.

## 14. Run web

```powershell
cd web
Copy-Item .env.example .env   # once
npm install
npm run dev
```

Opens `http://localhost:5173/`. API base: `VITE_API_BASE_URL=http://localhost:8080`.

## 15. Run Flutter

```powershell
cd mobile
flutter pub get
flutter run
```

Do not containerize Flutter.

## 16–18. Volumes and uploads

| Kind | Location |
|------|----------|
| PostgreSQL | Docker volume `exoticstamp_postgres_data` |
| Redis | Docker volume `exotic-stamp-redis-data` |
| Host uploads | `D:/Part-time/ExoticStamp/backend/uploads` (public URL `http://localhost:8080/uploads`) |
| Docker uploads | volume `exotic-stamp-uploads` → `/app/uploads` |

`storage.local.base-path` must stop at `/uploads` (no duplicated `/public/public` in the base path). Public files are served under `/uploads/public/**`.

## 19. Data reset warning

- Do **not** `docker compose down -v`
- Do **not** `docker volume rm exoticstamp_postgres_data`
- Do **not** `docker system prune` as a “cleanup” for this project
- Recreating the Postgres **container** is safe if the **named volume** is kept

## 20. Port conflict handling

| Port | Situation | Action |
|------|-----------|--------|
| 5432 | Native `postgres` + Docker | Publish Docker as `5433` and set `DB_URL` accordingly |
| 6379 | Orphan / stale Redis | Stop/rm conflicting container **without** `-v`, then `docker compose up -d` |
| 8080 | Host backend + `full` profile | Run only one backend |

Stale exited containers (`infra-*`, `metricsx-redis`) must not be started.

## 21. Flyway checksum troubleshooting

If startup fails with a checksum mismatch:

1. Stop the backend.
2. Note the exact migration version/file from the log.
3. **Do not** run `flyway repair` automatically in shared/prod-like data.
4. Do not edit already-applied migration files.
5. Resolve with a new forward migration or an intentional, documented repair.

Schema authority is Flyway only (`spring.jpa.hibernate.ddl-auto=validate`).

## 22. Windows path guidance

Prefer forward slashes in `.env`:

```env
STORAGE_LOCAL_PATH=D:/Part-time/ExoticStamp/backend/uploads
```

Backslashes in YAML/env can break escaping. Java `Path` accepts the forward-slash form.

## 23. Android emulator host mapping

| Client | Backend base |
|--------|----------------|
| Android emulator | `http://10.0.2.2:8080` (see `mobile/lib/core/config/api_config.dart`) |
| iOS simulator | `http://localhost:8080` |
| Physical device | `http://<your-LAN-IP>:8080` via `--dart-define=API_HOST=...` |

`localhost` inside the Android emulator is the emulator itself, not the Windows host.

## 24. Common failure modes (edge cases)

1. Port 5432 occupied by old/native PostgreSQL → use `POSTGRES_PORT=5433` + matching `DB_URL`.
2. Port 6379 occupied by `metricsx-redis` (if started) → stop it; do not delete unrelated volumes.
3. Existing ExoticStamp volume has valuable DBs (`exotic_stamp_flyway_test`, etc.) → preserve `exoticstamp_postgres_data`.
4. Host backend uses hostname `postgres` → connection fails; use `localhost`.
5. Docker backend uses `localhost` for DB → points at itself; use `postgres` / `redis`.
6. Redis `requirepass` set but Spring omits password → set `REDIS_PASSWORD` (wired in `application.yml`).
7. Compose run from a subdirectory → wrong project name; always run from repo root (`name: exoticstamp` + `COMPOSE_PROJECT_NAME`).
8. Windows backslashes in env → prefer `D:/...` paths.
9. Postgres up but not healthy → check `docker compose logs postgres`; wait task times out at 90s.
10. Redis auth healthcheck fails → password mismatch between `.env` and container command.
11. Backend before dependencies ready → use **Wait Until Healthy** / `depends_on` conditions.
12. Flyway checksum mismatch → stop and report; no automatic repair ops.
13. Hibernate schema update → forbidden; keep `validate`.
14. `.env` committed → blocked by `.gitignore` (`.env` / `.env.*`, allow `.env.example`).
15. `docker compose down -v` → destroys data; tasks use `down` only.
16. Android emulator cannot reach `localhost:8080` → use `10.0.2.2`.
17. Backend image as root → Dockerfile uses non-root `app`.
18. Upload path `/uploads/public/public` → keep base path at `/uploads` only.
19. Old Compose files / orphan containers → same `container_name` conflicts; replace orphans carefully.
20. Host + Docker backend both on 8080 → bind failure; exclusive modes.

## 25. Exact validation results (this machine)

| Check | Result |
|-------|--------|
| `docker compose config` | OK |
| Postgres healthy | Yes (`postgres:16-alpine`, port **5433**) |
| Redis healthy | Yes (password + AOF) |
| Volume preserved | `exoticstamp_postgres_data` (skipped init; prior DBs kept) |
| `mvnw clean test` | **375** tests, **0** failures, **4** skipped, BUILD SUCCESS |
| Host `spring-boot:run` | Started; Flyway V1–V18 on Docker PG 16.11 via `localhost:5433` |
| Storage path | `D:\Part-time\ExoticStamp\backend\uploads` |
| Actuator | `http://localhost:8080/actuator/health` → `{"status":"UP"}` |
| Swagger | `http://localhost:8080/swagger-ui/index.html` → 200 |
| OpenAPI | `http://localhost:8080/v3/api-docs` → 200 |
| Web | Vite ready at `http://localhost:5173/` |
| Flutter | `flutter doctor` no issues; `flutter pub get` OK |
| Backend image | `docker compose --profile full build backend` → Built |

### Quick commands

```powershell
# Start infra
docker compose up -d

# Stop infra (keeps volumes)
docker compose down

# Host backend
cd backend; $env:SPRING_PROFILES_ACTIVE="dev"; .\mvnw.cmd spring-boot:run
```
