# Local Development Environment — Pre-Change Audit (Phase 0)

**Date:** 2026-07-13  
**Scope:** Read-only inspection before standardizing Compose, env, Cursor tasks, and host/Docker backend modes.  
**Policy:** No containers, volumes, or `.env` files were deleted or overwritten during this audit.

---

## 1. Root structure (observed)

| Path | Status |
|------|--------|
| `backend/` | Present |
| `mobile/` | Present |
| `web/` | Present |
| `infra/` | Present (legacy Dockerfile/scripts) |
| `docs/` | Present |
| `docker-compose.yml` | Present (root) |
| `.env` | **Missing** |
| `.env.example` | **Missing** (root); `backend/.env.example` and `web/.env.example` exist |
| `.gitignore` | Present |
| `.vscode/` | **Missing** |
| `backend/Dockerfile` | **Missing** (legacy: `infra/docker/backend.Dockerfile`) |
| Maven wrapper | `backend/mvnw`, `backend/mvnw.cmd`, only-script wrapper |

---

## 2. Existing Compose files

| File | Role |
|------|------|
| `/docker-compose.yml` | Only Compose file. No `name:` key (project defaults to directory → `exoticstamp`). Defines `postgres`, `redis`, and always-on `api` (not profile-gated). Hardcoded DB credentials. Postgres host port mapped as `5433:5432` in file, but **running** container publishes **5432**. Redis has no password, no named volume, no AOF. No dedicated network name. |
| `infra/docker/backend.Dockerfile` | Single-stage-ish Maven image build; skips tests; runs as root; jar name hardcoded. |
| `infra/scripts/start-local.sh`, `stop-local.sh` | Legacy helpers (not used by this standardization). |
| `infra/environments/local.env` | Dev defaults: `postgres` / `f123`, Redis localhost:6379. |

---

## 3. Existing container names

| Name | Image | Status | Compose project | Notes |
|------|-------|--------|-----------------|-------|
| `exotic-stamp-postgres` | `postgres:16` | **Up (healthy)** | `exoticstamp` | Active project DB host |
| `exotic-stamp-redis` | `redis:7-alpine` | **Up** | **none** (orphan; no Compose labels) | Occupies 6379 |
| `infra-postgres-1` | `postgres:15` | Exited | `infra` | Stale; would bind 5432 if started |
| `infra-redis-1` | `redis:7-alpine` | Exited | `infra` | Stale; would bind 6379 if started |
| `metricsx-redis` | `redis:7-alpine` | Exited | — | Stale; would bind 6379 if started |
| Other postgres (`dunglv_*`, `keen_leakey`, …) | postgres | Exited | — | Unrelated |
| `budgetpal-*` | various | Created/Exited | budgetpal | Unrelated |

`docker compose ls`: project **`exoticstamp`** running **(1)** service — postgres only. Redis is not managed by current Compose despite sharing the name `exotic-stamp-redis`.

---

## 4. Port conflicts

| Port | Listener | Conflict risk |
|------|----------|---------------|
| **5432** | `exotic-stamp-postgres` (active) + exited `infra-postgres-1` | Active ExoticStamp postgres owns 5432. Compose file’s `5433` mapping is **stale vs reality**. |
| **6379** | `exotic-stamp-redis` (orphan, active) + exited `infra-redis-1`, `metricsx-redis` | Orphan Redis owns 6379. Recreating Compose Redis with same `container_name` requires stopping/removing the orphan first (no `-v`). |
| **8080** | None listening at audit time | Free for host or Docker backend |

---

## 5. Existing volumes (relevant)

| Volume | Likely use |
|--------|------------|
| **`exoticstamp_postgres_data`** | Mounted on `exotic-stamp-postgres` → `/var/lib/postgresql/data`. **Must preserve.** |
| Anonymous volume on Redis (`6bc98887…`) | Orphan Redis `/data`. Cache-only; low value. |
| `infra_postgres_data`, `infra_redis_data` | Old `infra` stack — do not attach |
| `budgetpal_*` | Unrelated — do not attach |

Target names from the plan (`exotic-stamp-postgres-data`, `exotic-stamp-redis-data`) **must not replace** `exoticstamp_postgres_data` silently — that would create an empty DB.

---

## 6. Likely active ExoticStamp database

- Container: `exotic-stamp-postgres`
- Volume: `exoticstamp_postgres_data`
- Credentials (from container env): `POSTGRES_USER=postgres`, `POSTGRES_PASSWORD=f123`, `POSTGRES_DB=exotic_stamp`
- Encoding: UTF-8
- Databases present:
  - `exotic_stamp` — **empty** (no user tables, no `flyway_schema_history`)
  - `exotic_stamp_flyway_test` — **has schema + Flyway history** (valuable test/dev data; do not drop volume)
