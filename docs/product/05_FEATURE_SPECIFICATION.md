# 05 — Feature Specification: Exotic Stamp / Metro Stamp

> Status: Draft v0.1  
> Owner: Product / Tech Lead  
> Purpose: Convert the clarified requirements and feature inventory into testable feature specifications before backlog execution.

---

## 1. Specification Rules

This document is the feature-level contract between product, backend, mobile, admin web, and QA.

A feature is not implementation-ready unless it has:

- business goal;
- actor;
- user/system flow;
- input and output;
- business rules;
- data integrity rules;
- security rules;
- acceptance criteria;
- failure modes;
- metrics or audit events.

### Status Legend

| Status | Meaning |
|---|---|
| Locked | Ready for implementation unless explicitly changed. |
| Recommended | Proposed implementation direction, still needs founder/product confirmation. |
| Open | Blocking decision remains unresolved. |
| Deferred | Not part of MVP implementation. |

---

## 2. MVP Feature Spec Index

| ID | Feature | MVP Status | Module Owner | Depends On |
|---|---|---:|---|---|
| FS-01 | Authentication / Session | Locked | `auth`, `user` | Foundation security |
| FS-02 | RBAC / Admin Security | Locked | `rbac` | Auth |
| FS-03 | Line Management | Locked | `metro` | RBAC |
| FS-04 | Station Management | Locked | `metro` | Line Management |
| FS-05 | Scan Key Management | Locked | `metro` | Station Management |
| FS-06 | Public Station Data | Locked | `metro` | Station Management |
| FS-07 | Default Campaign | Locked / Recommended | `collection`, `reward` | Station Data |
| FS-08 | Scan Resolve | Locked | `metro`, `collection` | Scan Key Management |
| FS-09 | GPS Validation | Locked / Recommended | `collection` | Scan Resolve |
| FS-10 | Stamp Collection + Idempotency | Locked | `collection` | Auth, Campaign, Scan, GPS |
| FS-11 | Stamp Book | Locked | `collection`, `metro` | Stamp Collection |
| FS-12 | Milestone Reward | Locked / Recommended | `reward` | Stamp Collection |
| FS-13 | Voucher Allocation | Conditional MVP | `reward` | Milestone Reward |
| FS-14 | Notification / Share Tracking | MVP Support | `community`, `reward` | Collection, Reward |
| FS-15 | Monetization Foundation | MVP Support / Phase 2 | `monetization` | Collection stable |
| FS-16 | Analytics / Audit / Monitoring | MVP Support | `common`, `infra`, modules | All sensitive flows |

---

## 3. FS-01 — Authentication / Session

### Business Goal

Ensure every scan, stamp, reward, admin action, and event can be linked to a verified account/session.

### Actors

- End User
- Admin
- Backend System

### Flow

1. User registers by email/password.
2. User logs in.
3. Backend returns access token and refresh token/session.
4. Mobile stores tokens securely.
5. Mobile uses access token for APIs.
6. Mobile refreshes session when access token expires.
7. User can logout.
8. Password reset invalidates unsafe sessions according to security policy.

### Input

| Operation | Input |
|---|---|
| Register | email, password, optional username/profile fields |
| Login | email/username, password, deviceFingerprint |
| Refresh | refresh token/cookie, deviceFingerprint |
| Logout | access token, refresh token/session id |
| Reset password | reset token/OTP, new password |

### Output

| Operation | Output |
|---|---|
| Register | user summary, verification state |
| Login | user summary, access token, refresh session |
| Refresh | new access token, rotated refresh session |
| Logout | success state |
| Reset password | success state; sessions invalidated if policy requires |

### Business Rules

- Anonymous users cannot collect stamps.
- Admin APIs require authenticated admin identity.
- Refresh token rotation must prevent reuse attack.
- Password reset must invalidate active sessions according to token version policy.
- Email verification before scan is a product decision; default recommendation: required before public launch, optional for internal beta.

### Data Integrity Rules

- Email/username uniqueness enforced at DB and service level.
- Refresh session must be tied to user and device fingerprint.
- Token reuse attack must revoke affected session or user sessions.
- Access token invalidation policy must be deterministic.

### Security Rules

- Passwords are hashed with a strong algorithm.
- No password/token appears in DTO, logs, audit logs, or error response.
- Access token lifetime is short.
- Refresh token is stored and rotated securely.
- Admin permissions are evaluated server-side.

