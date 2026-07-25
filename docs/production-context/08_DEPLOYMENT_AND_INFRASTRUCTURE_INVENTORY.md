# Deployment And Infrastructure Inventory

## Target Inventory Status

| Area | Status | Evidence |
|---|---|---|
| AWS Lightsail | documented / scripted only | backend staging deploy scripts and docs mention remote/container flow |
| S3 | documented and partially implemented | config + prod validator + disabled Java S3 classes |
| Docker | implemented and locally evidenced | backend Dockerfile, root compose, prod-like compose |
| Caddy | implemented as examples | staging/local/example Caddyfiles |
| PostgreSQL | implemented locally and in compose | root compose + prod-like compose + Flyway |
| Redis | implemented locally and in compose | root compose + prod-like compose + readiness checks |
| Vercel | intended only | no committed project config found |
| GitHub Actions | backend implemented | backend CI workflow |
| Backup / restore | scripted only | deployment scripts mention backup/preflight |
| Release manifest | implemented as example/script | `backend/infra/release/release-manifest.example.json`, release script |
| Rollback | documented/scripted only | deploy script preserves previous version marker, but no live environment verification |
| Smoke tests | scripted only | `backend/scripts/smoke/staging_smoke.sh` |
| Monitoring / alerts | partial | actuator health and logs; no external alerting config found |

## Docker

- Local stack in root `docker-compose.yml`
- Prod-like stack in `backend/docker-compose.prod-like.yml`
- Backend image is multi-stage, non-root user, readiness healthcheck

## Caddy

Staging Caddy example includes:

- TLS termination via domain placeholder
- Swagger blocking
- readiness/liveness proxying
- `/api/*` reverse proxy
- HSTS and basic security headers

## AWS / S3 Gaps

- No live AWS resource evidence was inspected
- No Terraform / CloudFormation was found
- S3 active implementation is incomplete in compiled code because active classes are disabled

## Vercel Gaps

- No `vercel.json`
- No committed Vercel env/project binding
- No proof of staging or production deployment state

## Verification Levels

- Implemented locally:
  - backend Docker/compose patterns
  - web local build/test
  - mobile local analyze/test
- Staging verified:
  - not proven from source alone
- Production verified:
  - not proven from source alone
