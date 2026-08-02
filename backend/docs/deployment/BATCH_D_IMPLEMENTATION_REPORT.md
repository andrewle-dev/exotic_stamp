# Batch D Implementation Report — S3 Storage, Container Packaging, Health, Caddy

**Date:** 2026-07-24  
**Audit companions:** `BACKEND_PRODUCTION_READINESS_AUDIT.md` v2, `BACKEND_REMEDIATION_PLAN.md` v2  
**Remediation IDs:** R-P0-06 / F-003,F-010; R-P0-07a / F-031; R-P0-07b / F-032; R-P0-07c / F-033; R-P0-07d / F-034  

---

## 1. Verdict

**PARTIAL**

| Item | Status |
|------|--------|
| R-P0-06 S3 storage | **DONE** (code + LocalStack IT scaffolding; live AWS ops remaining) |
| R-P0-07a Graceful shutdown | **DONE** |
| R-P0-07b JVM MaxRAMPercentage | **DONE** |
| R-P0-07c `.dockerignore` | **DONE** |
| R-P0-07d Caddy / network | **PARTIAL** (example + compose + runbook; host `caddy validate` + staging HTTPS smoke still required) |

This batch prepares deployment-ready artifacts. It does **not** claim production deployment completed.

---

## 2. Exact files changed (primary)

### Storage
- `infra/storage/**` (API, properties, ObjectKeyFactory, PublicUrlResolver, metrics)
- `infra/storage/s3/**` (S3ClientConfig, S3StorageService, health, exception mapper)
- `infra/storage/local/**` (LocalStorageService rewrite)
- `infra/storage/asset/**` (StoredAsset, lifecycle, orphan cleanup)
- `modules/metro/application/PublicAssetUploadService.java`
- `modules/metro/application/StationCommandService.java`
- `modules/metro/application/StationImagePointerService.java`
- `config/ProdStartupValidator.java`
- `db/migration/V21__stored_assets.sql`
- `pom.xml` (AWS SDK v2 s3 + url-connection-client; testcontainers localstack)

### Runtime / health
- `application.yml`, `application-prod.yml` (graceful shutdown, health groups, S3 props)
- `common/exceptions/storage/ConcurrentAssetReplaceException.java`
- `common/exceptions/GlobalExceptionHandler.java`

### Docker / Caddy
- `Dockerfile`, `.dockerignore`, `docker-compose.prod-like.yml`
- `infra/caddy/Caddyfile.example`
- `scripts/ci/graceful_shutdown_smoke.sh`, `.ps1`
- `.env.example`, `.env.prod-like.example`

### Docs / CI
- `docs/deployment/BATCH_D_IMPLEMENTATION_REPORT.md` (this file)
- `docs/deployment/LIGHTSAIL_NETWORK_AND_CADDY_RUNBOOK.md`
- `docs/deployment/S3_IAM_AND_BUCKET_POLICY_RUNBOOK.md`
- `docs/deployment/BACKEND_REMEDIATION_PLAN.md`
- `docs/deployment/BACKEND_ENVIRONMENT_MATRIX.md`
- `scripts/ci/expected_integration_tests.txt`

### Tests
- Unit: ObjectKeyFactory, AssetLifecycle, OrphanCleanup, S3ExceptionMapper, StationImageReplaceConsistency, HealthGroups, LocalStorage, ProdStartup*
- IT: `S3StorageLocalStackIT`, `FlywayV21MigrationIT`

---

## 3. S3 SDK and client choice

| Choice | Value |
|--------|-------|
| SDK | AWS SDK for Java **v2** `software.amazon.awssdk:s3:2.29.45` |
| HTTP | `url-connection-client` (bounded connect/socket timeouts) |
| Client | Single managed `S3Client` bean + `S3Presigner` bean |
| Per-request clients | **No** |

---

## 4. Credential provider design

- Production uses **`DefaultCredentialsProvider`** chain only.
- No access keys in YAML, Docker images, Compose, or Caddyfile.
- Static keys may arrive only via runtime secret delivery when that mechanism is explicitly selected.
- **Unresolved ops decision:** Lightsail product may not expose an EC2-style instance profile — operators must verify IAM role / temporary credential delivery for the selected Lightsail SKU (see S3 runbook).

---

## 5. Storage configuration