### Acceptance Criteria

- Valid user can register and login.
- Invalid credentials return stable error without leaking account existence beyond policy.
- Refresh token rotation works.
- Reused refresh token is rejected.
- Logout prevents further refresh.
- Protected API rejects unauthenticated requests.
- Admin API rejects normal users.

### Failure Modes

| Case | Expected Behavior |
|---|---|
| Wrong password | Reject with stable auth error. |
| Expired access token | Reject and allow refresh flow. |
| Invalid refresh token | Reject; force login. |
| Refresh reuse attack | Revoke session/user sessions according to policy. |
| Redis unavailable | Apply configured fail-open/fail-safe policy; log critical error. |

### Metrics / Audit

- login success/fail count;
- refresh success/fail/reuse count;
- logout events;
- password reset events;
- admin login events.

---

## 4. FS-02 — RBAC / Admin Security

### Business Goal

Protect all admin and operational APIs from unauthorized usage.

### Actors

- Admin
- Super Admin
- Backend System

### Flow

1. Admin logs in.
2. Backend loads roles and permissions.
3. Admin calls protected endpoint.
4. Backend checks permission.
5. Backend audits high-impact operations.

### Input

- authenticated user id;
- role assignment request;
- permission-protected API request.

### Output

- role/permission view;
- success/failure response;
- audit log entry for sensitive changes.

### Business Rules

- Normal users cannot access admin endpoints.
- Only authorized admins can assign/revoke roles.
- Permission checks must be endpoint-level, not UI-only.
- No endpoint should rely on frontend hiding buttons.

### Data Integrity Rules

- Duplicate role assignments are ignored or rejected deterministically.
- Role/permission mappings must not be partially updated.
- Role revocation must invalidate cached permission views.

### Security Rules

- Sensitive APIs must use `@PreAuthorize` or equivalent guard.
- Permission cache must be invalidated after role/permission changes.
- Do not expose internal permission IDs unless needed.

### Acceptance Criteria

- Admin can perform permitted operation.
- Normal user receives 403.
- Unauthenticated user receives 401.
- Role assignment and revocation are idempotent or deterministic.
- Permission cache cannot return stale privilege after revocation.

### Failure Modes

| Case | Expected Behavior |
|---|---|
| Admin loses role while session active | Next protected request is rejected after cache invalidation. |
| Duplicate role assignment | No duplicate rows; stable response. |
| Concurrent assign/revoke | Final state must be consistent. |

### Metrics / Audit

- role assignment/revocation audit;
- unauthorized admin access attempts;
- permission cache invalidation events.

---

## 5. FS-03 — Line Management

### Business Goal

Allow admin to define metro lines without code deployment.

### Actors

- Admin
- Mobile App User
- Backend System

### Flow

1. Admin creates metro line.
2. Admin updates line metadata/status.
3. Public API exposes active lines.
4. Collection and stamp book use line as grouping dimension.

### Input

- line code;
- line name;
- display order;
- status;
- optional description/color/metadata.

### Output

- line summary;
- line detail;
- active line list.

### Business Rules

- Line code is unique.
- Inactive line cannot be used for new collection.
- Deleting a line with existing stations/stamps must be blocked or converted to inactive.

### Data Integrity Rules

- Unique index on line code.
- Status check constraint.
- Soft deletion/status-based deactivation preferred.
- Cache invalidation after line update.

### Security Rules

- Create/update/delete line requires admin permission.
- Public line response must not include internal audit fields unless intended.

### Acceptance Criteria

- Admin can create/update/disable line.
- Duplicate line code is rejected.
- Public API lists only active lines.
- Existing stamp data remains intact after line deactivation.

### Failure Modes

| Case | Expected Behavior |
|---|---|
| Duplicate code | 409 conflict. |
| Disable line with active stations | Either cascade status by explicit admin action or reject with validation error. |
| Public API reads stale line | Cache is evicted on write. |

### Metrics / Audit

- line created/updated/disabled audit;
- active line count.

---

## 6. FS-04 — Station Management

### Business Goal

Allow admin to define station metadata, location, radius, media, and operational state.

### Actors

- Admin
- End User
- Backend System

### Flow

