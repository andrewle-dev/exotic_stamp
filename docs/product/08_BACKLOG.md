# 08 — Backlog: Exotic Stamp / Metro Stamp

> Status: Draft v0.1  
> Owner: Product / Tech Lead / Engineering  
> Purpose: Break epics into implementation-ready stories with backend, mobile/admin, and QA tasks.

---

## 1. Backlog Rules

A story is implementation-ready only when:

- it has a clear acceptance criterion;
- dependencies are known;
- data integrity impact is stated;
- API behavior is deterministic if story touches backend/mobile contract;
- QA cases include happy path, negative path, and at least one edge case.

Priority:

| Priority | Meaning |
|---|---|
| P0 | Must ship in MVP. |
| P1 | Should ship if it does not endanger P0 correctness. |
| P2 | Phase 2 or later. |

---

## 2. P0 Backlog Summary

| Epic | P0 Stories |
|---|---:|
| EPIC-01 Identity & Session | 4 |
| EPIC-02 RBAC & Admin Security | 5 |
| EPIC-03 Metro Data Operations | 7 |
| EPIC-04 Scan Key & Public Station Contract | 5 |
| EPIC-05 Default Campaign & Stamp Design | 4 |
| EPIC-06 Scan / GPS / Collection Core | 11 |
| EPIC-07 Stamp Book | 4 |
| EPIC-08 Reward & Voucher Engine | 5 core + conditional voucher |
| EPIC-09 Mobile Integration Contract | 8 |
| EPIC-10 Operations, Audit & QA | 6 |

---

## 3. EPIC-01 — Identity & Session Backlog

### AUTH-01 — Register by email/password

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Validate request, create user, hash password, enforce unique email/username, emit verification if enabled. |
| Mobile Task | Registration form, validation states, success/error UI. |
| Admin Task | None. |
| QA Task | Valid register, duplicate email, weak password, malformed email. |
| Data Integrity | Unique email/username at DB + domain validation. |
| Acceptance Criteria | Valid request creates user; duplicate email returns deterministic conflict; password never exposed. |
| Dependencies | User module, auth security config. |

### AUTH-02 — Login and receive session

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Authenticate credentials, issue access/refresh token, bind session to deviceFingerprint if supported. |
| Mobile Task | Login form, secure token storage, authenticated navigation. |
| QA Task | Valid login, invalid password, inactive user, unverified email policy. |
| Data Integrity | Refresh session stored once per policy. |
| Acceptance Criteria | Valid credentials produce usable access token; invalid credentials rejected. |
| Dependencies | AUTH-01. |

### AUTH-03 — Refresh token rotation

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Rotate refresh token, reject reused token, revoke affected session/user according to policy. |
| Mobile Task | Auto-refresh interceptor, logout on refresh failure. |
| QA Task | Normal refresh, expired refresh, reused refresh, concurrent refresh. |
| Data Integrity | Prevent stale refresh token reuse; session state consistent. |
| Acceptance Criteria | Old refresh token cannot be reused after rotation. |
| Dependencies | AUTH-02. |

### AUTH-04 — Logout current device

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Invalidate refresh session and optionally denylist access token. |
| Mobile Task | Clear local session and redirect to login. |
| QA Task | Logout then refresh, logout twice, logout with expired token. |
| Data Integrity | Session invalidated exactly once; no token leak. |
| Acceptance Criteria | Logged out device cannot refresh. |
| Dependencies | AUTH-02, AUTH-03. |

---

## 4. EPIC-02 — RBAC & Admin Security Backlog

### RBAC-01 — Define MVP permissions

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Add permissions for line/station/scan-key/asset/campaign/reward/voucher/admin analytics. |
| QA Task | Verify permissions seeded and mapped. |
| Data Integrity | Permission names stable; no duplicate permission. |
| Acceptance Criteria | All sensitive endpoints have corresponding permission. |
| Dependencies | Auth module. |

### RBAC-02 — Protect admin endpoints

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Add method-level permission guards to admin APIs. |
| QA Task | Normal user receives 403; admin succeeds. |
| Data Integrity | No bypass through alternate route. |
| Acceptance Criteria | Every admin endpoint is protected server-side. |
| Dependencies | RBAC-01. |