| Property / env | Purpose |
|----------------|---------|
| `STORAGE_PROVIDER` | `local` (dev/test) / `s3` (prod required) |
| `STORAGE_PUBLIC_BASE_URL` | Public URL base (CDN-compatible) |
| `AWS_S3_BUCKET` / `AWS_REGION` | Bucket + region |
| `AWS_S3_ENDPOINT` + `AWS_S3_PATH_STYLE` | LocalStack only |
| Timeouts / retries | Typed on `storage.s3.*` |
| Cleanup | `storage.cleanup.*` (retention, batch, dry-run, cron) |

Prod fail-fast when provider ≠ `s3` or bucket/region/public base URL missing.

---

## 6. Storage data model

Table `stored_assets` (V21):

- `provider`, `object_key` (unique), `content_type`, `byte_size`, `checksum`
- `visibility` PUBLIC/PRIVATE
- `status` PENDING / ACTIVE / ORPHANED
- `entity_type`, `entity_id`, `public_url`
- `created_at`, `orphaned_at`, `delete_after`

Legacy entity URL columns retained (`stations.image_url`, partner/campaign/stamp/milestone image URLs) widened to VARCHAR(512).

---

## 7. Flyway migration

- **V21__stored_assets.sql** — additive only; no edits to V1–V20.
- Later cleanup migration (drop legacy URL formats after backfill) documented as follow-up, not shipped.

---

## 8. Legacy URL compatibility

- Existing full URLs / local `/uploads/...` rows continue to work in API responses.
- New uploads store derived public URL = `STORAGE_PUBLIC_BASE_URL + "/" + object_key`.
- Orphan tracking extracts object key when URL matches configured base; otherwise logs and skips hard tracking.

---

## 9. Object-key convention

```
public/stations/{stationId}/cover/{uuid}.{ext}
public/stamp-designs/{designId}/{uuid}.{ext}
public/partners/{partnerId}/logo|banner/{uuid}.{ext}
public/campaigns/{campaignId}/{uuid}.{ext}
public/rewards/{milestoneId}/{uuid}.{ext}
public/temporary/{yyyy}/{mm}/{dd}/{uuid}.{ext}   # staged public admin uploads
private/users/{userId}/{uuid}.{ext}
```

Extension from detected format (not original filename). Path traversal rejected. Unique UUID keys (no overwrite).

---

## 10. Public / private policy

| Class | Access |
|-------|--------|
| Public business media under `public/*` | Permanent URL via `STORAGE_PUBLIC_BASE_URL` |
| Private under `private/*` | Presigned GET only; never arbitrary caller-supplied keys without authz |
| Bucket | Block public write; do not use object ACL `public-read` |
| CDN | Optional later; base URL remains compatible |

S3 health is a **separate** actuator group and is **not** part of readiness (API can serve while uploads temporarily fail).

---

## 11. Upload / DB consistency

| Sequence | Behavior |
|----------|----------|
| Validation fails | No PutObject; no DB mutation |
| S3 fails | No DB pointer; `STORAGE_WRITE_FAILED` |
| S3 OK, DB fails | PENDING metadata survives (`REQUIRES_NEW`); reconciliation orphans stale PENDING |
| Replace success | New ACTIVE; previous ORPHANED; **no immediate delete** |
| Concurrent replace | Pointer compare → `ConcurrentAssetReplaceException` (409); loser object orphaned |

PutObject and PostgreSQL commit are **not** one atomic transaction.

---

## 12. Replace / orphan lifecycle

- Retention default **14 days** (`STORAGE_ORPHAN_RETENTION`)
- `OrphanCleanupJob` bounded batch + duration; dry-run; refuses ACTIVE keys; shutdown-aware
- Metrics: upload success/failure, orphan created, cleanup success/failure, missing referenced

---

## 13. Cleanup / reconciliation design

Idempotent scheduled job (cron configurable). Never unbounded bucket scan on startup. Prefix/pagination via DB orphan rows primarily; ListBucket reserved for future ops tooling under least-privilege IAM.

---

## 14. LocalStack test results

`S3StorageLocalStackIT` present (`disabledWithoutDocker=true`). Executes under Failsafe when Docker available. Does not contact real AWS.

---

## 15–16. Surefire / Failsafe / coverage

| Suite | Result |
|-------|--------|
| Surefire (`mvn clean test`) | **526** tests, **0** failures, **0** skipped |
| Failsafe (`mvn clean verify -Pci`) | **36** tests, **0** failures, **0** errors, **0** skipped |
| LocalStack IT | **PASS** (3 tests) |
| Flyway V21 IT | **PASS** (2 tests) |
| JaCoCo overall | **LINE 53% / BRANCH 40%** — CI gate still fails (R-P1-10; unchanged thresholds) |
| Assert manifest | PASS under Failsafe |