1. Admin creates station under a line.
2. Admin sets coordinates and radius.
3. Admin uploads/links media and stamp design.
4. Admin activates station.
5. Mobile reads station metadata.
6. Collection uses station data for GPS and stamp book.

### Input

- line id;
- station code;
- name;
- latitude;
- longitude;
- radiusMeters;
- status;
- display order;
- station description;
- image/stamp asset references.

### Output

- station summary;
- station detail;
- active station list by line.

### Business Rules

- Station code is unique within line.
- Station must belong to an existing line.
- Station must have valid coordinates before activation.
- Radius must be bounded.
- Station with existing stamps should not be hard deleted.

### Data Integrity Rules

- FK station → line.
- Unique `(line_id, station_code)`.
- Valid latitude/longitude constraints.
- Radius min/max check constraint.
- Index for public station query by line/status.
- Cache invalidated after update.

### Security Rules

- Admin-only mutation.
- Public DTO must not expose scan keys/secrets.
- Upload path must not permit path traversal.

### Acceptance Criteria

- Admin can create/update/disable station.
- Invalid coordinates are rejected.
- Public station API excludes sensitive fields.
- Stamp book reflects updated station metadata after cache invalidation.

### Failure Modes

| Case | Expected Behavior |
|---|---|
| Station enabled without coordinates | Reject. |
| Radius too large | Reject to prevent mass cheating. |
| Delete station with existing stamps | Reject or mark inactive. |
| Asset upload invalid type | Reject. |

### Metrics / Audit

- station create/update/disable audit;
- station active count;
- station coordinate/radius changes.

---

## 7. FS-05 — Scan Key Management

### Business Goal

Map NFC/QR scan values to stations safely while supporting key rotation and abuse control.

### Actors

- Admin
- End User
- Backend System

### Flow

1. Admin creates or imports scan key for station.
2. Backend stores key status and type.
3. App scans NFC or QR.
4. Backend resolves active key to station.
5. Admin can rotate/revoke compromised key.

### Input

- station id;
- scan type: NFC or QR;
- key value or generated token config;
- status;
- validity window;
- optional rotation reason.

### Output

- admin scan key summary with masked value;
- scan resolve result;
- audit entry.

### Business Rules

- Only active scan keys are usable.
- NFC key values should be treated as secrets or semi-secrets.
- QR in production must be dynamic, short-lived, and one-time use.
- Key rotation must immediately invalidate old key for collect.

### Data Integrity Rules

- Unique active scan key value.
- Key status check constraint.
- Index on scan key hash/value for hot path.
- Do not store plaintext key if hash-based lookup is feasible; if plaintext is required, restrict exposure.
- Cache invalidated on key rotation.

### Security Rules

- Public API never returns scan key value.
- Logs redact scan values.
- Admin scan key response masks values.
- Rate limit scan resolve attempts.

### Acceptance Criteria

- Valid active NFC key resolves station.
- Revoked key fails immediately.
- QR token expires and cannot be reused.
- Logs do not expose raw key.

### Failure Modes

| Case | Expected Behavior |
|---|---|
| Key brute force attempt | Rate limited and audited. |
| Key rotated while app has old cached state | Old key rejected. |
| Duplicate key assigned | 409 conflict. |
| QR token reused | Reject with `SCAN_KEY_USED`. |

### Metrics / Audit

- key created/rotated/revoked audit;
- invalid scan attempts;
- scan resolve latency.

---

## 8. FS-06 — Public Station Data

### Business Goal

Provide mobile with safe station and stamp book metadata without leaking operational secrets.

### Actors

- End User
- Mobile App
- Backend System

### Flow

1. Mobile requests active lines/stations.
2. Backend returns public-safe metadata.
3. Mobile renders station list/detail/stamp book base data.

### Input

- optional line id;
- optional status filter for public APIs fixed to active;
- pagination if needed.

### Output

- active line list;
- active station list;
- station detail;
- asset URLs;
- collection metadata if authenticated endpoint includes progress.

### Business Rules

- Only active lines/stations exposed publicly.
- Public response excludes NFC/QR secrets.
- Station detail can include story, image, nearby places, social proof if supported.

### Data Integrity Rules

- Read from source of truth or cache with invalidation.
- Public APIs should not depend on admin-only fields.

### Security Rules

- No sensitive fields.
- Public endpoint rate-limited if abused.
- Signed/public asset URLs must be scoped correctly.