### RBAC-03 — Assign/revoke admin role

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Implement role assignment/revocation or verify existing implementation. |
| Admin Task | Minimal admin user management if UI exists. |
| QA Task | Assign, duplicate assign, revoke, concurrent change. |
| Data Integrity | Unique user-role assignment; cache invalidation. |
| Acceptance Criteria | Role changes take effect deterministically. |
| Dependencies | RBAC-01. |

### RBAC-04 — Permission cache invalidation

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Evict cached permission/role view after assignment/revocation. |
| QA Task | Revoked user cannot call admin endpoint after cache eviction. |
| Data Integrity | Prevent stale privilege. |
| Acceptance Criteria | Permission changes are reflected immediately or within documented TTL. |
| Dependencies | RBAC-03. |

### RBAC-05 — Audit admin role changes

| Field | Value |
|---|---|
| Priority | P1 but recommended P0 if audit exists |
| Backend Task | Write audit event for role assignment/revocation. |
| QA Task | Audit row exists and redacts sensitive data. |
| Data Integrity | Append-only audit. |
| Acceptance Criteria | Each role mutation is traceable. |
| Dependencies | Audit infrastructure. |

---

## 5. EPIC-03 — Metro Data Operations Backlog

### METRO-01 — Create line

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Admin create line API, DTO validation, service, repository adapter, migration check. |
| Admin Task | Create line form if admin web included. |
| QA Task | Valid create, duplicate code, missing name/code. |
| Data Integrity | Unique line code; status constraint. |
| Acceptance Criteria | Admin can create line; duplicate code rejected. |
| Dependencies | RBAC-02. |

### METRO-02 — Update / disable line

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Update line metadata/status; cache eviction. |
| QA Task | Update valid fields, disable line, invalid status. |
| Data Integrity | Do not hard delete line with stations/stamps. |
| Acceptance Criteria | Disabled line is not collectable through active public flow. |
| Dependencies | METRO-01. |

### METRO-03 — Create station

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Admin create station API with line FK, code, coordinates, radius, status. |
| Admin Task | Station form. |
| QA Task | Valid create, invalid line, duplicate station code in line, invalid GPS. |
| Data Integrity | FK line; unique `(line_id, station_code)`; coordinate/radius checks. |
| Acceptance Criteria | Admin can create valid station under line. |
| Dependencies | METRO-01. |

### METRO-04 — Update station location/radius/status

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Update station metadata, location, radius, status; evict station/stamp book caches. |
| QA Task | Radius boundary, disable station, invalid coordinates. |
| Data Integrity | Prevent huge radius; no hard delete with existing stamps. |
| Acceptance Criteria | Updated station data affects collect validation. |
| Dependencies | METRO-03. |

### METRO-05 — Upload public station/stamp asset

| Field | Value |
|---|---|
| Priority | P0/P1 |
| Backend Task | Validate file type/size/path, store asset, return public URL/reference. |
| Admin Task | Upload UI if available. |
| QA Task | Valid image, oversized file, invalid MIME, path traversal attempt. |
| Data Integrity | Asset linked to station/stamp design; no unsafe file path. |
| Acceptance Criteria | Asset is retrievable through public-safe URL. |
| Dependencies | METRO-03. |

### METRO-06 — Public station list/detail

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Public/mobile endpoint for active line/station data, excludes secrets. |
| Mobile Task | Render station list/detail. |
| QA Task | Active station visible, inactive hidden/unavailable, no scan key in response. |
| Data Integrity | Cache invalidated on station update. |
| Acceptance Criteria | Mobile can load stations without sensitive data. |
| Dependencies | METRO-03, METRO-04. |

### METRO-07 — Station status policy

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Define DRAFT/ACTIVE/INACTIVE behavior and enforce in collect/public APIs. |
| QA Task | Draft/inactive station cannot be collected. |
| Data Integrity | Status check constraint. |
| Acceptance Criteria | Station state behaves consistently across admin, public, collect. |
| Dependencies | METRO-03. |

---

## 6. EPIC-04 — Scan Key & Public Station Contract Backlog

### SCANKEY-01 — Create NFC scan key

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Admin create/assign NFC key to station; store status and masked representation. |
| QA Task | Valid key, duplicate key, invalid station. |
| Data Integrity | Unique active key; key redaction. |
| Acceptance Criteria | Active NFC key can resolve to station in collect flow. |
| Dependencies | METRO-03. |

