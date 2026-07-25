# Exotic Stamp Backend — Production Readiness Audit (v2)

**Audit version:** 2  
**Audit date:** 2026-07-22 (v1) / **Revised:** 2026-07-23 (v2)  
**Scope:** `D:/Part-time/ExoticStamp/backend` (nested git root) within monorepo `D:/Part-time/ExoticStamp`  
**Mode:** Audit-only documentation correction — no application source, migrations, tests, `pom.xml`, Docker, or workflows modified  
**Production target:** AWS Lightsail + Caddy (TLS) + PostgreSQL + Redis + Amazon S3  
**Expected scale:** 3,000–5,000 registered users  

### v2 change log (mandatory corrections)

| Change | Detail |
|--------|--------|
| Removed | **F-017** (single-ACTIVE NFC unique) and remediation **R-P1-04** — contradicts OpenAPI: multiple ACTIVE NFC keys per station are allowed for multiple physical gates |
| Replaced NFC guidance | Correct lifecycle invariants (unique hash, transitions, one-time raw payload, concurrent activate, install/gate metadata, audit). **QR policy treated separately** |
| F-005 | Downgraded **BLOCKER → MEDIUM** — parent monorepo `.gitignore` applies to `backend/.env` and `backend/uploads/**` |
| F-004 | Reclassified **BLOCKER → HIGH**; P0 **release-governance** gate (not a runtime source defect) |
| F-010 | Split into F-010, F-031, F-032, F-033, F-034 |
| CI | Failsafe / explicit `*IT` execution; fail on Docker-disabled Testcontainers — not total skipped count alone |
| Rate limit | Composite keys + `Retry-After` |
| Redis | Per-flow policy matrix |
| Reward side effect | Compare sync / async+reconcile / outbox; MVP recommendation |
| ENV | Canonical names only; JWT ≥32 random bytes before Base64; topology-dependent DB SSL; isolated S3 per env |
| Lightsail + S3 creds | Infrastructure decision — do not assume EC2 instance profile |
| Assets | Delayed orphan lifecycle; URLs from `object_key` + `STORAGE_PUBLIC_BASE_URL` |
| Added | **F-037** tracked `.m2` (and similar) in backend git index |

---

## 1. Executive verdict

| Field | Value |
|-------|-------|
| **Verdict** | **NOT READY** |
| **Confidence** | **HIGH** |

Runtime and secret issues still block Internet-facing and real-user launch. CI absence is a **governance** blocker for production release, not a runtime code defect. Multiple ACTIVE NFC keys per station are **by design**.

Successful `mvn clean test` / `verify` ≠ production readiness. No pentest or load test was executed.

---

## 2. Confidence level

**HIGH** for source/config evidence and local Maven runs.  
**Lower** for: Lightsail↔S3 credential mechanism (unvalidated), live bucket policies, remote secret exposure history, Docker-enabled CI IT results.

---

## 3. Build and test evidence

Unchanged from v1 evidence capture:

| Check | Result |
|-------|--------|
| Backend git toplevel | `D:/Part-time/ExoticStamp/backend` |
| Parent git toplevel | `D:/Part-time/ExoticStamp` |
| Java / Maven / Boot | 21.0.7 / 3.9.16 / 3.3.5 |
| `mvn clean test` | BUILD SUCCESS — 441 run, 0 fail, **4 skipped** (Docker missing) |
| `mvn clean verify` | BUILD SUCCESS — same Surefire run; **no Failsafe `*IT` split configured** |
| JaCoCo | Not configured |
| `.github/workflows` | Absent in backend tree |

**v2 ignore verification (mandatory):**

```text
# From parent monorepo root D:/Part-time/ExoticStamp
git check-ignore -v backend/.env
→ .gitignore:12:.env    backend/.env

git check-ignore -v backend/uploads/example
→ .gitignore:32:backend/uploads/**    backend/uploads/example
```