### Acceptance Criteria

- Mobile can load station list.
- Disabled stations are hidden or marked unavailable according to product rule.
- Response schema is stable for Flutter integration.

### Failure Modes

| Case | Expected Behavior |
|---|---|
| No active stations | Return empty list, not server error. |
| Asset missing | Return placeholder/fallback field. |
| Station disabled after client loaded | Collect endpoint rejects. |

### Metrics / Audit

- station list request count;
- cache hit/miss;
- public endpoint latency.

---

## 9. FS-07 — Default Campaign

### Business Goal

Support the MVP with one default campaign while preserving future multi-campaign expansion.

### Actors

- Admin
- End User
- Backend System

### Flow

1. Default campaign exists in database.
2. Collection flow auto-selects active default campaign.
3. Reward engine evaluates milestones within selected campaign.
4. Future campaigns can be added without breaking existing collection data.

### Input

- campaign code;
- campaign name;
- active window;
- default flag;
- line scope if per-line campaign is chosen.

### Output

- selected campaign id in collection context;
- campaign summary for admin/mobile if needed.

### Business Rules

- MVP uses one active default campaign.
- Future design must support multiple campaigns.
- A station may belong to campaign through campaign station mapping.
- Exact decision still required: global default campaign vs per-line default campaign.

### Recommended MVP Decision

```text
Use one global default campaign for MVP.
Keep campaign_stations table and campaign_id in user_stamps.
Do not expose campaign selection to mobile yet.
```

### Data Integrity Rules

- Only one active default campaign if global strategy is chosen.
- Campaign status and date window enforced server-side.
- Collection stores campaign id explicitly.

### Security Rules

- Admin-only mutation.
- Public/mobile cannot force arbitrary campaign id unless explicitly allowed later.

### Acceptance Criteria

- Valid collect request auto-selects default campaign.
- No active default campaign returns deterministic error.
- Expired/inactive campaign blocks collect.
- Existing stamps remain queryable after campaign ends.

### Failure Modes

| Case | Expected Behavior |
|---|---|
| Two campaigns marked default | Startup check or admin validation rejects. |
| Campaign expires during scan | Collect checks campaign at transaction time. |
| Station not part of campaign | Collect rejected. |

### Metrics / Audit

- campaign created/updated audit;
- active campaign count;
- collect count by campaign.

---

## 10. FS-08 — Scan Resolve

### Business Goal

Convert an NFC/QR scan value into a validated station context without exposing scan secrets.

### Actors

- End User
- Mobile App
- Backend System

### Flow Options

#### Option A — One-step collect endpoint

```text
Mobile scans NFC/QR + sends GPS + clientRequestId
↓
Backend resolves station
↓
Backend validates GPS/campaign/duplicate
↓
Backend writes stamp
↓
Backend returns collection result
```

#### Option B — Two-step resolve + collect

```text
Mobile scans NFC/QR
↓
Backend resolves station and eligibility
↓
Mobile shows pre-stamp screen/ad
↓
Mobile calls collect
↓
Backend validates again and writes stamp
```

### Recommended MVP Decision

Use **one-step collect endpoint** for data integrity. Add a separate resolve endpoint only for UX preview if needed, but never trust it for final collect.

### Input

```json
{
  "scanType": "NFC",
  "scanValue": "raw-or-token-value",
  "latitude": 10.0,
  "longitude": 106.0,
  "accuracyMeters": 25,
  "deviceFingerprint": "string",
  "clientRequestId": "uuid"
}
```

### Output

```json
{
  "stamp": {},
  "station": {},
  "campaign": {},
  "progress": {},
  "reward": null,
  "idempotentReplay": false
}
```

### Business Rules

- Scan type must be explicit.
- NFC key must resolve to active station.
- QR token must be valid, unexpired, and unused.
- Resolve success alone does not guarantee collect success; final collect must revalidate.

### Data Integrity Rules

- Scan resolve lookup must be indexed.
- QR token consume must be atomic.
- Scan value must be redacted from logs.
- Repeated request with same clientRequestId must be idempotent.

### Security Rules

- Rate limit scan attempts.
- Do not leak whether a scan key belongs to a specific station in error details.
- Reject unsupported scan types.

### Acceptance Criteria