### SCANKEY-02 — Rotate/revoke scan key

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Revoke old key, activate new key, audit change, evict scan resolve cache. |
| QA Task | Old key rejected, new key accepted, logs redacted. |
| Data Integrity | No two active keys causing ambiguity unless explicitly allowed. |
| Acceptance Criteria | Rotation takes effect immediately. |
| Dependencies | SCANKEY-01. |

### SCANKEY-03 — QR dynamic token

| Field | Value |
|---|---|
| Priority | P0 if QR fallback MVP |
| Backend Task | Generate/validate/consume QR token with TTL and one-time-use semantics. |
| Mobile/Admin Task | Define where QR is displayed/scanned. |
| QA Task | Valid token, expired token, reused token, concurrent consume. |
| Data Integrity | Atomic token consume; no static QR in production. |
| Acceptance Criteria | QR token cannot be reused. |
| Dependencies | Redis/token storage, METRO-03. |

### SCANKEY-04 — Scan key redaction

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Mask scan values in DTO and logs. |
| QA Task | Inspect response/log output for raw key leakage. |
| Data Integrity | Sensitive scan value not exposed. |
| Acceptance Criteria | Public API/logs do not show raw scan key. |
| Dependencies | SCANKEY-01. |

### SCANKEY-05 — Invalid scan rate limit

| Field | Value |
|---|---|
| Priority | P1/P0 public launch |
| Backend Task | Rate limit repeated invalid scan attempts by user/device/IP. |
| QA Task | Repeated invalid attempts return `RATE_LIMITED`. |
| Data Integrity | Prevent brute force/key enumeration. |
| Acceptance Criteria | Abuse attempts are throttled and logged. |
| Dependencies | Security/Redis. |

---

## 7. EPIC-05 — Default Campaign & Stamp Design Backlog

### CAMP-01 — Seed/create default campaign

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Create campaign entity/record and default campaign selection rule. |
| QA Task | No default campaign, duplicate default campaign, inactive default campaign. |
| Data Integrity | Enforce one active default according to selected policy. |
| Acceptance Criteria | Collect flow can resolve active default campaign. |
| Dependencies | Metro data. |

### CAMP-02 — Link stations to default campaign

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Use `campaign_stations` or equivalent relation. |
| QA Task | Station not in campaign cannot collect. |
| Data Integrity | FK campaign/station; no orphan relation. |
| Acceptance Criteria | Only campaign stations count in stamp book/rewards. |
| Dependencies | CAMP-01, METRO-03. |

### CAMP-03 — Stamp design configuration

| Field | Value |
|---|---|
| Priority | P0/P1 |
| Backend Task | Link station/campaign to stamp design asset. |
| Admin Task | Upload/select stamp asset if UI exists. |
| QA Task | Missing asset fallback, invalid asset, update cache. |
| Data Integrity | No broken required stamp asset if MVP requires visuals. |
| Acceptance Criteria | Stamp book/collect response contains stamp design URL or fallback. |
| Dependencies | METRO-05. |

### CAMP-04 — Campaign active window validation

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Check campaign status/start/end during collect. |
| QA Task | Before start, after end, inactive campaign. |
| Data Integrity | Collection stores campaign id. |
| Acceptance Criteria | Inactive/expired campaign blocks collect. |
| Dependencies | CAMP-01. |

---

## 8. EPIC-06 — Scan / GPS / Collection Core Backlog

### COLL-01 — Define collect command and API DTO

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Request DTO, command object, response DTO, validation annotations. |
| Mobile Task | Match request payload fields. |
| QA Task | Missing scan type/value/GPS/clientRequestId validation. |
| Data Integrity | `clientRequestId` required for idempotency. |
| Acceptance Criteria | API contract is stable and documented. |
| Dependencies | Feature spec approval. |

### COLL-02 — Resolve scan inside collect flow

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Resolve NFC/QR to station from active key/token. |
| QA Task | Valid NFC, invalid key, revoked key, expired QR. |
| Data Integrity | Indexed hot path; redacted logging. |
| Acceptance Criteria | Collect can locate station safely. |
| Dependencies | SCANKEY-01/03. |

