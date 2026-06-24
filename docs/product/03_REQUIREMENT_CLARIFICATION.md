# 03 — Requirement Clarification: Exotic Stamp / Metro Stamp

> Status: Draft v0.1  
> Owner: Product / Founder / Tech Lead  
> Purpose: Convert product ideas into explicit, testable business rules before feature specification and backlog creation.

---

## 1. Requirement Clarification Scope

This document clarifies requirements for the MVP and near-future expansion of Exotic Stamp.

The focus is not UI polish. The focus is correctness of:

- scan flow;
- station/campaign state;
- stamp collection;
- reward issuance;
- voucher allocation;
- admin control;
- monetization event integrity;
- mobile/backend contract.

## 2. Requirement Status Legend

| Status | Meaning |
|---|---|
| Confirmed | Treat as locked unless founder/product explicitly changes it. |
| Proposed | Recommended rule, needs confirmation. |
| Open | Missing business decision. Blocks precise implementation. |
| Deferred | Not part of MVP, keep for later phase. |

## 3. Confirmed / Current Assumptions

| Requirement | Status | Notes |
|---|---|---|
| Android-first mobile MVP | Confirmed | iOS is not first release unless separately funded/planned. |
| Backend uses Spring Boot architecture | Confirmed | Current backend already follows Spring-pragmatic DDD direction. |
| Admin controls metro data in-app | Confirmed | Lines, stations, assets, campaign/reward data should not require code deploy. |
| User collects stamps via NFC + GPS | Confirmed | QR fallback allowed. |
| Static QR is not acceptable for production | Confirmed | QR must be dynamic or tightly controlled. |
| MVP uses one default campaign | Confirmed | Backend should auto-select default campaign unless request explicitly supports campaign later. |
| Future design must support multiple collections | Confirmed | For events like daily streaks or total collection count. |
| Current MVP should prevent duplicate station stamp per campaign | Confirmed | Use unique constraint and service validation. |
| Reward must issue once per milestone/user | Confirmed | DB uniqueness required. |

## 4. Product Actors

### 4.1 End User

| Requirement | Status | Clarification |
|---|---|---|
| Register/login | Confirmed | Email/password MVP. Social login deferred unless requested. |
| Scan station | Confirmed | NFC primary, QR fallback. |
| View stamp book | Confirmed | Show collected and uncollected stations. |
| Receive reward | Confirmed | Based on milestone. |
| Share result | Proposed | MVP can support share event tracking and mobile share intent. |

### 4.2 Admin

| Requirement | Status | Clarification |
|---|---|---|
| Manage line | Confirmed | CRUD line and status. |
| Manage station | Confirmed | CRUD station, location, media, scan key, status. |
| Manage campaign | Proposed | MVP default campaign may be seeded but future admin control needed. |
| Manage reward milestone | Proposed | Admin should configure milestones without redeploy. |
| Manage voucher pool | Proposed | Required if real vouchers are used. |
| View analytics | Proposed | Basic internal analytics only for MVP. |

### 4.3 Brand Partner

| Requirement | Status | Clarification |
|---|---|---|
| Provide voucher/reward | Proposed | Needed for commercial MVP. |
| View reports | Deferred | Manual/internal export for MVP; self-service dashboard later. |
| Run sponsored campaign | Deferred | Phase 2/3. |

## 5. Authentication / Authorization Requirements

| ID | Requirement | Status | Acceptance Criteria |
|---|---|---|---|
| AUTH-01 | User can register | Confirmed | Valid email/password creates inactive or active user according to verification policy. |
| AUTH-02 | User can login | Confirmed | Returns access token and refresh token/session. |
| AUTH-03 | User can refresh session | Confirmed | Refresh token rotation/reuse protection enforced. |
| AUTH-04 | User can logout | Confirmed | Refresh session invalidated; access token invalidation policy must be defined. |
| AUTH-05 | Admin endpoints require permission | Confirmed | Sensitive admin APIs protected with RBAC. |
| AUTH-06 | Scan endpoint requires authenticated user | Confirmed | Anonymous scan is rejected. |

### Open auth questions

| Question | Blocking? |
|---|---:|
| Should logout revoke access token immediately or only refresh token? | Medium |
| Should one user be allowed multiple active devices? | Yes for device-level token policy |
| Is email verification mandatory before scan? | Product decision |

## 6. Metro Line / Station Requirements

| ID | Requirement | Status | Acceptance Criteria |
|---|---|---|---|
| METRO-01 | Admin can create line | Confirmed | Line has name/code/status and unique code. |
| METRO-02 | Admin can create station | Confirmed | Station has line, name, station code, coordinates, radius, status. |
| METRO-03 | Station has scan keys | Confirmed | NFC/QR keys must map to station safely. |
| METRO-04 | Station can be active/inactive | Confirmed | Inactive station cannot be collected. |
| METRO-05 | Public app can list active stations | Confirmed | Response excludes sensitive scan secrets. |
| METRO-06 | Admin can upload station/stamp assets | Confirmed | Uploaded assets are public-safe and stored under controlled path/bucket. |

