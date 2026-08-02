# Batch F.0 Implementation Report

**Batch:** F.0 — Deployment Preparation, Release Engineering, Staging Runbook Closure  
**Date:** 2026-07-25  
**Baseline commit:** `ba6cb60`  
**Release version prepared:** `0.1.0-rc.1` (not published as a public production tag)  
**STOP POINT honored:** No live AWS resources created. No production deploy.

---

## 1. Verdict

**PASS — deployment preparation complete locally. R-P0-05 remains PARTIAL (no observed green GitHub Actions run).**

Local release engineering, prod-like boot, backup/restore rehearsal, deploy/rollback dry-runs, Caddy validation, and smoke (mandatory public checks) completed.  
**R-P0-05 remains PARTIAL** — `gh` CLI unavailable; no observed green GitHub Actions run URL/ID in this session.  
Branch protection remains documentation-only.

Do **not** treat this batch as production deployment complete.

---

## 2. Exact files changed (F.0 scope)

### Workflows / config
- `.github/workflows/backend-ci.yml` — PR targets, quality-branch pushes, CI diagnostics artifact
- `.env.example`, `.env.prod-like.example` — placeholders, APP_ENV, DB_HOST/PORT/NAME, delivery notes
- `.gitignore` — `artifacts/**`, staging/production env names
- `src/main/resources/application-prod.yml` — **JTE prod packaging fix** (`use-precompiled-templates`)
- `src/test/java/.../IntegrationTestSupport.java` — align prod IT JTE props with prod profile
- `src/test/java/.../RewardReconcileAdvisoryLockIT.java` — latch race fix (test-only)

### Release scripts
- `scripts/release/generate_release_manifest.{py,sh,ps1}`
- `scripts/release/build_image.{sh,ps1}`
- `scripts/release/inspect_image.{sh,ps1}`
- `infra/release/release-manifest.example.json`

### Deployment / smoke scripts
- `scripts/deployment/run_db_preflight.{sh,ps1}`
- `scripts/deployment/backup_postgres.{sh,ps1}`
- `scripts/deployment/verify_backup.{sh,ps1}`
- `scripts/deployment/restore_postgres.{sh,ps1}`
- `scripts/deployment/deploy_staging.{sh,ps1}`
- `scripts/deployment/rollback_staging.{sh,ps1}`
- `scripts/smoke/staging_smoke.{sh,ps1}`

### Infra
- `infra/caddy/Caddyfile.example` (forwarded headers + liveness)
- `infra/caddy/Caddyfile.staging.example`
- `infra/caddy/Caddyfile.local.example`
- `docker-compose.prod-like.local.yml`

### Docs
- `docs/deployment/RELEASE_VERSIONING_POLICY.md`
- `docs/deployment/STAGING_ENVIRONMENT_CHECKLIST.md`
- `docs/deployment/SECRET_ROTATION_AND_DELIVERY_RUNBOOK.md`
- `docs/deployment/AWS_STAGING_RESOURCE_PLAN.md`
- `docs/deployment/F0_LOCAL_PROD_LIKE_VALIDATION.md`
- `docs/deployment/DATABASE_MIGRATION_PREFLIGHT_RUNBOOK.md`
- `docs/deployment/POSTGRES_BACKUP_RESTORE_RUNBOOK.md`
- `docs/deployment/STAGING_SMOKE_TEST_PLAN.md`
- `docs/deployment/STAGING_RELEASE_CHECKLIST.md`
- `docs/deployment/BATCH_F0_IMPLEMENTATION_REPORT.md` (this file)
- Updates: `BACKEND_REMEDIATION_PLAN.md`, `BACKEND_ENVIRONMENT_MATRIX.md`, `GITHUB_BRANCH_PROTECTION_CHECKLIST.md`

---

## 3. GitHub Actions evidence

| Field | Value |
|-------|-------|
| Repository | `itdept-studio/EXOTIC_STAMP` (remote `exotic`) |
| Workflow | `.github/workflows/backend-ci.yml` |
| Observed run ID / URL | **Not observed** (`gh` not installed; no Actions UI capture this session) |
| Conclusion | **Unverified** |

### Operator commands to close R-P0-05

```bash
git push -u exotic HEAD:chore/batch-f0-deployment-prep
# or open PR to main
gh run list --repo itdept-studio/EXOTIC_STAMP --workflow=backend-ci.yml --limit 5
gh run view <run-id> --repo itdept-studio/EXOTIC_STAMP
```

UI: GitHub → Actions → Backend CI → confirm green on the pushed SHA.

---

## Local quality gate (post-F.0)

| Check | Result |
|-------|--------|
| mvn -B -ntp clean verify -Pci | BUILD SUCCESS |
| Surefire | 855 / 0 fail / 0 skip |
| Failsafe | 64 / 0 fail / 0 skip |
| ssert_test_reports.py --strict | ASSERTION OK |
| JaCoCo summary / critical | PASS |

## 4. R-P0-05 status

**PARTIAL** — local Failsafe/Surefire/JaCoCo tooling present; remote green Actions run not proven in F.0.

---

## 5. Release versioning design