```text
# From nested backend git root D:/Part-time/ExoticStamp/backend
git check-ignore -v .env            → not ignored (exit 1)
git check-ignore -v uploads/example → not ignored (exit 1)
```

**Tracked junk (mandatory):** `git ls-files` in backend lists **`.m2/repository/...`** (index still contains ~2086 paths; working tree shows mass `D` deletions). `.gitignore` lists `.m2/` but files remain versioned until removed from the index.

---

## 4. Findings summary table

| ID | Severity | Area | Finding | Evidence | Production impact | Required action |
|----|----------|------|---------|----------|-------------------|-----------------|
| F-001 | BLOCKER | Secrets | Secret defaults in `application-dev.yml` | L31–32, L84–85 (redacted) | Token forgery / mailbox abuse | Rotate; remove defaults |
| F-002 | BLOCKER | Data | Unscoped seeders mutate all profiles | `MetroLineSeeder`; `CollectionBootstrapper` | Silent prod data writes | Profile/flag gate |
| F-003 | BLOCKER | Storage | S3 stub; production target unmet | `S3StorageService` throws; default `local` | No multi-node/CDN-ready media | Implement S3 or explicit single-node acceptance |
| F-004 | **HIGH** | CI/CD | No GitHub Actions; release unverifiable | No `.github/workflows` | Governance risk before prod | P0 pipeline + Failsafe IT gates |
| F-005 | **MEDIUM** | Secrets hygiene | Nested backend `.gitignore` omits `.env`/uploads; parent monorepo **does** ignore them | Parent check-ignore evidence above | Accidental commit if using nested repo alone | Align backend `.gitignore`; prefer monorepo workflow |
| F-006 | CRITICAL | Security | No HTTP rate limiting (Bucket4j unused) | `pom.xml`; `SecurityConfig` public auth/scan | Brute force / scan abuse | Composite-key limits + `Retry-After` |
| F-007 | CRITICAL | Security | Redis outage weakens controls without per-flow matrix in code | `AUTH_REDIS_UNAVAILABLE_BEHAVIOR.md`; OTP fail-open | Abuse when Redis down | Enforce matrix (OTP fail-closed) |
| F-008 | CRITICAL | Integrity | No unique on `user_rewards.voucher_pool_id` | V4/V16 schema | Double-link risk | Partial unique |
| F-009 | CRITICAL | Integrity | Stamp commit without reliable reward completion | Async listeners; no durable reconcile | Missing rewards | MVP: async + idempotent reconciliation (see §13) |
| F-010 | HIGH | Storage | Local uploads as primary production storage risk | `storage.provider=local`; `/uploads/**` | Data loss on replace/multi-instance | S3 primary; see F-003 |
| F-011 | HIGH | Config | Machine-specific `D:/Part-time/...` default path | `application.yml` L174 | Broken deploys | Env-only paths |
| F-012 | HIGH | Security | `GENERIC` upload skips magic-byte checks | `FileValidator` L53–56 | MIME spoof | Always validate bytes |
| F-013 | HIGH | Security | JWT issuer not verified on parse; no audience | `JwtProvider.extractClaims` | Claim hygiene | Require issuer |
| F-014 | HIGH | Security | Short JWT secret padded via SHA-256 | `JwtProvider.secretKey` L137–146 | Weak secrets accepted | Require ≥32 random bytes before encoding |
| F-015 | HIGH | Config | Prod CORS/URL/cookie fail-fast incomplete | prod yml vs Vercel | Admin auth breakage | Canonical ENV + validation |
| F-016 | HIGH | DB | Idempotency window vs permanent unique | yml 1h; V15 unique | Retry semantics confusion | Align + map DIV to replay |
| ~~F-017~~ | — | — | **REMOVED in v2** (single-ACTIVE NFC) | OpenAPI allows multiple ACTIVE NFC | — | See F-036 |
| F-018 | HIGH | Ops | Unstructured logs; no correlation ID | `logback-spring.xml` | Hard incident response | JSON + request ID |
| F-019 | HIGH | Test | ITs skip without Docker; no Failsafe split; no coverage gate | Surefire skipped=4; no Failsafe | False release confidence | Failsafe + Docker-required CI |
| F-020 | MEDIUM | Architecture | JPA on domain (ADR-allowed) | ADR-001 | Long-term purity | Accept or split later |
| F-021 | MEDIUM | Security | Swagger paths always `permitAll` | `SecurityConfig` L45–50 | Docs leak if re-enabled | Non-prod only |
| F-022 | MEDIUM | Security | CSRF off; Origin filter mitigates cookies | `SecurityConfig` L80 | Residual CSRF | Keep + document Vercel SameSite |
| F-023 | MEDIUM | Deps | Unused Cloudinary / MySQL / (Bucket4j until used) | `pom.xml` | Confusion / surface | Remove or use |
| F-024 | MEDIUM | DB | Default-campaign unique ignores soft-delete | V9 partial unique | Ops stuck | Soft-delete-aware unique |
| F-025 | MEDIUM | Auth | Denylist/refresh Redis paths fail-open | Redis repos + docs | Revoked JWT until expiry | Follow matrix in F-007 |
| F-026 | — | — | **Superseded by F-033** (`.dockerignore`) | — | — | See F-033 |
| F-027 | MEDIUM | Perf | No explicit pool sizing | yml defaults | Unvalidated under load | Tune after staging load test |
| F-028 | LOW | Hygiene | Issuer leftover `metricsX` | `application.yml` L112 | Confusion | `exotic-stamp` |
| F-029 | LOW | Config | Unused cache TTL keys (`sales`, `booking`) | `application.yml` | Noise | Clean |
| F-030 | INFO | Docs | Known prod gaps (pentest, proxy) | docs | Awareness | Track |
| F-031 | MEDIUM | Deploy | No `server.shutdown=graceful` | `application*.yml` | In-flight request loss on restart | Enable graceful shutdown |
| F-032 | MEDIUM | Deploy | No JVM/container memory limits in image entrypoint | `Dockerfile` ENTRYPOINT | OOM risk on Lightsail | `-XX:MaxRAMPercentage` / memory |
| F-033 | MEDIUM | Deploy | No `.dockerignore` | Missing file | Fat/risky build context | Add ignore |
| F-034 | MEDIUM | Deploy | Caddy/runtime TLS proxy config not in backend repo | No Caddyfile; prod forward-headers present | Misconfigured cookies/HTTPS | Ops Caddy checklist |
| F-035 | — | reserved | — | — | — | — |
| F-036 | MEDIUM | NFC | Residual NFC lifecycle gaps vs OpenAPI invariants (QR separate) | OpenAPI tag; `StationScanKeyCommandService`; V18 | Ops/security edge cases | Enforce invariants below |
| F-037 | HIGH | Repo hygiene | `.m2` (and historically IDE/AI paths) versioned in backend git | `git ls-files` `.m2/...`; ~2086 index paths | Repo bloat; accidental binary leak | `git rm -r --cached .m2`; keep ignore |