### Station data rules

- Station code should be unique within a line.
- Coordinates must be valid latitude/longitude.
- Radius must be bounded to avoid accidental wide geofence.
- Scan secrets must be redacted from logs and public DTOs.
- Station deletion should be soft delete/status-based if user stamps exist.

### Open station questions

| Question | Blocking? |
|---|---:|
| Exact station list for MVP? | Yes |
| Default GPS radius: 100m, 150m, or station-specific? | Yes |
| Are underground stations allowed special GPS policy? | Yes |
| Should admins be able to rotate NFC tag keys? | Yes |

## 7. Scan Resolve Requirements

| ID | Requirement | Status | Acceptance Criteria |
|---|---|---|---|
| SCAN-01 | App can scan by NFC key | Confirmed | Backend resolves active NFC key to active station. |
| SCAN-02 | App can scan by QR token | Confirmed | Backend resolves QR token only if valid, unexpired, and unused. |
| SCAN-03 | Static QR is disallowed for production | Confirmed | Production QR must have TTL or rotating token. |
| SCAN-04 | Invalid scan key returns explicit error | Confirmed | Return 404/422 depending on policy; do not leak key details. |
| SCAN-05 | Scan response contains station summary | Confirmed | No sensitive fields. |
| SCAN-06 | Scan resolve should be fast | Proposed | Hot-path indexed by scan key. |

### Scan input

Minimum request fields:

```json
{
  "scanType": "NFC | QR",
  "scanValue": "string",
  "latitude": 10.0,
  "longitude": 106.0,
  "accuracyMeters": 25,
  "deviceFingerprint": "string",
  "clientRequestId": "uuid"
}
```

### Scan output

Minimum success response:

```json
{
  "station": {
    "id": "uuid",
    "name": "string",
    "lineId": "uuid",
    "lineName": "string"
  },
  "eligible": true,
  "reason": null
}
```

### Open scan questions

| Question | Blocking? |
|---|---:|
| Should scan resolve and collect be one endpoint or two-step flow? | Yes |
| Should pre-stamp ad happen before or after backend collect validation? | Yes if ads in MVP |
| Should QR token be generated by station display device or backend admin screen? | Yes for QR architecture |

## 8. GPS Validation Requirements

| ID | Requirement | Status | Acceptance Criteria |
|---|---|---|---|
| GPS-01 | Backend validates distance | Confirmed | Server computes distance, not client. |
| GPS-02 | Coordinates must be valid | Confirmed | Invalid lat/lng rejected. |
| GPS-03 | Station radius must be configurable | Proposed | Per-station radius supported. |
| GPS-04 | GPS outside radius rejects collect | Confirmed | Return explicit error code. |
| GPS-05 | GPS poor accuracy policy defined | Open | Decide whether to reject or allow fallback. |

### Proposed GPS rule

```text
Allow collect if:
- distance(userLocation, stationLocation) <= station.radiusMeters
AND
- client accuracy <= maxAllowedAccuracyMeters
```

For underground stations:

```text
Allow station-specific override:
- larger radius; or
- NFC-only fallback; or
- manual whitelist for beta testing.
```

## 9. Stamp Collection Requirements

| ID | Requirement | Status | Acceptance Criteria |
|---|---|---|---|
| COLL-01 | User can collect station stamp | Confirmed | Valid scan + GPS + active station/campaign creates user stamp. |
| COLL-02 | Duplicate collection prevented | Confirmed | Same user/station/campaign cannot create duplicate stamp. |
| COLL-03 | Collection is transactional | Confirmed | Stamp write and post-collect event must be consistent. |
| COLL-04 | Stamp Book updates after collect | Confirmed | Cache invalidated after write. |
| COLL-05 | Collection returns progress | Proposed | Response includes collected count and total count. |
| COLL-06 | Collection triggers reward evaluation | Confirmed | Reward engine evaluates after successful collect. |

### Collection business rules

A collection is valid only if:

1. user is authenticated;
2. user account is active/eligible;
3. station exists and is active;
4. campaign exists and is active/default-selected;
5. scan key is valid for that station;
6. GPS rule passes;
7. duplicate constraint passes;
8. request is not rate-limited or flagged as abuse.

### Duplicate / idempotency policy

Current recommended policy:

| Case | Response |
|---|---|
| Exact same `clientRequestId` retried after success | Return previous success response if idempotency record exists. |
| Same user/station/campaign but different request | Return `409 STAMP_ALREADY_COLLECTED`. |
| Concurrent NFC + QR request | One succeeds; the other returns deterministic duplicate response. |