- Valid NFC resolves and collects.
- Invalid key returns stable error.
- Expired QR token returns `SCAN_KEY_EXPIRED`.
- Used QR token returns `SCAN_KEY_USED`.
- Scan value is not visible in logs.

### Failure Modes

| Case | Expected Behavior |
|---|---|
| QR token expires during request | Transaction-time validation decides; if expired, reject. |
| QR token consumed by another request | Reject with used/duplicate error. |
| NFC key cloned | Key still works unless revoked; abuse detection/rate limit logs suspicious pattern. |

### Metrics / Audit

- scan resolve attempts;
- scan resolve success/failure by reason;
- invalid/expired/used token count;
- scan endpoint latency.

---

## 11. FS-09 — GPS Validation

### Business Goal

Confirm the user is physically near the station before allowing collection.

### Actors

- End User
- Mobile App
- Backend System

### Flow

1. Mobile requests GPS permission.
2. Mobile sends lat/lng/accuracy.
3. Backend loads station coordinates/radius.
4. Backend computes distance.
5. Backend accepts or rejects collect.

### Input

- latitude;
- longitude;
- accuracyMeters;
- station coordinates from backend source of truth.

### Output

- GPS validation result;
- distanceMeters if safe to return;
- error code if rejected.

### Business Rules

- Server computes distance.
- Station radius is configurable.
- GPS accuracy threshold must be defined.
- Underground station exception policy must be explicit.

### Recommended MVP Rule

```text
Allow collect if:
- distance <= station.radiusMeters
- accuracyMeters <= maxAllowedAccuracyMeters

Default radius: station-specific, seeded at 100m unless underground station requires larger configured radius.
Default max accuracy: 50m for normal stations, configurable.
```

### Data Integrity Rules

- Coordinates must be valid.
- Radius cannot be unbounded.
- Store GPS metadata on user stamp for audit/debug, but avoid exposing precise history unnecessarily.

### Security Rules

- Do not trust client-calculated distance.
- Rate limit repeated out-of-range scans.
- Avoid leaking exact anti-cheat thresholds beyond intended client UX.

### Acceptance Criteria

- In-range GPS passes.
- Out-of-range GPS rejects with `GPS_OUT_OF_RANGE`.
- Poor accuracy rejects or follows configured fallback.
- Invalid coordinates reject with validation error.

### Failure Modes

| Case | Expected Behavior |
|---|---|
| GPS permission denied | Mobile shows blocking state; backend rejects missing coordinates. |
| Location stale | Reject if timestamp support is added; otherwise rely on mobile freshness policy. |
| Underground GPS poor | Apply station-specific policy only if configured. |

### Metrics / Audit

- GPS pass/fail count;
- average distance at collect;
- out-of-range attempts by station;
- poor accuracy attempts.

---

## 12. FS-10 — Stamp Collection + Idempotency

### Business Goal

Persist a valid stamp exactly once for a user/station/campaign while handling retries and race conditions safely.

### Actors

- End User
- Mobile App
- Backend System
- Reward Engine

### Flow

1. Mobile sends scan collect request with clientRequestId.
2. Backend authenticates user.
3. Backend resolves station.
4. Backend selects active default campaign.
5. Backend validates station/campaign/GPS/duplicate.
6. Backend writes `user_stamps`.
7. Backend stores idempotency result.
8. Backend triggers reward evaluation.
9. Backend returns stamp/progress/reward summary.

### Input

- authenticated user;
- scanType;
- scanValue;
- GPS coordinates and accuracy;
- deviceFingerprint;
- clientRequestId.

### Output

- stamp id;
- station summary;
- campaign summary;
- collectedAt;
- progress;
- reward if issued or pending;
- idempotentReplay boolean.

### Business Rules

- User can collect one stamp per station per campaign in MVP.
- Same clientRequestId retry returns previous successful response if available.
- Same user/station/campaign with different request returns `STAMP_ALREADY_COLLECTED`.
- Only committed stamps count toward progress/rewards.

### Data Integrity Rules

- Unique `(user_id, station_id, campaign_id)` for MVP collection uniqueness.
- Idempotency key unique by `(user_id, client_request_id)`.
- Collection write must be transactional.
- Cache eviction after write.
- Reward evaluation must be deduped.

### Security Rules

- Scan endpoint requires authentication.
- Rate limit repeated scan attempts.
- Do not expose internal duplicate constraint details.
- Device fingerprint is advisory, not authoritative identity.