---

## 5. Detailed findings (selected)

### F-001 — Committed secret defaults (BLOCKER) — confirmed defect

Unchanged: JWT/SMTP/DB defaults in `application-dev.yml` must be rotated and removed.

### F-004 — Missing CI/CD (HIGH, P0 governance) — confirmed process gap

Not a runtime source defect. **Mandatory before production release** as release governance: no automated compile/IT/coverage/deploy gates. See remediation R-P0-05 (Failsafe + Docker-required ITs).

### F-005 — Ignore hygiene (MEDIUM) — design risk / nested-repo gap

Parent monorepo rules **do apply** and ignore `backend/.env` and `backend/uploads/**`. Downgraded from BLOCKER. Residual risk: nested `backend` git root does not ignore `.env`/`uploads` if developers commit from that root alone.

### F-006 — Rate limiting (CRITICAL) — confirmed defect

Public auth and `POST /api/v1/metro/scan/resolve` lack application rate limits. Remediation must use **endpoint-specific composite keys**, not IP-only (see remediation plan).

### F-007 — Redis outage (CRITICAL) — confirmed / documented

OTP cooldown/attempts currently fail-open when Redis is down. Remediation is a **per-flow policy matrix** (OTP issue/verify fail-closed; public cache may fall back to DB; token revocation/refresh follow explicit policies). See §12 and ENV/remediation docs.

