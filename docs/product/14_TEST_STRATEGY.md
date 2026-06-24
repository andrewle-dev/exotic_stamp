# 14 — Test Strategy: Exotic Stamp / Metro Stamp

> Status: Draft v0.1  
> Owner: QA Lead / Tech Lead / Backend Lead / Mobile Lead  
> Purpose: Define test scope, test levels, test data, acceptance gates, and failure-mode coverage for MVP release.

---

## 1. Feasibility Check

Testing is feasible if the team avoids the common mistake: only testing happy-path API responses.

Exotic Stamp needs failure-mode testing because the highest-risk flows are race-sensitive and hardware-dependent:

- NFC/QR scan;
- GPS validation;
- duplicate scan prevention;
- reward/voucher idempotency;
- Redis/token behavior;
- cache invalidation;
- Android device compatibility.

The MVP is not testable enough if QA cannot prove what happens when mobile retries, Redis fails, station data changes, or two requests race.

---

## 2. Test Objectives

1. Prove scan-to-stamp flow works end-to-end.
2. Prove duplicate stamps cannot be created.
3. Prove rewards cannot be issued twice.
4. Prove voucher codes cannot be double-allocated.
5. Prove public APIs do not leak sensitive data.
6. Prove mobile retry behavior is deterministic.
7. Prove admin changes are reflected after cache invalidation.
8. Prove app is usable on real Android devices with NFC/GPS.
9. Prove deployment has safe smoke, rollback, and monitoring path.

---

## 3. Test Levels

| Level | Owner | Scope |
|---|---|---|
| Unit Test | Backend dev | Domain rules, service logic, mapper behavior. |
| Repository Integration Test | Backend dev | JPA mappings, constraints, indexes, migrations. |
| API Integration Test | Backend dev / QA | Controller + security + service + DB behavior. |
| Contract Test | Backend + Mobile | Request/response/error compatibility. |
| Mobile Integration Test | Mobile dev / QA | Flutter flow against staging backend. |
| Device Test | QA | Real Android NFC/GPS behavior. |
| Load Test | Backend / QA | Scan collect, stamp book, impression ingest. |
| Security Test | Backend / QA | Auth, RBAC, token revocation, upload, data leaks. |
| Release Smoke Test | QA / DevOps | Production/staging critical path after deploy. |

---

## 4. Test Environment Matrix

| Environment | Purpose | Data |
|---|---|---|
| Local | Dev feedback | Seeded minimal data, test containers if available. |
| CI | Automated regression | Ephemeral DB/Redis, deterministic fixtures. |
| Staging | API/mobile integration | Realistic station/campaign/reward setup. |
| Production | Smoke only | Real data, non-destructive test account. |

---

## 5. Required Test Data

### 5.1 Users

- Normal active user.
- Pending verification user.
- Disabled user.
- Admin user with metro permissions.
- Admin user without reward permission.
- User with existing stamps.
- User with existing reward.

### 5.2 Metro data

- Active line with 14 stations.
- Active station with NFC key.
- Active station with QR fallback.
- Inactive station.
- Station with strict GPS radius.
- Station with relaxed GPS radius.
- Station with missing stamp design.

### 5.3 Campaign/reward data

- Active default campaign.
- Expired campaign.
- Paused campaign.
- Milestones: 3, 7, 14 or confirmed MVP values.
- Digital reward.
- Voucher reward.
- Voucher pool with enough codes.
- Voucher pool with exactly one code.
- Empty voucher pool.

### 5.4 Monetization/community data

- Active ad creative.
- Expired ad creative.
- Affiliate banner active/inactive.
- Referral code for another user.
- Referral code for self-referral test.

---

## 6. Backend Unit Test Scope

### 6.1 Auth

- Password validation.
- Login success.
- Invalid password.
- Disabled user.
- Refresh token rotation.
- Refresh token reuse attack.
- Logout current device.
- Logout all devices if supported.
- Access token revocation validator behavior.