### Acceptance Criteria

- First valid scan creates stamp.
- Duplicate scan returns deterministic duplicate error.
- Same clientRequestId replay returns same success response.
- Concurrent NFC/QR scan results in one stamp only.
- Stamp Book shows new stamp immediately after cache invalidation.

### Failure Modes

| Case | Expected Behavior |
|---|---|
| Network timeout after DB commit | Retry with same clientRequestId returns previous success. |
| Two requests race | DB unique constraint prevents duplicate; response mapped to deterministic error/replay. |
| Reward evaluation fails | Stamp remains; reward retry path or pending state handles issue. |
| Cache eviction fails | Log error; avoid returning stale data if possible; next query can bypass cache after write. |

### Metrics / Audit

- collect success count;
- duplicate count;
- idempotent replay count;
- collect latency;
- station/campaign collect distribution.

---

## 13. FS-11 — Stamp Book

### Business Goal

Show user progress across stations and create retention pressure to complete the line/campaign.

### Actors

- End User
- Mobile App
- Backend System

### Flow

1. User opens stamp book.
2. Backend loads active stations for line/campaign.
3. Backend loads user stamps.
4. Backend merges station list with collection state.
5. Backend returns progress and stamp metadata.

### Input

- user id from auth;
- optional line id;
- optional campaign id, hidden for MVP default campaign.

### Output

- total stations;
- collected count;
- completion percentage;
- list of station cards with collected/uncollected state;
- collectedAt if collected;
- stamp asset URL;
- reward progress summary if available.

### Business Rules

- Uncollected stations remain visible but locked/gray in UX.
- Progress counts committed stamps only.
- Inactive stations behavior must be decided: hidden, disabled, or shown as unavailable.
- Default campaign selected automatically in MVP.

### Data Integrity Rules

- Query must not double count duplicate stamps.
- Cache key must include user, line, campaign.
- Cache invalidated after collect, station update, campaign update, stamp design update.

### Security Rules

- User can only view their own stamp book unless admin permission exists.
- No scan key data in stamp book response.

### Acceptance Criteria

- New user sees all stations as uncollected.
- After collect, station changes to collected.
- Progress count is correct.
- Response remains stable with zero stations or inactive campaign.

### Failure Modes

| Case | Expected Behavior |
|---|---|
| Station removed from active list | Product policy decides hidden/unavailable; count remains consistent. |
| Cache stale after collect | Write flow evicts cache. |
| Campaign ended | Historical stamp book remains viewable if policy allows. |

### Metrics / Audit

- stamp book view count;
- completion rate;
- average collected count per user.

---

## 14. FS-12 — Milestone Reward

### Business Goal

Issue rewards when users reach configured collection milestones.

### Actors

- End User
- Admin
- Reward Engine
- Backend System

### Flow

1. Stamp collect succeeds.
2. Reward engine counts valid stamps for user/campaign.
3. Engine finds active milestones met by count.
4. Engine checks if reward already issued.
5. Engine creates user reward.
6. If voucher reward, allocate voucher.
7. Backend returns reward summary or sends notification.

### Input

- user id;
- campaign id;
- collected count;
- milestone config;
- reward config.

### Output

- issued reward summary;
- milestone reached;
- voucher info if applicable;
- notification event.

### Business Rules

- Reward issued once per milestone/user/campaign.
- Multiple milestones may be evaluated if backfill/data correction happens.
- Reward config must be active.
- Exact MVP milestones must be confirmed.

### Recommended MVP Milestones

```text
3 stamps  -> digital sticker / internal reward
7 stamps  -> partner voucher if available
14 stamps -> completion reward if line has 14 stations
```

If station count differs, milestone numbers must be adjusted.

### Data Integrity Rules

- Unique `(user_id, milestone_id)` or `(user_id, campaign_id, milestone_id)`.
- Reward issue must be idempotent.
- Voucher allocation atomic if voucher exists.
- Lost event must be recoverable through retry or reconciliation.

### Security Rules

- Client cannot claim milestone completion.
- Client cannot choose reward.
- Admin reward config changes are audited.

### Acceptance Criteria

- User receives reward once when milestone reached.
- Duplicate reward job does not create duplicate user reward.
- Reward response or notification is visible to user.
- Reward issue failure does not corrupt stamp collection.