### COLL-03 — Validate station and campaign state

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Enforce station active, line active, campaign active, station in campaign. |
| QA Task | Inactive station, inactive line, expired campaign. |
| Data Integrity | Revalidate at transaction time. |
| Acceptance Criteria | Invalid state blocks collect. |
| Dependencies | CAMP-01/02/04. |

### COLL-04 — GPS distance validation

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Compute distance server-side using station coordinates/radius. |
| Mobile Task | Send lat/lng/accuracy. |
| QA Task | In range, out of range, invalid lat/lng. |
| Data Integrity | Store GPS metadata if required; reject impossible coordinates. |
| Acceptance Criteria | Out-of-range scan returns `GPS_OUT_OF_RANGE`. |
| Dependencies | METRO-04. |

### COLL-05 — GPS accuracy policy

| Field | Value |
|---|---|
| Priority | P0/P1 depending final decision |
| Backend Task | Enforce `accuracyMeters <= maxAllowedAccuracy`. |
| Mobile Task | Show poor accuracy UI. |
| QA Task | Missing accuracy, poor accuracy, station-specific override. |
| Data Integrity | Prevent low-quality GPS from bypassing anti-cheat. |
| Acceptance Criteria | Poor GPS is rejected or handled by documented fallback. |
| Dependencies | COLL-04. |

### COLL-06 — Persist user stamp

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Create `user_stamps` record transactionally. |
| QA Task | Valid collect writes row with user/station/campaign/timestamp. |
| Data Integrity | Unique `(user_id, station_id, campaign_id)`. |
| Acceptance Criteria | Valid scan creates one stamp. |
| Dependencies | COLL-02/03/04. |

### COLL-07 — Duplicate collection prevention

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Service check + DB constraint + exception mapping. |
| QA Task | Same station/campaign second request returns `STAMP_ALREADY_COLLECTED`. |
| Data Integrity | DB constraint is final source of truth under race. |
| Acceptance Criteria | Duplicate stamp is impossible. |
| Dependencies | COLL-06. |

### COLL-08 — Idempotent retry

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Store idempotency result by `(user_id, clientRequestId)`; replay previous success. |
| Mobile Task | Generate stable UUID per scan attempt and reuse on retry. |
| QA Task | Timeout/retry, same id replay, id conflict with different payload. |
| Data Integrity | Prevent duplicate on network timeout. |
| Acceptance Criteria | Same request retry returns same success response or deterministic idempotency error. |
| Dependencies | COLL-06. |

### COLL-09 — Cache invalidation after collect

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Evict user stamp book/progress cache after stamp write. |
| QA Task | Stamp book immediately reflects new stamp. |
| Data Integrity | Prevent stale progress/reward display. |
| Acceptance Criteria | Post-collect query is fresh. |
| Dependencies | COLL-06, BOOK stories. |

### COLL-10 — Post-collect event

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Publish event after stamp creation for reward/analytics. |
| QA Task | Event fired once after successful collect; not fired on failure. |
| Data Integrity | Event loss strategy documented; reward dedupe still required. |
| Acceptance Criteria | Reward engine receives valid collect context. |
| Dependencies | COLL-06. |

### COLL-11 — Stable collection error mapping

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Map domain errors to stable mobile error codes. |
| Mobile Task | Handle each error code. |
| QA Task | Invalid key, GPS fail, duplicate, inactive station, rate limit. |
| Data Integrity | No raw DB/JPA error leaks. |
| Acceptance Criteria | Mobile can display correct state for each failure. |
| Dependencies | COLL-01 through COLL-10. |

---

## 9. EPIC-07 — Stamp Book Backlog

### BOOK-01 — Get my stamp book

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Query stations + user stamps for default campaign/line. |
| Mobile Task | Render grid/list. |
| QA Task | New user, partial collection, completed collection. |
| Data Integrity | Count committed stamps only. |
| Acceptance Criteria | User sees collected/uncollected stations and progress. |
| Dependencies | COLL-06, METRO-06. |

### BOOK-02 — Include stamp design and station metadata

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Response includes station name, asset URL, collectedAt if collected. |
| Mobile Task | Show collected/uncollected visuals. |
| QA Task | Missing asset fallback, inactive station policy. |
| Data Integrity | No scan secrets in response. |
| Acceptance Criteria | Stamp book is safe for public/mobile rendering. |
| Dependencies | CAMP-03. |