### 6.2 Metro

- Station status validation.
- GPS field validation.
- Scan-key hash/rotation logic.
- Public DTO excludes scan secrets.

### 6.3 Collection

- Resolve valid NFC key.
- Resolve invalid NFC key.
- Resolve expired QR token.
- GPS within range.
- GPS out of range.
- Campaign inactive.
- Duplicate stamp.
- Same idempotency key replay.

### 6.4 Reward

- Milestone reached.
- Milestone not reached.
- Reward already issued.
- Voucher allocation success.
- Voucher pool exhausted.
- Reward event retry is no-op.

### 6.5 Monetization

- Active creative selection.
- Expired creative ignored.
- Impression dedup.
- Invalid placement rejected.

### 6.6 Community

- Referral code generation.
- Self-referral blocked.
- Duplicate referred user blocked.
- Share event scoped to authenticated user.

---

## 7. Repository / Migration Tests

### Required checks

| Table Area | Constraint Test |
|---|---|
| Users | Duplicate email rejected. |
| RBAC | Duplicate user-role rejected. |
| Stations | Duplicate station code per line rejected. |
| Scan key | Duplicate active scan key rejected. |
| User stamps | Duplicate `(user_id, station_id, campaign_id)` rejected. |
| User rewards | Duplicate `(user_id, milestone_id)` rejected. |
| Voucher pool | Same voucher cannot be assigned twice. |
| Referrals | Same referred user cannot be referred twice. |
| Ad impressions | Required FKs/status validations work as designed. |

### Migration test rule

CI should run migrations from a clean DB. Do not test only against a manually mutated local database.

---

## 8. API Test Matrix — P0

### 8.1 Auth API

| Case | Expected |
|---|---|
| Register valid user | `201/200`, user created. |
| Register duplicate email | `409`. |
| Login valid user | Access token returned. |
| Login wrong password | `401`. |
| Refresh valid token | New access token. |
| Refresh reused token | Reuse attack handling. |
| Logout then use old access token | Rejected if access revocation is enabled. |

---

### 8.2 Metro API

| Case | Expected |
|---|---|
| Admin creates line | Success if permission exists. |
| Non-admin creates line | `403`. |
| Admin creates station with invalid GPS | `400`. |
| Duplicate station code in same line | `409`. |
| Public station list | No scan secrets exposed. |
| Rotate scan key | Old key no longer resolves if revoked. |

---

### 8.3 Collection API

| Case | Expected |
|---|---|
| Valid NFC + GPS | Stamp created. |
| Valid QR + GPS | Stamp created. |
| GPS outside radius | `422 GPS_OUT_OF_RANGE`. |
| Station inactive | `422 STATION_NOT_ACTIVE`. |
| Campaign inactive | `422 CAMPAIGN_NOT_ACTIVE`. |
| Duplicate scan | `409 STAMP_DUPLICATE` or idempotent replay by policy. |
| Retry same idempotency key | Original successful response. |
| Two concurrent collects | Exactly one row created. |

---

### 8.4 Reward API

| Case | Expected |
|---|---|
| User reaches milestone | Reward issued once. |
| Event retried | No duplicate reward. |
| Voucher available | One voucher assigned. |
| Last voucher race | One user gets voucher, other gets deterministic fallback/failure. |
| User reads another reward | `403/404`. |

---

## 9. Mobile Test Matrix

### 9.1 Device targets

Minimum real device pool:

- Samsung A-series with NFC.
- Xiaomi Redmi / Poco with NFC if available.
- Oppo / Vivo common Vietnam-market device.
- One Android device without NFC to validate QR fallback.

### 9.2 Mobile flows

