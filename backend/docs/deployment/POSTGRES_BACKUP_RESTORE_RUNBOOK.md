# PostgreSQL Backup and Restore Runbook

**Batch:** F.0
**Tools:** pg_dump / pg_restore custom format (-Fc). Passwords via PGPASSWORD only.

## Backup

```powershell
$env:APP_ENV='local-prod-like'
$env:DB_HOST='localhost'; $env:DB_PORT='5432'; $env:DB_NAME='exotic_stamp'
$env:DB_USERNAME='exotic_app'; $env:PGPASSWORD='...'
.\scripts\deployment\backup_postgres.ps1
.\scripts\deployment\verify_backup.ps1 -DumpPath <path.dump>
```

Filename includes environment, database, UTC timestamp, Flyway schema version. SHA-256 sidecar written.

Production backup requires `ALLOW_PRODUCTION_BACKUP=YES`.

## Restore (disposable target only)

```powershell
.\scripts\deployment\restore_postgres.ps1 `
  -Dump <path.dump> `
  -TargetDb exotic_stamp_restore_tmp `
  -Confirm YES `
  -CreateDb
```

- Explicit target DB required
- Explicit `-Confirm YES`
- No DROP DATABASE
- Production restore requires `ALLOW_PRODUCTION_RESTORE=YES_I_UNDERSTAND`

## Rehearsal policy

Test only against local or isolated disposable databases — never production data.