### BOOK-03 — Progress summary

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Return total, collected, percentage, next milestone if available. |
| Mobile Task | Progress bar/state. |
| QA Task | 0%, partial, 100%, no stations. |
| Data Integrity | No double count. |
| Acceptance Criteria | Progress matches DB state. |
| Dependencies | BOOK-01. |

### BOOK-04 — Stamp book cache policy

| Field | Value |
|---|---|
| Priority | P1 but recommended for scale |
| Backend Task | Cache read-heavy stamp book and evict on writes. |
| QA Task | Cache hit/miss, invalidation after collect/station update. |
| Data Integrity | Cache key includes user + line + campaign. |
| Acceptance Criteria | Cache does not serve stale post-collect result. |
| Dependencies | BOOK-01, COLL-09. |

---

## 10. EPIC-08 — Reward & Voucher Engine Backlog

### REWARD-01 — Configure milestone and reward

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Admin/service support milestone requiredStamps, reward type, status. |
| Admin Task | Minimal admin config or seed data. |
| QA Task | Valid milestone, duplicate threshold policy, inactive milestone. |
| Data Integrity | Required stamps positive; reward active. |
| Acceptance Criteria | Reward engine can load active milestone config. |
| Dependencies | CAMP-01. |

### REWARD-02 — Evaluate milestone after collect

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Count valid stamps and find reached milestones. |
| QA Task | Below threshold, exact threshold, above threshold, multiple thresholds. |
| Data Integrity | Count only committed stamps in campaign. |
| Acceptance Criteria | Milestone match is correct. |
| Dependencies | COLL-10, REWARD-01. |

### REWARD-03 — Issue user reward once

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Insert user reward with unique constraint/dedupe. |
| QA Task | Duplicate event/job cannot create duplicate reward. |
| Data Integrity | Unique user+milestone/campaign. |
| Acceptance Criteria | User receives at most one reward per milestone. |
| Dependencies | REWARD-02. |

### REWARD-04 — Return/view user rewards

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Endpoint for my rewards and reward detail. |
| Mobile Task | Reward list/detail screen or result state. |
| QA Task | User sees own rewards only. |
| Data Integrity | Authorization prevents reading other user reward. |
| Acceptance Criteria | User can view issued rewards. |
| Dependencies | REWARD-03. |

### REWARD-05 — Reward notification

| Field | Value |
|---|---|
| Priority | P1 |
| Backend Task | Create notification after reward issue. |
| Mobile Task | Display reward notification if notification UI exists. |
| QA Task | Notification created once; failure does not duplicate reward. |
| Data Integrity | Notification not source of truth. |
| Acceptance Criteria | Reward visibility improves without corrupting reward state. |
| Dependencies | REWARD-03. |

### VOUCHER-01 — Import voucher pool

| Field | Value |
|---|---|
| Priority | Conditional P0 if real vouchers |
| Backend Task | Admin import/list voucher codes with validation. |
| QA Task | Duplicate code, invalid reward, expired voucher. |
| Data Integrity | Unique voucher code; no plaintext logs. |
| Acceptance Criteria | Admin can load valid voucher pool. |
| Dependencies | REWARD-01. |

### VOUCHER-02 — Atomic voucher allocation

| Field | Value |
|---|---|
| Priority | Conditional P0 if real vouchers |
| Backend Task | Lock/conditional update available voucher and attach to user reward. |
| QA Task | Concurrent users cannot receive same code. |
| Data Integrity | One voucher assigned once. |
| Acceptance Criteria | Voucher allocation is race-safe. |
| Dependencies | VOUCHER-01, REWARD-03. |

### VOUCHER-03 — Empty voucher pool policy

| Field | Value |
|---|---|
| Priority | Conditional P0 if real vouchers |
| Backend Task | Return pending fulfillment/fallback behavior. |
| QA Task | Empty pool still leaves stamp collected and reward state deterministic. |
| Data Integrity | Do not rollback valid stamp due to empty voucher if policy says pending. |
| Acceptance Criteria | Empty pool does not corrupt user progress. |
| Dependencies | VOUCHER-02. |