See `RELEASE_VERSIONING_POLICY.md`. Pre-prod: `v0.1.0-rc.N`. Docker: `v<version>` + `git-<short-sha>`. Rollback via digest or immutable tag. No `latest`-only releases. No public production tag created.

---

## 6. Release manifest result

Generated: `artifacts/release/release-manifest-ba6cb60.json` (gitignored under `artifacts/**`).  
Example schema committed: `infra/release/release-manifest.example.json`.  
Version: `0.1.0-rc.1`, Flyway `23`, digest recorded after local build.

---

## 7. Docker image build result

| Item | Value |
|------|-------|
| Tags | `exotic-stamp-backend:v0.1.0-rc.1`, `exotic-stamp-backend:git-ba6cb60` |
| Build | PASS (`build_image.ps1 -AllowDirty`) |
| Local digest / image id | `sha256:f60279718b1a248d6a35bfe98651c684d2b0a860f7be712a3cda57614ee334ee` (local build metadata; registry digest unresolved until push) |

---

## 8. Non-root / image inspection

**INSPECT PASS** — user `app` (non-root); `/app/app.jar` present; forbidden path shallow scan clean.

---

## 9. Prod-like stack result

**PASS** after JTE packaging fix + non-localhost FRONTEND/BACKEND URLs.  
Profile `prod`, Flyway v23, readiness/liveness UP via Caddy `:80`, backend port unpublished, LocalStack profile used for S3 substitute. Details: `F0_LOCAL_PROD_LIKE_VALIDATION.md`.

---

## 10–11. Environment / secret inventory

See `STAGING_ENVIRONMENT_CHECKLIST.md` and `SECRET_ROTATION_AND_DELIVERY_RUNBOOK.md`.  
Spring does **not** auto-load `.env`; Compose `env_file` / systemd `EnvironmentFile` required.

---

## 12–13. AWS staging plan / Lightsail sizing

See `AWS_STAGING_RESOURCE_PLAN.md`. Layouts A (cost-minimized all-in-one) and B (production-like separation). **No resources created.**

---

## 14–15. Database preflight / backup

Preflight SQL executed read-only against local prod-like DB (Flyway 23).  
Backup: custom-format dump + SHA-256. Verify: `pg_restore --list` PASS.

---

## 16. Restore rehearsal

Restored into disposable `exotic_stamp_restore_tmp` (no DROP DATABASE). Flyway history row count **23**. PASS.

---

## 17–18. Deploy / rollback dry-runs

`deploy_staging.ps1 -DryRun` → exit 0.  
`rollback_staging.ps1 -DryRun` → exit 0 (Flyway reverse forbidden, documented).

---

## 19. Staging smoke (local)

**SMOKE PASS** — liveness, readiness, swagger 404, invalid login 401, rate-limit 429 + Retry-After.

---

## 20. Caddy validation

Pinned `caddy:2.8-alpine`: example, staging, and local Caddyfiles → **Valid configuration**.

---

## 21. Remaining AWS user actions (F.1)

1. Open AWS Console (non-prod account) → choose region.  
2. Create Lightsail instance/container + static IP + firewall (80/443/22 only).  
3. Create staging S3 bucket + IAM least privilege + credential delivery decision.  
4. DNS `api-staging.<domain>`.  
5. Provision PostgreSQL/Redis per layout A or B.  
6. Inject secrets via `/etc/exotic-stamp/staging.env` or Compose `env_file` (never git).

**Exact Console open point:** AWS sign-in → region selector → **Lightsail** create + **S3** create (see plan §).

---

## 22. Remaining blockers

| Blocker | Status |
|---------|--------|
| Green GitHub Actions on exotic | Open (R-P0-05) |
| Branch protection applied | Open (docs only) |
| Lightsail↔S3 credential mechanism | Unresolved ops decision |
| Live AWS staging resources | Not created (by design) |
| Clean git tree for official release | Working tree still contains prior batch work |

---

## 23. Explicitly unchanged production semantics

- NFC lifecycle, collection, reward/voucher business rules, idempotency semantics: **unchanged**
- JWT/OTP/rate-limit behavior: **unchanged**
- S3 object lifecycle semantics: **unchanged**
- API success DTO shapes: **unchanged**
- Flyway migrations **V1–V23**: **not modified**
- JaCoCo thresholds: **unchanged**

### Allowed production packaging fix

`application-prod.yml` JTE `use-precompiled-templates: true` — required for Docker `prod` profile boot (confirmed defect during F.0 local validation).

---

## Data integrity audit (Phase 15)

| Invariant | Status |
|-----------|--------|
| Preflight blocks dirty reward/voucher data | Scripts fail on launch-blocking findings |
| Backup before migration deploy | Deploy script order: preflight → backup → image |
| Failed upload leaves no DB pointer | Existing S3 semantics; smoke plan documents |
| Failed new container does not replace healthy | Deploy keeps prior image; success after smoke |
| Rollback does not down-migrate | Explicitly forbidden in rollback scripts |
| Manifest identifies source/image | Generator fields required |
| Staging/prod secrets not mixed | APP_ENV/target guards; naming templates |
| Scripts reject production by default | Deploy/rollback/restore guards |
| Logs redact tokens/vouchers | Smoke redaction helpers |

No production AWS contact performed.
