# Database Migration Preflight Runbook

**Batch:** F.0
**Policy:** Forward-only Flyway. Never edit applied migrations V1–V23. Never auto-fix dirty data.

## Purpose

Gate staging/production deploys when reward/voucher/campaign/idempotency integrity is dirty or Flyway history is failed.

## Prerequisites

- `psql` client available
- Connection via env: DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, PGPASSWORD (or DB_PASSWORD)
- Never put passwords on argv

## Commands

```bash
export APP_ENV=staging
export DB_HOST=... DB_PORT=5432 DB_NAME=... DB_USERNAME=...
export PGPASSWORD=...
./scripts/deployment/run_db_preflight.sh
```

```powershell
$env:APP_ENV='local-prod-like'
$env:DB_HOST='localhost'; $env:DB_PORT='5432'; $env:DB_NAME='exotic_stamp'
$env:DB_USERNAME='exotic_app'; $env:PGPASSWORD='...'
.\scripts\deployment\run_db_preflight.ps1
```

## Checks performed

1. PostgreSQL version / extensions
2. flyway_schema_history listing
3. Failed Flyway rows (launch-blocking)
4. Current schema version
5. Packaged vs applied migration awareness
6. BATCH_E_DATA_PREFLIGHT.sql (read-only)
7. stored_assets status counts when table exists

## Exit codes

| Code | Meaning |
|------|---------|
| 0 | PASS |
| 1 | FAIL — launch-blocking findings |
| 2 | Usage / missing env |

## Operator actions on FAIL

Resolve duplicate voucher links, conflicting default campaigns, failed Flyway rows manually. Do not run destructive SQL from CI. Re-run preflight until clean, then backup, then deploy (Flyway applies pending additive migrations only).