---

## 11. EPIC-09 — Mobile Integration Contract Backlog

### MOBILE-01 — Auth contract

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Document request/response/error for auth endpoints. |
| Mobile Task | Implement secure token storage and refresh interceptor. |
| QA Task | Expired access token refresh flow. |
| Acceptance Criteria | Mobile can maintain session safely. |
| Dependencies | AUTH stories. |

### MOBILE-02 — Station contract

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Document line/station list/detail response. |
| Mobile Task | Render station screens. |
| QA Task | Empty station list, inactive station behavior. |
| Acceptance Criteria | Mobile can show station metadata safely. |
| Dependencies | METRO-06. |

### MOBILE-03 — Collect contract

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Document collect request/response including `clientRequestId`. |
| Mobile Task | Send NFC/QR + GPS + device metadata. |
| QA Task | NFC/QR happy paths and all core failures. |
| Acceptance Criteria | Mobile can complete collect flow end-to-end. |
| Dependencies | COLL-01 through COLL-11. |

### MOBILE-04 — Error code matrix

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Produce stable error codes and HTTP status mapping. |
| Mobile Task | Map each error to UI state. |
| QA Task | Trigger each error. |
| Acceptance Criteria | No unknown backend error for expected business failures. |
| Dependencies | COLL-11. |

### MOBILE-05 — Retry/idempotency behavior

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Document replay vs duplicate rules. |
| Mobile Task | Reuse same clientRequestId for retry. |
| QA Task | Network timeout then retry. |
| Acceptance Criteria | Retry is safe and deterministic. |
| Dependencies | COLL-08. |

### MOBILE-06 — GPS failure behavior

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Document GPS required fields and error codes. |
| Mobile Task | GPS permission UI, poor accuracy UI, out-of-range UI. |
| QA Task | Permission denied, stale/poor GPS, out-of-range. |
| Acceptance Criteria | User receives clear scan failure reason. |
| Dependencies | COLL-04, COLL-05. |

### MOBILE-07 — Stamp book contract

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Document stamp book schema. |
| Mobile Task | Render progress and station cards. |
| QA Task | New/partial/completed book. |
| Acceptance Criteria | UI matches backend state. |
| Dependencies | BOOK stories. |

### MOBILE-08 — Reward contract

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Document reward response/list/detail. |
| Mobile Task | Show reward after collect or from reward list. |
| QA Task | Reward issued, no reward, voucher pending. |
| Acceptance Criteria | User can understand earned reward state. |
| Dependencies | REWARD stories. |

---

## 12. EPIC-10 — Operations, Audit & QA Backlog

### OPS-01 — Audit sensitive admin actions

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Audit line/station/scan-key/campaign/reward/voucher admin changes. |
| QA Task | Verify audit exists and redacts secrets. |
| Data Integrity | Append-only audit log. |
| Acceptance Criteria | Sensitive admin action is traceable. |
| Dependencies | Admin APIs. |

### OPS-02 — Rate limit auth/scan

| Field | Value |
|---|---|
| Priority | P0/P1 |
| Backend Task | Add rate limiting to login and scan endpoints. |
| QA Task | Exceed threshold returns `RATE_LIMITED`. |
| Data Integrity | Prevent brute force and scan key enumeration. |
| Acceptance Criteria | Abuse attempts throttled. |
| Dependencies | Redis/security infra. |

### OPS-03 — Core integration tests

| Field | Value |
|---|---|
| Priority | P0 |
| Backend Task | Integration tests for migration constraints and service flows. |
| QA Task | Run in CI. |
| Data Integrity | Validate DB constraints catch races/duplicates. |
| Acceptance Criteria | Tests fail if duplicate stamp/reward/voucher is possible. |
| Dependencies | Core modules implemented. |

### OPS-04 — API tests for scan/reward

| Field | Value |
|---|---|
| Priority | P0 |
| Backend/QA Task | API tests for auth, public station, collect, stamp book, reward. |
| Acceptance Criteria | End-to-end backend MVP flow passes. |
| Dependencies | EPIC-06, EPIC-08. |

### OPS-05 — Smoke test checklist