This must be confirmed before mobile integration.

## 10. Stamp Book Requirements

| ID | Requirement | Status | Acceptance Criteria |
|---|---|---|---|
| BOOK-01 | User can view line/campaign stamp book | Confirmed | Shows all stations and collection status. |
| BOOK-02 | Collected station shows stamp design | Confirmed | Includes collected timestamp and station metadata. |
| BOOK-03 | Uncollected station is visible but locked/gray | Proposed | Mobile UX requirement. |
| BOOK-04 | Progress count is accurate | Confirmed | Count reflects committed stamps only. |
| BOOK-05 | Cache used safely | Proposed | Cache invalidated on collect/station update. |

## 11. Campaign Requirements

| ID | Requirement | Status | Acceptance Criteria |
|---|---|---|---|
| CAMP-01 | MVP has default campaign | Confirmed | Backend auto-selects active default campaign. |
| CAMP-02 | Campaign has active/inactive window | Proposed | Inactive/expired campaign cannot collect. |
| CAMP-03 | Future supports multiple campaigns | Confirmed | Data model must not block future campaign expansion. |
| CAMP-04 | Future supports repeatable collection events | Confirmed | Design should not hard-code one collection model forever. |

### Open campaign questions

| Question | Blocking? |
|---|---:|
| Is default campaign global or per line? | Yes |
| Can user collect stamps before campaign start? | Yes |
| What happens to old stamps when campaign ends? | Yes |

## 12. Reward / Milestone Requirements

| ID | Requirement | Status | Acceptance Criteria |
|---|---|---|---|
| REWARD-01 | Milestone is based on collected stamp count | Confirmed | Example: 3, 7, 14 stamps. |
| REWARD-02 | Reward issued once per milestone/user | Confirmed | DB unique constraint and service logic enforce this. |
| REWARD-03 | Reward can be digital or voucher | Proposed | Types must be explicit. |
| REWARD-04 | Voucher allocation must be atomic | Confirmed | No duplicate voucher code. |
| REWARD-05 | Reward response returned after collect | Proposed | If synchronous; otherwise notification/query needed. |
| REWARD-06 | Admin can configure milestones | Proposed | Avoid redeploy for reward changes. |

### Reward issue policy options

| Option | Pros | Cons |
|---|---|---|
| Synchronous reward issue inside collect transaction | Strong consistency, simple mobile response | Slower collect endpoint, voucher lock contention. |
| Async reward issue after collect event | Faster collect endpoint | Needs retry/outbox and user notification. |

Recommended MVP:

```text
Persist stamp synchronously.
Evaluate reward synchronously or transaction-after-commit with strong dedupe.
For voucher allocation, use atomic update / lock.
```

### Open reward questions

| Question | Blocking? |
|---|---:|
| What exact milestones exist in MVP? | Yes |
| Are rewards real vouchers or mock digital rewards for beta? | Yes |
| What happens when voucher pool is empty? | Yes |
| Do rewards expire? | Yes |

## 13. Voucher Pool Requirements

| ID | Requirement | Status | Acceptance Criteria |
|---|---|---|---|
| VOUCHER-01 | Admin/import process can create voucher pool | Proposed | Each code belongs to reward/partner/campaign. |
| VOUCHER-02 | Voucher code allocated once | Confirmed | Unique and atomic assignment. |
| VOUCHER-03 | Voucher has status | Proposed | AVAILABLE, RESERVED, ISSUED, REDEEMED, EXPIRED. |
| VOUCHER-04 | User can view assigned voucher | Proposed | User sees code/details after issue. |
| VOUCHER-05 | Voucher shortage handled | Open | Need fallback policy. |

## 14. Monetization Requirements

| ID | Requirement | Status | Acceptance Criteria |
|---|---|---|---|
| MON-01 | Pre-stamp ad slot exists in product flow | Proposed | Decide MVP vs Phase 2. |
| MON-02 | Impression/click tracking is backend-validated | Confirmed for future | Client cannot forge revenue-critical fields. |
| MON-03 | Affiliate banner swiper | Deferred | Phase 2. |
| MON-04 | Partner dashboard | Deferred | Phase 3. |
| MON-05 | Ad frequency cap | Proposed | Required before public ad rollout. |

### Monetization data rule

Do not trust client-provided:

- partner price;
- payout type;
- CPM/CPC/CPI values;
- campaign ownership;
- reward eligibility;
- impression validity.

## 15. Social Share / Community Requirements