- Desired greenfield user `exotic_stamp` **cannot** be introduced via env alone without re-init; adapt host/app credentials to **`postgres` / existing password**.

---

## 7. Likely stale containers

- `infra-postgres-1`, `infra-redis-1`, `infra-mosquitto-1`
- `metricsx-redis`
- Ad-hoc postgres containers from months ago
- `budgetpal-*` created-but-not-running

These must **not** be auto-deleted. Document manual stop only if a user starts them and conflicts with 5432/6379.

---

## 8. Spring / storage / Flyway snapshot

| Topic | Current state |
|-------|---------------|
| Datasource | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` |
| Redis | `REDIS_HOST`, `REDIS_PORT` — **no `REDIS_PASSWORD` property** |
| JPA | `ddl-auto: validate` in dev/prod; **`open-in-view` not set** (Boot default true) |
| Flyway | Enabled in dev with `baseline-on-migrate` + `repair-on-migrate` |
| Storage | `STORAGE_PROVIDER`, `STORAGE_LOCAL_PATH`, `STORAGE_LOCAL_URL`; defaults already point at `D:/Part-time/ExoticStamp/backend/uploads` and `http://localhost:8080/uploads` |
| Multipart | 5MB file / 6MB request |
| Actuator | Dependency present; **no management YAML**; `/actuator/**` not in security permit list |
| Profiles | Not hardcoded in `application.yml` (good) |
| Server port | Not set (default 8080) |

---

## 9. Files that will be modified / created

**Create**

- `docs/LOCAL_DEVELOPMENT_ENVIRONMENT_AUDIT.md` (this file)
- `docs/LOCAL_DEVELOPMENT_ENVIRONMENT.md`
- `docs/LOCAL_DEVELOPMENT_IMPLEMENTATION_REPORT.md`
- `.env.example` (root)
- `.env` (local only, if missing — **dev credentials adapted to existing volume**)
- `.vscode/tasks.json`
- `.vscode/launch.json`
- `backend/Dockerfile`

**Update**

- `docker-compose.yml` (project name, network, volumes, healthchecks, Redis auth/AOF, `full` profile backend)
- `.gitignore` (allow committing VS Code tasks/launch; keep `.env` ignored)
- `backend/src/main/resources/application.yml` (Redis password/timeout, open-in-view, server.port)
- `backend/src/main/resources/application-dev.yml` (align defaults with host localhost + password support)
- `backend/.env.example` (align Redis password / port notes)
- Possibly `SecurityConfig` only if Docker health needs public `/actuator/health` (minimal)

**Do not overwrite**

- Existing root `.env` if it appears later
- Applied Flyway migrations
- Unrelated business code

---

## 10. Data-loss risks

| Risk | Severity | Mitigation |
|------|----------|------------|
| Renaming Compose volume to `exotic-stamp-postgres-data` | **Critical** | Keep explicit `name: exoticstamp_postgres_data` |
| `docker compose down -v` | **Critical** | Forbidden; tasks must use `down` without `-v` |
| Changing `POSTGRES_USER` to `exotic_stamp` on existing volume | **High** | Keep `postgres` in local `.env`; document in example |
| Attaching `infra_postgres_data` by mistake | **High** | Explicit volume name only |
| Orphan Redis recreate with password | Low (cache) | Stop/rm container without `-v`; new named volume |
| Starting both host backend and `full` profile backend on 8080 | Medium | Document mutual exclusion |
| Hibernate `ddl-auto` other than `validate` | Medium | Keep validate; never create/update |
| Flyway checksum mismatch | Medium | Stop and report; no automatic repair in ops docs (dev YAML still has repair-on-migrate — do not expand) |

---

## 11. Recommended Phase 7 strategy

1. **Postgres:** Reuse `exotic-stamp-postgres` + volume `exoticstamp_postgres_data` (Option A). Keep host port **5432**. Keep user **`postgres`**.
2. **Redis:** Orphan `exotic-stamp-redis` blocks standardized Compose service with the same name (Option C). Explicit command before recreate:  
   `docker stop exotic-stamp-redis` then `docker rm exotic-stamp-redis` (**no** `-v`).
3. Do **not** start exited `metricsx-redis` / `infra-*` containers.
4. If 5432/6379 ever occupied by non-ExoticStamp processes, fall back to host ports 5433/6380 in `.env` and mirror in `DB_URL` / `REDIS_PORT`.

---

## 12. Audit verdict

Safe to proceed with file changes. **Primary constraint:** preserve `exoticstamp_postgres_data` and existing Postgres role `postgres`. **Blocking ops step:** replace orphan Redis container under the same name before Compose Redis can join project `exoticstamp`.