| Field | Value |
|---|---|
| Priority | P0 |
| QA Task | Create executable smoke test sequence for staging/prod. |
| Acceptance Criteria | Auth → admin setup → scan → stamp book → reward verified after deploy. |
| Dependencies | All P0 flows. |

### OPS-06 — Basic monitoring/logging

| Field | Value |
|---|---|
| Priority | P0 public launch |
| Backend/Ops Task | Health checks, error logs, request latency, critical failure alerts. |
| QA Task | Trigger controlled failure and verify visibility. |
| Acceptance Criteria | Production issues are observable. |
| Dependencies | Deployment environment. |

---

## 13. EPIC-11 — Monetization Foundation Backlog

### MON-01 — Ad slot model

| Field | Value |
|---|---|
| Priority | P1/Phase 2 |
| Backend Task | Define ad slot table/model for pre-stamp/home placements. |
| QA Task | Active/inactive slot validation. |
| Data Integrity | No client-controlled payout fields. |
| Acceptance Criteria | Backend can decide eligible ad slot. |
| Dependencies | Collection stable. |

### MON-02 — Impression tracking with dedupe

| Field | Value |
|---|---|
| Priority | P1/Phase 2 |
| Backend Task | Track impression event with server-issued ad decision id and clientRequestId. |
| QA Task | Duplicate impression retry, expired ad decision, fake event. |
| Data Integrity | Prevent fake/double-count impressions. |
| Acceptance Criteria | Invalid/fake impression rejected. |
| Dependencies | MON-01. |

### MON-03 — Click tracking

| Field | Value |
|---|---|
| Priority | P2 |
| Backend Task | Track click event and redirect/link metadata. |
| QA Task | Expired campaign click, duplicate click, unauthorized click. |
| Data Integrity | Click cannot invent partner payout. |
| Acceptance Criteria | Click event stored safely. |
| Dependencies | MON-02. |

---

## 14. EPIC-12 — Community / Growth Foundation Backlog

### COMM-01 — Share event tracking

| Field | Value |
|---|---|
| Priority | P1 |
| Backend Task | Store share event for user/stamp/reward/platform. |
| Mobile Task | Native share then report event. |
| QA Task | Valid share, duplicate retry, share other user entity. |
| Data Integrity | User can only report own entity. |
| Acceptance Criteria | Share event recorded without blocking share UX. |
| Dependencies | Collection/reward. |

### COMM-02 — Reward notification

| Field | Value |
|---|---|
| Priority | P1 |
| Backend Task | Create notification when reward issued. |
| Mobile Task | Fetch/display notifications if screen exists. |
| QA Task | Notification once per reward. |
| Data Integrity | Notification is not reward source of truth. |
| Acceptance Criteria | User can discover reward from notification. |
| Dependencies | REWARD-03. |

### COMM-03 — Referral code foundation

| Field | Value |
|---|---|
| Priority | P2 |
| Backend Task | Generate unique referral code per user. |
| QA Task | Unique code, duplicate generation, user deletion state. |
| Data Integrity | One active referral code per user. |
| Acceptance Criteria | User has stable referral code. |
| Dependencies | Auth/User. |

---

## 15. Recommended Implementation Cut

### Sprint / Stage Candidate 1 — Backend P0 Foundation

```text
AUTH-01..04
RBAC-01..04
METRO-01..07
SCANKEY-01..04
CAMP-01..04
```

### Sprint / Stage Candidate 2 — Collection Core

```text
COLL-01..11
BOOK-01..03
OPS-03 partial
MOBILE-03..06 contract
```

### Sprint / Stage Candidate 3 — Reward Core

```text
REWARD-01..04
VOUCHER-01..03 if real vouchers
BOOK-03 next milestone integration
MOBILE-08
OPS-04
```

### Sprint / Stage Candidate 4 — Hardening + Mobile Contract

```text
MOBILE-01..08
OPS-01..06
BOOK-04 cache if needed
COMM-01/02 if time allows
MON-01 contract only if pre-stamp ad remains in MVP support
```

---

## 16. Backlog Exit Criteria

This backlog is accepted when:

- every P0 story has owner and estimate;
- open product decisions are resolved or removed from P0;
- each story has acceptance criteria;
- duplicate/retry/reward/voucher edge cases are testable;
- implementation order follows dependency map;
- no P2 story is started before P0 scan/reward correctness is stable.