### F-009 — Reward completion (CRITICAL) — confirmed defect

Options compared in §13. For 3k–5k MVP: prefer **async + idempotent reconciliation** as minimum reliable approach; transactional outbox is optional stronger form.

### F-010 / F-031–F-034 — Former F-010 split

| ID | Topic |
|----|--------|
| F-010 | Local production uploads / missing production object storage posture |
| F-031 | Graceful shutdown |
| F-032 | JVM/container memory |
| F-033 | Missing `.dockerignore` |
| F-034 | Caddy/runtime configuration |

### F-017 — REMOVED

OpenAPI (`docs/api/openapi.json`, tag **Admin Station Scan Keys**):

> Multiple ACTIVE NFC keys per station are allowed (e.g. multiple gates).  
> `payloadToWrite` is returned only once at creation.

Activate description: only DRAFT/INACTIVE → ACTIVE; multiple ACTIVE NFC allowed.

### F-036 — NFC lifecycle vs correct invariants (MEDIUM)

**Correct NFC invariants (contract + schema):**

| Invariant | Current evidence | Gap? |
|-----------|------------------|------|
| Globally unique `key_hash` | V18 `uq_station_scan_keys_key_hash` | OK |
| Valid state transitions | `canActivate()`, terminal checks, `ScanKeyAlreadyActiveException` | Partial — ensure all transitions covered in domain |
| Raw payload returned once | `create` → `StationScanKeyCreatedView` with `payloadToWrite`; list/get metadata only per OpenAPI | OK if clients never re-fetch raw (server cannot re-issue) |
| Safe concurrent activation | Entity `@Version`; optimistic lock → 409 | OK for same key; multi-ACTIVE across keys is intentional |
| Installation / gate metadata | `label`, `placementNote`, install GPS/device fields on verify | Prefer requiring gate label for multi-gate stations (recommendation) |
| Audit trail | `MetroAuditHelper` on create/activate/revoke/lost/verify | Strengthen retention/queryability (recommendation) |
| `replaced_by_id` | Column exists; activate does not set replacement chain | Design: optional for NFC multi-gate; use for intentional replace flows |

**QR policy (separate):** `QR_STATIC` / `QR_DYNAMIC_PLACEHOLDER` must not inherit NFC multi-gate assumptions blindly. Static QR may be single logical poster per station; dynamic placeholder needs its own rotation/expiry rules. Do **not** enforce “one ACTIVE QR” without an explicit product decision — document and test separately from NFC.

### F-037 — Tracked `.m2` (HIGH) — confirmed defect

Backend index still versions Maven local-repo artifacts under `.m2/`. Working tree deletions do not remove history/index until `git rm --cached`. IDE/AI folders are gitignored in backend; parent may differ — `.m2` is the confirmed versioned junk.

---

## 6. Architecture violations

Unchanged summary: presentation→application→domain←infrastructure largely respected; ArchUnit green; JPA-on-domain ADR-allowed design risk; no controller→repository.

---

## 7. Hardcode and magic-value inventory

Unchanged material findings (secrets, GPS 150 triplication, Cloudinary logos, `metricsX`, Windows path). Dev secret defaults remain classification **7/4**.

---

## 8. Secret exposure findings

