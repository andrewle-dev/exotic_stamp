# 15 — Release Plan: Exotic Stamp / Metro Stamp

> Status: Draft v0.1  
> Owner: Tech Lead / DevOps / QA / Founder  
> Purpose: Define release gates, deployment process, rollback strategy, smoke tests, and post-release monitoring for MVP.

---

## 1. Feasibility Check

A controlled MVP release is feasible if the team treats release as an operational event, not just `mvn clean package` and deploy.

Exotic Stamp has physical-world dependencies. A backend release can affect:

- real NFC/QR scans at stations;
- reward/voucher issuance;
- partner reporting;
- user trust;
- ad/revenue tracking.

Therefore, every release must have migration checks, smoke tests, rollback steps, and monitoring.

---

## 2. Release Types

| Release Type | Description | Required Gate |
|---|---|---|
| Dev build | Local/internal development build. | Compile + unit tests. |
| Staging release | Integration environment for QA/mobile/admin. | Full backend regression + migration. |
| Release candidate | Candidate for production. | Staging E2E + device test + security smoke. |
| Production release | Real users and real station data. | Founder/Tech/QA approval. |
| Hotfix | Urgent production fix. | Narrow regression + rollback plan. |

---

## 3. Pre-Release Checklist

### 3.1 Product gate

- P0 scope confirmed.
- Open P0 decisions resolved.
- Release notes written.
- Known limitations documented.
- Operational owner assigned for launch window.

### 3.2 Backend gate

- `mvn clean test` passes.
- `mvn clean package` passes.
- Flyway migrations run from clean DB in CI/staging.
- No local/dev secrets committed.
- Production profile does not enable unsafe defaults.
- Swagger disabled/protected in production.
- CORS reviewed.
- Rate limits configured for auth/scan endpoints.
- Admin permissions verified.

### 3.3 Database gate

- Migration order verified.
- Backup exists before production migration.
- New constraints/indexes reviewed for lock impact.
- Rollback or forward-fix plan documented.
- Seed data verified for default campaign/stations/milestones.

### 3.4 Mobile gate

- Android release build generated.
- NFC tested on real devices.
- QR fallback tested.
- GPS permission states tested.
- Token refresh behavior tested.
- API base URL points to correct environment.
- Crash reporting/logging enabled if available.

### 3.5 Admin gate

- Admin can log in.
- Admin can manage line/station.
- Admin can verify station status and scan keys.
- Admin reward configuration visible.
- Admin cannot access routes without permission.

---

## 4. Deployment Pipeline — MVP

```text
Merge to main/release branch
↓
CI: compile + test
↓
Build artifact / Docker image
↓
Deploy to staging
↓
Run migration on staging
↓
Run smoke + regression subset
↓
Tag release candidate
↓
Backup production DB
↓
Deploy production
↓
Run production migration
↓
Run production smoke
↓
Monitor 24h
```

---

## 5. Staging Smoke Test

Must pass before production:

| Area | Test |
|---|---|
| Auth | Login user and admin. |
| RBAC | Non-admin cannot access admin endpoint. |
| Metro | Fetch line/station list. |
| Scan resolve | Resolve active test NFC/QR key. |
| Collection | Collect stamp with valid GPS. |
| Duplicate | Retry same collect request. |
| Stamp Book | New stamp appears. |
| Reward | Milestone reward issued if threshold reached. |
| Voucher | Voucher allocation works if enabled. |
| Asset | Public image loads. |
| Logs | No unexpected stack traces. |

---

## 6. Production Smoke Test

Use a controlled test account and test station/scan key if possible.

### Required production checks

1. API health endpoint returns OK.
2. Login works.
3. Admin login works.
4. Public station list loads.
5. Test station detail loads.
6. Scan resolve works for test key.
7. Collect test stamp works or dry-run endpoint works if production collect cannot be used.
8. Stamp Book updates.
9. Reward behavior is correct for test milestone or disabled safely.
10. Logs and metrics are visible.

### Production smoke rule

Do not test using real partner voucher codes unless the test account and reward are clearly marked operational/test.

---

## 7. Rollback Strategy

### 7.1 App/backend rollback

If deployment breaks runtime behavior but migration is backward-compatible:

```text
Stop new version
↓
Start previous artifact/image
↓
Run smoke test
↓
Investigate offline
```

### 7.2 Migration rollback

Prefer forward-fix migrations over destructive rollback.

Rules:

- Do not drop columns/tables in MVP production migrations unless confirmed unused.
- For additive migrations, rollback by app version is usually enough.
- For constraint changes, prepare a data cleanup script before applying.
- For enum/status changes, verify old app behavior before deploy.

### 7.3 Feature flag rollback

High-risk features should be flaggable:

- pre-stamp ads;
- QR fallback;
- voucher reveal;
- referral reward;
- experimental campaign.

---

## 8. Monitoring Plan

### 8.1 Metrics to watch

| Metric | Why |
|---|---|
| API error rate | Detect broken release. |
| Auth failure spike | Token/security config issue. |
| Scan resolve failure rate | NFC/QR key issue. |
| Collect success rate | Core product health. |
| Duplicate scan rate | Abuse or UX retry issue. |
| GPS out-of-range rate | Station radius/device problem. |
| Reward issue failure rate | Milestone/voucher issue. |
| Voucher pool remaining | Partner ops risk. |
| DB connection usage | Load/scaling issue. |
| Redis error rate | Token/cache/QR risk. |

### 8.2 Logs to preserve

- Auth security events.
- Admin scan-key changes.
- Station status changes.
- Collect attempts with redacted scan metadata.
- GPS validation failures.
- Reward issue failures.
- Voucher allocation failures.
- Ad impression ingestion errors.

### 8.3 Sensitive log rules

Never log:

- password;
- raw JWT;
- refresh token;
- raw NFC/QR secret;
- full voucher code unless explicitly protected;
- precise personal data beyond operational need.

---

## 9. Incident Response

### Severity levels

| Severity | Example | Response |
|---|---|---|
| SEV-1 | Users cannot scan/collect globally. | Immediate rollback/hotfix. |
| SEV-1 | Duplicate voucher issued. | Disable voucher issuance, investigate DB, notify ops. |
| SEV-2 | One station scan key broken. | Deactivate/rotate key, notify ops. |
| SEV-2 | GPS false negatives high. | Adjust station radius after approval. |
| SEV-3 | Admin UI issue but API works. | Patch in normal hotfix cycle. |
| SEV-3 | Analytics delay. | Backfill if events are stored. |

---

## 10. Release Notes Template

```text
Release: vX.Y.Z
Date:
Owner:
Environment:

Summary:
- ...

Backend changes:
- ...

Mobile changes:
- ...

Database migrations:
- ...

Feature flags:
- ...

Known issues:
- ...

Rollback plan:
- ...

Smoke test result:
- ...
```

---

## 11. Go-Live Readiness Checklist

| Area | Required |
|---|---:|
| P0 feature tests pass | Yes |
| Real-device NFC/QR test pass | Yes |
| Production secrets configured | Yes |
| DB backup completed | Yes |
| Admin test account ready | Yes |
| Test mobile account ready | Yes |
| Station seed data verified | Yes |
| Default campaign active | Yes |
| Reward policy configured | Yes |
| Voucher policy confirmed | Conditional |
| Monitoring/log access ready | Yes |
| Rollback artifact available | Yes |
| Owner on standby for 24h | Yes |

---

## 12. Edge Cases / Failure Modes

1. **Migration succeeds but app rollback cannot read new enum/status**  
   Backward compatibility must be checked before deploy.

2. **Production seed data missing default campaign**  
   Collect endpoint may fail globally even though station data exists.

3. **Redis points to wrong environment**  
   Auth/QR/cache behavior becomes inconsistent. Environment variables must be verified.

4. **Mobile build points to staging API**  
   Users install app but data does not match production.

5. **Voucher pool uses real partner codes during smoke test**  
   Test can consume paid partner inventory. Use test reward or mock partner.

6. **Swagger exposed publicly**  
   Increases attack surface. Disable/protect in production.

---

## 13. Release Acceptance Gate

A release can proceed only when:

- staging smoke passes;
- production backup exists;
- migration risk is reviewed;
- rollback plan is documented;
- production secrets are verified;
- QA signs off P0 flow;
- operational owner is available during launch window;
- known unresolved risks are explicitly accepted.