| Flow | Expected |
|---|---|
| First open → register/login | User enters authenticated app. |
| Token expired during use | Refresh works or user is routed to login. |
| NFC scan success | Collect request sent and stamp shown. |
| QR fallback success | Collect request sent and stamp shown. |
| GPS permission denied | Clear error, no corrupt backend state. |
| GPS inaccurate | Clear error if over threshold. |
| Network timeout after scan | Retry behavior matches API contract. |
| App killed during scan | No duplicate stamp after reopening. |
| Stamp Book refresh | Latest stamp appears. |
| Reward achieved | Reward screen/notification visible. |

---

## 10. Load Test Strategy

### 10.1 Hot paths

| Endpoint | Target Test |
|---|---|
| Login/refresh | Moderate auth concurrency. |
| Scan resolve | High read QPS. |
| Collect stamp | Concurrent write with duplicate stress. |
| Stamp book | Repeated read after collect. |
| Impression ingest | High append-only write. |

### 10.2 Minimum scenarios

1. **Duplicate stamp race test**  
   100 concurrent requests for same user/station/campaign must create exactly one stamp.

2. **Multi-user station load test**  
   Many users collect same station concurrently; all eligible users succeed once.

3. **Stamp Book cache test**  
   After collect, stamp book must not return stale state.

4. **Reward race test**  
   Multiple events for same user/milestone must create exactly one reward.

5. **Ad impression ingestion**  
   Sustained append events must not degrade scan collect beyond acceptable latency.

---

## 11. Security Test Strategy

### Required checks

- Missing token returns `401`.
- User token cannot call admin API.
- Admin without permission returns `403`.
- JWT with invalid signature rejected.
- Revoked/old access token rejected if policy enabled.
- Refresh token reuse detected.
- Public station API does not leak scan key/hash.
- Voucher code cannot be read by another user.
- File upload rejects executable/malicious file names.
- Swagger disabled or protected in production.
- CORS production origin restricted.
- Rate limit scan/auth endpoints.

---

## 12. Regression Suite by Stage

| Stage | Regression Gate |
|---:|---|
| 1 | Auth + RBAC tests pass. |
| 2 | Auth + RBAC + Metro CRUD + public data tests pass. |
| 3 | All previous + collection/idempotency tests pass. |
| 4 | All previous + reward/voucher tests pass. |
| 5 | Backend regression + mobile integration smoke pass. |
| 6 | All previous + monetization tracking tests pass. |
| 8 | Full regression + load/security smoke pass. |

---

## 13. Data Integrity Test Requirements

| Risk | Test Required |
|---|---|
| Duplicate stamp | Concurrent API test + DB constraint test. |
| Duplicate reward | Event retry test + DB constraint test. |
| Voucher double allocation | Race test with one voucher. |
| Stale cache | Collect then immediately read stamp book. |
| Raw secret leak | DTO/API snapshot test. |
| Token reuse | Refresh reuse scenario. |
| Invalid GPS | Boundary tests for lat/lng/radius/accuracy. |

---

## 14. Edge Cases / Failure Modes

1. **Redis unavailable**  
   Cache read failure should not necessarily break station list, but QR token validation may need fail-safe behavior. Define separately.

2. **Database unique conflict occurs despite pre-check**  
   Service must translate DB conflict into deterministic domain error.

3. **Mail queue fails**  
   Register should not corrupt user state. Mail job should retry or expose pending verification state.

4. **Mobile sends malformed GPS**  
   Backend rejects with validation error. No partial stamp state.

5. **Reward evaluation fails after stamp created**  
   Stamp remains valid. Reward should be retried or visible as pending job depending architecture.

6. **Ad tracking endpoint is spammed**  
   Rate limit and dedup must prevent uncontrolled table growth.

---

## 15. Test Acceptance Gate

Testing is acceptable only when:

- P0 backend tests pass in CI;
- migration tests run from clean DB;
- duplicate scan and duplicate reward race tests pass;
- mobile real-device NFC/QR/GPS tests pass;
- staging E2E flow passes;
- production smoke checklist is ready;
- known skipped tests are documented with reason and owner.