| Item | Classification |
|------|----------------|
| `application-dev.yml` defaults | **Rotate immediately** |
| Parent `.env` | gitignored at monorepo; local-only |
| Nested backend `.env` ignore gap | **should be gitignored** (MEDIUM F-005) |
| Tracked `.m2` | **should not be versioned** (F-037) |
| Historical `k8s/secret.yaml` | rotate/purge candidate |
| GitHub Secrets | for CI when added |
| Lightsail | server env / validated secret delivery — **not** assumed instance profile |

---

## 9. Environment / profile findings

See `BACKEND_ENVIRONMENT_MATRIX.md` v2 — canonical names only; JWT strength definition; topology-dependent `DB_SSL_MODE`; separate S3 buckets/isolation for dev/test/prod.

---

## 10. S3 findings

| Topic | v2 position |
|-------|-------------|
| Implementation | Stub — BLOCKER for stated AWS S3 target |
| Credentials on Lightsail | **Infrastructure decision requiring validation** — Lightsail may not provide EC2-style instance profiles; options include IAM user keys in server secret store, Lightsail containers credential docs, or other AWS-supported patterns — **verify before choose** |
| Buckets | Separate buckets (or strict prefix + IAM isolation) for **dev**, **test**, **prod** |
| Public URLs | Derive from `object_key` + `STORAGE_PUBLIC_BASE_URL` (not hardcoded hostnames) |
| Replace behavior | Mark prior object **orphaned**; delete via **delayed lifecycle**, not immediate delete |
| Never embed AWS keys in image | Still required |

---

## 11. Database / Flyway findings

- V1–V20 present; `ddl-auto: validate` in dev/prod — good  
- Keep: `uq_user_stamps_collect`, `uq_user_rewards_once`, voucher code unique, `uq_station_scan_keys_key_hash`  
- **Do not** add single-ACTIVE NFC unique  
- Gaps remain: `user_rewards.voucher_pool_id` unique; soft-delete default campaign; idempotency policy alignment  

---

## 12. Security findings

### Redis per-flow policy matrix (target)

| Flow | Redis role | Required outage behavior |
|------|------------|--------------------------|
| OTP **issue** (forgot / verify resend) | Store OTP + cooldown | **Fail closed** — do not send OTP if store fails |
| OTP **verify** / reset | Read OTP + attempts | **Fail closed** — reject verify if Redis unavailable or miss |
| Public cache reads (stations, stamp-book cache) | Cache aside | **May fall back to DB** |
| Access token denylist | Fast revoke | Documented: fail-open to DB `tokenVersion` SoT **or** fail-closed if denylist-only revoke used — choose explicitly; default docs today fail-open denylist |
| Refresh reuse / grace | Grace cache | DB pessimistic lock remains SoT; grace miss may fail concurrent retry — document |
| Refresh known-revoked Redis hint | Hint only | Must not override DB session validity |
| Rate limits | Counter store | Prefer fail-closed limits or edge (Caddy) limits when Redis down |

Current code/docs: OTP cooldown/attempts fail-open — **defect relative to this matrix**.

### Rate limiting (target design)

Endpoint-specific **composite keys**, examples:

| Endpoint class | Key dimensions |
|----------------|----------------|
| Login | IP + email-hash |
| Register | IP + email-hash |
| OTP issue | IP + email-hash |
| OTP verify | IP + email-hash + challenge/OTP id |
| Refresh | IP + user id (if known) + device fingerprint |
| Collect | user id + idempotency key + IP |
| Scan resolve | IP + raw challenge/key prefix hash |

Responses: HTTP **429** with **`Retry-After`** (seconds).

---

## 13. Data integrity and race-condition findings

Collect / voucher / refresh analysis from v1 retained (except single-ACTIVE NFC).

### F-009 — Reward issuance reliability options

