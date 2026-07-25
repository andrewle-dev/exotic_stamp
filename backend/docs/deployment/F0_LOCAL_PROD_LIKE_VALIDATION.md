# F.0 Local Prod-like Validation Record

**Date (UTC):** 2026-07-25  
**Commit at build:** ba6cb60 (working tree dirty — prior batch + F.0 prep; `--allow-dirty`)  
**Release version used:** `0.1.0-rc.1`  
**Image tags:** `exotic-stamp-backend:v0.1.0-rc.1`, `exotic-stamp-backend:git-ba6cb60`

## Commands and results

| Step | Command / action | Exit / result |
|------|------------------|---------------|
| Caddy example validate | `docker run --rm -e DOMAIN=localhost -v .../Caddyfile.example:/etc/caddy/Caddyfile:ro caddy:2.8-alpine caddy validate` | **0** Valid |
| Caddy staging validate | same with `Caddyfile.staging.example` | **0** Valid |
| Caddy local validate | `Caddyfile.local.example` (`:80`) | **0** Valid |
| Image build | `scripts/release/build_image.ps1 -AllowDirty -Version 0.1.0-rc.1` | **0** |
| Image inspect | `scripts/release/inspect_image.ps1 -Image exotic-stamp-backend:git-ba6cb60` | **INSPECT PASS** (non-root `app`) |
| Compose up | `docker compose -f docker-compose.prod-like.yml -f docker-compose.prod-like.local.yml --profile local-s3 --env-file .env.prod-like up -d` | **0** |
| Backend ports | `docker ps` | Backend **8080/tcp only** (unpublished); Caddy **80/443** published |
| Prod profile | Logs: `The following 1 profile is active: "prod"` | PASS |
| Flyway | Applied / at **v23** | PASS |
| JTE packaging defect | Fixed via `application-prod.yml` `gg.jte.use-precompiled-templates: true` | Required for boot |
| FRONTEND/BACKEND URLs | Must be non-localhost for `ProdStartupValidator` | Used `https://admin.example.com` / `https://api.example.com` |
| Readiness via Caddy | `curl http://localhost/actuator/health/readiness` | **200** `{"status":"UP"}` |
| Liveness | same `/liveness` | **200** |
| Swagger | `/swagger-ui`, `/v3/api-docs` | **404** |
| Staging smoke local | `staging_smoke.ps1` | **SMOKE PASS** (liveness, readiness, swagger, invalid login 401, 429+Retry-After) |
| LocalStack bucket | `awslocal s3 mb s3://exotic-stamp-local-test` | Created |
| DB preflight | `BATCH_E_DATA_PREFLIGHT.sql` via docker exec (read-only) | Completed; Flyway **23** |
| Backup | `pg_dump -Fc` inside postgres container | PASS + SHA-256 |
| Verify | `pg_restore --list` | PASS (246 TOC entries) |
| Restore | into `exotic_stamp_restore_tmp` (no DROP) | PASS; flyway rows **23** |
| Graceful SIGTERM | `docker stop -t 40 backend-backend-1` | PASS; restart readiness **200** |
| Deploy dry-run | `deploy_staging.ps1 -DryRun` | **0** |
| Rollback dry-run | `rollback_staging.ps1 -DryRun` | **0** |

## Notes

- Spring Boot does not load `.env` alone; Compose `env_file` used.
- Local ACME avoided via `Caddyfile.local.example`.
- No real AWS credentials; LocalStack only.
- PostgreSQL/Redis not published on host.