### Failure Modes

| Case | Expected Behavior |
|---|---|
| Reward job runs twice | Unique constraint/dedupe returns one reward. |
| Milestone changed after issue | Existing issued reward remains historically valid unless explicit migration. |
| Voucher unavailable | Create pending reward or fallback according to policy. |

### Metrics / Audit

- reward issued count by milestone;
- duplicate prevented count;
- reward issue failure count;
- reward claim/view count.

---

## 15. FS-13 — Voucher Allocation

### Business Goal

Assign real voucher codes to qualified users without duplicate allocation.

### MVP Status

Conditional. Required only if MVP uses real partner voucher codes. If beta uses mock/digital rewards, voucher pool can be deferred but schema should remain compatible.

### Actors

- Admin
- End User
- Partner/Ops
- Reward Engine

### Flow

1. Admin imports voucher codes for reward/partner/campaign.
2. User reaches milestone.
3. Reward engine locks available voucher.
4. Voucher status changes to issued/reserved.
5. User reward stores assigned voucher reference.
6. User views voucher.

### Input

- partner id;
- reward id;
- campaign id;
- voucher code;
- expiry date;
- status.

### Output

- voucher summary for admin;
- masked or full voucher code for assigned user;
- allocation result.

### Business Rules

- A voucher code can be assigned once.
- Voucher belongs to a specific reward/partner/campaign scope.
- Voucher has expiry.
- Voucher shortage policy must be confirmed.

### Recommended Voucher Shortage Policy

```text
If voucher pool is empty:
- create user_reward with status PENDING_FULFILLMENT;
- notify admin/ops;
- do not fail the stamp collect transaction.
```

### Data Integrity Rules

- Unique voucher code.
- Atomic allocation using row lock or conditional update.
- Status transitions controlled: AVAILABLE -> RESERVED/ISSUED -> REDEEMED/EXPIRED.
- No two user_rewards can point to the same voucher.

### Security Rules

- Voucher code visible only to assigned user and authorized admin.
- Voucher import must validate duplicates.
- Do not log voucher codes in plaintext.

### Acceptance Criteria

- Available voucher is assigned once.
- Concurrent users cannot receive the same code.
- Empty pool creates deterministic pending/failure state.
- User can view assigned voucher.

### Failure Modes

| Case | Expected Behavior |
|---|---|
| Concurrent allocation | One transaction wins; other retries or pending. |
| Duplicate import | Reject duplicate rows. |
| Voucher expires before user views | Show expired status; do not reassign automatically unless policy exists. |

### Metrics / Audit

- voucher pool remaining;
- voucher allocated count;
- voucher shortage count;
- voucher redemption status if available.

---

## 16. FS-14 — Notification / Share Tracking

### Business Goal

Support reward visibility and viral/share loop without overbuilding community features.

### Actors

- End User
- Mobile App
- Backend System

### Flow — Notification

1. Reward is issued.
2. Backend creates notification.
3. Mobile fetches notification list or receives push later.
4. User opens reward/stamp book.

### Flow — Share Tracking

1. User shares stamp/stamp book/reward.
2. Mobile performs native share intent.
3. Mobile reports share event to backend.
4. Backend stores event for analytics.

### Input

- notification type;
- user id;
- reward/stamp context;
- share platform;
- shared entity type/id.

### Output

- notification list;
- share event accepted response.

### Business Rules

- Notification is informational, not source of truth for reward.
- Share tracking is best-effort.
- Referral rewards are deferred unless explicitly moved into MVP.

### Data Integrity Rules

- Notification must reference existing user/reward/stamp when applicable.
- Duplicate share event from retry should be deduped if clientRequestId exists.
- Analytics should tolerate duplicate share reports if dedupe is not implemented yet.

### Security Rules

- User can only create share event for own stamp/reward.
- Do not trust client-provided user id.

### Acceptance Criteria

- User can see reward notification after reward issue.
- Share event can be logged.
- Share tracking failure does not block native sharing.

### Failure Modes

| Case | Expected Behavior |
|---|---|
| Notification creation fails | Reward remains issued; retry/log failure. |
| Share event duplicate | Deduplicate or accept with duplicate flag; do not corrupt core data. |
| User reports share for another user reward | Reject. |

### Metrics / Audit