| Option | Pros | Cons | Fit for 3–5k MVP |
|--------|------|------|------------------|
| **A. Synchronous reward issuance** in collect TX | Strong consistency | Longer collect latency; couples domains; harder retries | Acceptable if kept thin |
| **B. Async + idempotent reconciliation** | Fast collect; `uq_user_rewards_once` safety; scheduled re-eval of stamps missing rewards | Brief lag; needs job + metrics | **Recommended minimum** |
| **C. Transactional outbox** | Durable event delivery | More schema/ops | Stronger; optional upgrade from B |

**v2 recommendation:** Implement **B** first (reconcile job keyed by stamp/user/campaign/milestone idempotency). Treat **C** as evolution of B if lag/ops pain appears. Do not require full outbox before MVP if B is solid.

---

## 14. Performance findings

Unchanged: no load tests run; JaCoCo ≠ performance; pool defaults unvalidated. Staging scenario plan remains in remediation doc.

---

## 15. Testing and coverage findings

| Issue | v2 requirement |
|-------|----------------|
| `*IT` may only run via Surefire accidentally | Configure **Failsafe** (or surefire includes) so `*IT` run on `mvn verify` |
| Docker missing | **Fail** release job when Testcontainers disabled/skipped — do **not** rely only on total skipped count |
| Gate | Assert **non-zero** integration tests executed |
| JaCoCo | Still absent — add with critical-path thresholds |

---

## 16. Deployment / Caddy findings

Split across F-010, F-031–F-034. Prod `forward-headers-strategy: framework` helps Caddy TLS termination. Caddyfile still not authored (ops follow-up). Required constraints unchanged: internal bind, forwarded proto, edge blocks for swagger, rate limits optional at edge.

**Lightsail + S3 credentials:** do **not** assume EC2 instance profile. Validate the exact mechanism for Lightsail (VM vs containers) before implementation.

---

## 17. CI/CD findings

F-004 HIGH governance. Pipeline must: PR verify with Failsafe ITs, Docker required, coverage optional but recommended, no prod deploy from PR, manual prod approval later.

---

## 18. Observability findings

F-018 retained. Never log OTP, JWT, refresh cookies, raw NFC, voucher codes, AWS secrets.

---

## 19. Asset ownership summary

See `BACKEND_ASSET_OWNERSHIP_MATRIX.md` v2 — delayed orphan deletion; public URL = `STORAGE_PUBLIC_BASE_URL` + `object_key`.

---

## 20. Items not verifiable from source alone

Lightsail↔S3 credential mechanism; live IAM/bucket policies; remote secret history; Docker CI IT results; load/pentest; whether nested vs parent git is the team’s commit path; QR product policy finalization.

---

## 21. Explicit go-live blockers

**Runtime / security (must fix before Internet or real users):**

1. F-001 secret rotation/removal  
2. F-002 seeder gating  
3. F-003 / F-010 production object storage posture  
4. F-006 composite rate limits  
5. F-007 Redis per-flow fail-closed OTP  
6. F-008 / F-009 / F-011 / F-015 critical integrity & prod config  

**Release governance (P0 before production launch, not runtime code defect):**

7. F-004 CI/CD with Failsafe + Docker-required ITs  
8. F-019 IT execution guarantees  
9. F-037 stop shipping/tracking `.m2` in git  

---

## 22. Final recommendation

**NOT READY.** Complete P0 runtime fixes and P0 governance CI before any production claim. Re-audit after P0+P1.

**No application source or existing engineering config was modified in this v2 documentation pass.**

## Batch E.2 note (2026-07-25)

Local quality gates closed under Batch E.2: JaCoCo overall/auth/collection/reward thresholds met; Failsafe ITs include advisory-lock, pending-stock concurrency, and notification dedup proofs. GitHub Actions green run on remote remains the open R-P0-05 item. See BATCH_E2_IMPLEMENTATION_REPORT.md and GITHUB_BRANCH_PROTECTION_CHECKLIST.md.