| ID | Requirement | Status | Acceptance Criteria |
|---|---|---|---|
| COMM-01 | User can share stamp book or stamp result | Proposed | Mobile share intent/deep link. |
| COMM-02 | Backend tracks share event | Proposed | Platform, user, stamp/reward context. |
| COMM-03 | Referral code created for user | Deferred/Proposed | May be Phase 2. |
| COMM-04 | Referral abuse prevention | Deferred | Needed before rewards for referral. |
| COMM-05 | Notification inbox | Proposed | Useful for reward issued messages. |

## 16. Admin Requirements

### 16.1 Admin operation rules

- Admin actions must be permission-protected.
- Admin create/update/delete must be audited.
- Sensitive fields must be masked in API response and logs.
- High-impact changes should be status-based rather than hard delete.
- Admin updates to hot-path data must invalidate cache.

### 16.2 Admin feature list

| Feature | MVP? | Notes |
|---|---:|---|
| Line management | Yes | Already in backend direction. |
| Station management | Yes | Includes GPS/radius/asset/status. |
| Scan-key management | Yes | Must be secure. |
| Campaign management | Maybe | At least default campaign support. |
| Reward management | Yes/Maybe | Required if real rewards. |
| Voucher pool management | Maybe | Required if real voucher codes. |
| Analytics dashboard | Basic | Full dashboard later. |
| Partner management | Phase 2 | Needed for monetization. |

## 17. API / Mobile Contract Requirements

### 17.1 Mobile must know

- exact endpoint for scan/collect;
- retry behavior;
- error codes;
- GPS required fields;
- QR token flow;
- expected response for duplicate;
- reward response shape;
- stamp book response shape;
- auth refresh behavior;
- offline behavior.

### 17.2 Backend must provide deterministic error codes

| Error Code | Meaning |
|---|---|
| SCAN_KEY_INVALID | NFC/QR value cannot be resolved. |
| SCAN_KEY_EXPIRED | QR token expired. |
| SCAN_KEY_USED | QR token already consumed. |
| STATION_INACTIVE | Station not collectable. |
| CAMPAIGN_INACTIVE | Campaign not collectable. |
| GPS_OUT_OF_RANGE | User not within station radius. |
| GPS_ACCURACY_TOO_LOW | GPS accuracy does not meet requirement. |
| STAMP_ALREADY_COLLECTED | Duplicate station/campaign collection. |
| REWARD_ALREADY_ISSUED | Reward duplicate prevented. |
| VOUCHER_POOL_EMPTY | Reward exists but no voucher currently available. |
| RATE_LIMITED | Too many attempts. |

## 18. Edge Cases / Failure Modes

### 18.1 Scan / collect

1. User scans NFC successfully but loses network before response.
2. User retries same request multiple times.
3. User scans NFC and QR simultaneously.
4. QR token expires while request is in flight.
5. QR token is consumed by another user first.
6. GPS permission denied on mobile.
7. GPS location is stale.
8. GPS accuracy is too low.
9. Station is disabled after user opens station screen but before scan.
10. Campaign ends during scan attempt.

### 18.2 Reward

1. User hits multiple milestones after one collect due to data correction.
2. Reward job runs twice.
3. Voucher pool empty.
4. Voucher code allocation deadlocks.
5. Partner disables reward while user qualifies.
6. User deletes account after reward issuance.

### 18.3 Admin

1. Admin uploads oversized/invalid image.
2. Admin rotates scan key while client has cached old key.
3. Admin deletes station with existing stamps.
4. Admin misconfigures huge radius and enables cheating.
5. Admin changes milestone after users already collected rewards.

### 18.4 Monetization

1. Client sends impression without valid ad slot.
2. Duplicate impression event due to retry.
3. User clicks expired banner.
4. Partner campaign inactive but client cached old banner.
5. Fraud bot spams ad endpoints.

## 19. Blockers Before Feature Specification

The following decisions must be confirmed before writing final feature specs:

| Blocker | Decision Needed |
|---|---|
| MVP station scope | Which line/stations are included? |
| Campaign behavior | Global default campaign or per-line default campaign? |
| Scan endpoint design | One-step collect or two-step resolve + collect? |
| GPS policy | Radius, accuracy threshold, underground fallback. |
| Duplicate scan behavior | 409 vs idempotent replay. |
| Reward policy | Exact milestones and reward types. |
| Voucher shortage policy | Pending, fallback reward, or failure. |
| Pre-stamp ad | MVP or Phase 2? |
| Admin deletion policy | Soft delete/status vs hard delete. |
| Mobile offline policy | No offline collect vs queued request. |

## 20. Exit Criteria for Requirement Clarification

This document is accepted when:

- every MVP feature has clear business rules;
- every scan/reward failure mode has expected behavior;
- MVP vs Phase 2 is separated;
- backend/mobile API behavior is deterministic;
- data integrity constraints are known before implementation;
- open blockers are either resolved or explicitly deferred.