- notification created/read count;
- share event count by platform;
- viral share conversion later if deep links added.

---

## 17. FS-15 — Monetization Foundation

### Business Goal

Reserve a clean monetization contract without compromising scan/collection correctness.

### MVP Status

MVP Support or Phase 2. Do not build full ad mediation before core scan/reward correctness is stable.

### Actors

- End User
- Admin/Ops
- Brand Partner later
- Backend System

### Flow — Minimal Foundation

1. Admin configures ad slot/advertisement if enabled.
2. Backend returns eligible pre-stamp ad metadata when appropriate.
3. Mobile displays ad according to product rule.
4. Backend validates and logs impression/click.
5. Analytics aggregates events later.

### Input

- ad slot context;
- station/campaign/user context from backend;
- impression/click clientRequestId;
- timestamp/device metadata.

### Output

- ad creative metadata;
- impression accepted response;
- click accepted response.

### Business Rules

- Pre-stamp ad must not create fake stamp eligibility.
- Impression should only be considered valid for a backend-issued ad decision.
- Client cannot send price/payout values.
- Frequency cap required before public ad rollout.

### Data Integrity Rules

- Impression event dedupe by clientRequestId/event id.
- Ad campaign active window enforced server-side.
- Do not count impression if ad slot is expired/invalid.
- Batch aggregation should not overwrite raw event integrity.

### Security Rules

- Partner pricing, payout, and campaign ownership are server-only.
- Reject impression/click for unauthorized or expired ad decision.
- Rate limit ad event endpoints.

### Acceptance Criteria

- Ad slot model exists without blocking collect.
- Impression endpoint rejects fake/expired events.
- Duplicate event does not double count if dedupe is enabled.
- Monetization can be disabled by config without breaking scan.

### Failure Modes

| Case | Expected Behavior |
|---|---|
| Ad service unavailable | Continue scan/collect if monetization is non-critical. |
| Client retries impression | Dedupe if possible. |
| Expired campaign cached on client | Backend rejects event. |
| Bot spams events | Rate limit and audit. |

### Metrics / Audit

- ad decision count;
- impression/click count;
- invalid ad event count;
- ad latency impact on scan flow.

---

## 18. FS-16 — Analytics / Audit / Monitoring

### Business Goal

Provide minimum operational visibility and forensic traceability for MVP.

### Actors

- Admin
- Ops
- Tech Lead
- Backend System

### Flow

1. Sensitive action occurs.
2. Backend writes audit log/event.
3. Metrics counters are updated or collected.
4. Admin/Ops reviews dashboards/logs.
5. Alerts trigger for critical failure rates.

### Input

- actor id;
- action;
- entity type/id;
- before/after if safe;
- request metadata;
- error details if applicable.

### Output

- audit log entry;
- metrics series;
- basic admin analytics.

### Business Rules

- Audit log required for admin changes, scan key rotation, reward config, voucher import/allocation, sensitive auth events.
- Basic analytics should include scan success/failure, top stations, reward counts, active users.
- Monitoring required before production release.

### Data Integrity Rules

- Audit logs append-only.
- Do not store sensitive secrets in audit details.
- Metrics derived from raw events should be reconcilable.

### Security Rules

- Audit logs accessible only to authorized admins/ops.
- Redact token, scan key, voucher code, password, OTP.

### Acceptance Criteria

- Admin mutation is audited.
- Scan/reward failure spikes are visible in logs/metrics.
- Production health endpoint/monitoring exists.
- Basic smoke test can verify critical flows after deploy.

### Failure Modes

| Case | Expected Behavior |
|---|---|
| Audit write fails | Critical admin action should fail or log fallback depending on action sensitivity. |
| Metrics write fails | Core transaction should not fail unless metric is business-critical. |
| Logging leaks secret | Block release; fix redaction. |

### Metrics / Audit

- auth metrics;
- scan metrics;
- reward/voucher metrics;
- admin audit metrics;
- API latency/error rate.

---

## 19. Feature Spec Exit Criteria

This document is accepted when:

- all MVP features have explicit acceptance criteria;
- each feature has data integrity rules;
- each feature has failure modes;
- unresolved decisions are marked Open or Recommended;
- no Phase 2 feature is allowed to leak into MVP without explicit tradeoff approval;
- mobile/backend contract can be derived from the specs.