JaCoCo failure is reported **independently** from Batch D storage/security gates.

---

## 17. Graceful shutdown

- `server.shutdown=graceful`
- Bounded `timeout-per-shutdown-phase` (default 30s)
- Orphan job sets shutting-down flag (`@PreDestroy`)
- Smoke: `scripts/ci/graceful_shutdown_smoke.*`
- No `System.exit()` in application code

---

## 18. Health / readiness

| Probe | Includes |
|-------|----------|
| Liveness | `livenessState` only |
| Readiness | `readinessState`, `db`, `redis` |
| Storage group | `s3Storage` (HeadBucket; not in readiness) |

Details: `when_authorized`. No credentials / JDBC secrets in health bodies.

---

## 19–21. Dockerfile / image / `.dockerignore`

- Multi-stage Java 21; non-root `app`; exec ENTRYPOINT; `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=75.0`
- HEALTHCHECK → `/actuator/health/readiness`; `STOPSIGNAL SIGTERM`
- No `.env` / `.git` / `.m2` / uploads in image context (`.dockerignore`)
- Digest pinning of base image: **follow-up** (versioned tags used)

---

## 22. Caddy syntax validation

Example file: `infra/caddy/Caddyfile.example` (Caddy **2.8+** for `request_body`).  
Validate on host:

```bash
docker run --rm -v "$PWD/infra/caddy/Caddyfile.example:/etc/caddy/Caddyfile:ro" caddy:2.8-alpine caddy validate --config /etc/caddy/Caddyfile
```

Staging HTTPS smoke still required before marking R-P0-07d complete.

---

## 23. Network boundary

Documented in `LIGHTSAIL_NETWORK_AND_CADDY_RUNBOOK.md`. Prod-like compose publishes **only** Caddy 80/443; backend/Postgres/Redis unpublished.

---

## 24. Lightsail / S3 credential decision

**Still requires operator validation** for the selected Lightsail product (instance role vs static secret delivery). Do not assume EC2 instance profile support.

---

## 25. Compatibility impact

- API success DTOs unchanged (`imageUrl` / `url` strings remain).
- Clients may see CDN-style URLs instead of `/uploads/...` after cutover.
- Batch C upload validation unchanged and still runs before storage write.

---

## 26. Data migration risk

**Medium.** Additive schema is safe. Legacy URLs remain readable. Optional later backfill to `stored_assets` + CDN rewrite.

---

## 27. Rollback plan

1. Set `STORAGE_PROVIDER=local` only in non-prod emergency (prod validator blocks local).
2. For prod rollback of app binary: redeploy previous JAR; V21 is additive and forward-compatible.
3. Do not delete S3 objects during rollback; orphans remain recoverable.

---

## 28. External AWS actions still required

- [ ] Create prod/dev/test buckets (separate)
- [ ] Apply least-privilege IAM + bucket policy (`public/*` read only if used)
- [ ] Encryption at rest; lifecycle for temporary/orphans; versioning decision
- [ ] Confirm Lightsail credential mechanism
- [ ] Set `STORAGE_PUBLIC_BASE_URL` (bucket website or CDN)
- [ ] Rotate any previously exposed credentials (Batch A ops still open)

---

## 29. Remaining P0/P1 blockers

- R-P0-05 CI governance / green GHA (Batch B/B.1)
- R-P1-10 JaCoCo gates
- R-P0-07d host Caddy validate + staging smoke
- Lightsail↔S3 credential proof
- Prior secret rotation (R-P0-01 ops)

---

## 30. Explicitly unchanged

- NFC lifecycle, collection idempotency, reward processing, voucher allocation
- Authentication / JWT semantics, rate-limit policies
- Upload content-validation rules (Batch C)
- API success DTO shapes
- Existing Flyway V1–V20
- JaCoCo thresholds

---

## Verification

| Check | Result |
|-------|--------|
| `mvn clean test` | **PASS** — 526 / 0 / 0 |
| `mvn clean verify -Pci` Failsafe | **PASS** — 36 / 0 / 0 |
| `mvn clean verify -Pci` JaCoCo gate | **FAIL** — 53%/40% (pre-existing R-P1-10) |
| LocalStack IT | **PASS** |
| Prod rejects local storage | **PASS** |
| Caddyfile `caddy validate` (2.8-alpine) | **PASS** — Valid configuration |
| Deployment completed? | **No** — artifacts only |
