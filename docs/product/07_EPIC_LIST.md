# 07 — Epic List: Exotic Stamp / Metro Stamp

> Status: Draft v0.1  
> Owner: Product / Tech Lead / Engineering  
> Purpose: Convert MVP scope and feature specs into epics that can be assigned, implemented, tested, and accepted.

---

## 1. Epic Principles

Each epic must have:

- business objective;
- included stories;
- excluded work;
- dependencies;
- data integrity concerns;
- acceptance gate;
- backend/mobile/admin/QA ownership.

Do not create epics by UI screen only. Exotic Stamp must be planned around business invariants: scan integrity, collection uniqueness, reward uniqueness, voucher safety, and admin-controlled operations.

---

## 2. Epic Overview

| Epic ID | Epic | MVP Priority | Primary Modules | Depends On |
|---|---|---:|---|---|
| EPIC-01 | Identity & Session | P0 | `auth`, `user` | Foundation |
| EPIC-02 | RBAC & Admin Security | P0 | `rbac`, `auth` | EPIC-01 |
| EPIC-03 | Metro Data Operations | P0 | `metro` | EPIC-02 |
| EPIC-04 | Scan Key & Public Station Contract | P0 | `metro`, `collection` | EPIC-03 |
| EPIC-05 | Default Campaign & Stamp Design | P0 | `collection`, `reward`, `metro` | EPIC-03 |
| EPIC-06 | Scan / GPS / Collection Core | P0 | `collection`, `metro` | EPIC-01, 04, 05 |
| EPIC-07 | Stamp Book | P0 | `collection`, `metro` | EPIC-06 |
| EPIC-08 | Reward & Voucher Engine | P0/P1 | `reward`, `collection` | EPIC-06 |
| EPIC-09 | Mobile Integration Contract | P0 | API + Flutter | EPIC-01, 04, 06, 07, 08 |
| EPIC-10 | Operations, Audit & QA | P0 | `common`, `infra`, all modules | All P0 epics |
| EPIC-11 | Monetization Foundation | P1/Phase 2 | `monetization` | EPIC-06 stable |
| EPIC-12 | Community / Growth Foundation | P1/Phase 2 | `community` | EPIC-06, 08 |

---

## 3. EPIC-01 — Identity & Session

### Objective

Provide secure user/admin identity and mobile session lifecycle.

### Included Stories

| Story ID | Story | Priority |
|---|---|---:|
| AUTH-01 | Register by email/password | P0 |
| AUTH-02 | Login and receive session | P0 |
| AUTH-03 | Refresh token rotation | P0 |
| AUTH-04 | Logout current device | P0 |
| AUTH-05 | Forgot/reset password | P1 if already available; P0 for public launch |
| AUTH-06 | Device fingerprint session binding | P1 |
| AUTH-07 | Access token revocation policy | P1/P0 depending security requirement |

### Excluded

- Social login.
- Phone OTP login.
- Enterprise SSO.

### Dependencies

- User entity and status.
- JWT/security configuration.
- Redis/session/token infrastructure.

### Data Integrity Concerns

- Refresh token reuse attack.
- Multiple sessions per device/user.
- Token version mismatch.
- Email uniqueness.

### Acceptance Gate

- User can register/login/refresh/logout.
- Reused refresh token is rejected.
- Protected APIs reject invalid/expired tokens.
- Password reset invalidates sessions according to policy.

### QA Scope

- Happy path auth.
- Invalid credentials.
- Refresh reuse.
- Logout then refresh.
- Concurrent refresh.

---

## 4. EPIC-02 — RBAC & Admin Security

### Objective

Ensure all admin operations are permission-protected.

### Included Stories

| Story ID | Story | Priority |
|---|---|---:|
| RBAC-01 | Define admin/user roles | P0 |
| RBAC-02 | Define permissions for metro/reward/assets | P0 |
| RBAC-03 | Assign/revoke role | P0 |
| RBAC-04 | Protect admin endpoints | P0 |
| RBAC-05 | Permission cache invalidation | P0 |
| RBAC-06 | Audit role changes | P1 |

### Excluded

- Organization/team hierarchy.
- Partner role self-service.

### Dependencies

- EPIC-01.
- Global security filter.

### Data Integrity Concerns

- Stale permission cache.
- Duplicate role assignment.
- Concurrent assign/revoke.

### Acceptance Gate

- Admin can access permitted endpoints.
- Normal user receives 403.
- Role revocation takes effect deterministically.

### QA Scope

- Permission matrix tests.
- Endpoint authorization tests.
- Duplicate assignment tests.

---

## 5. EPIC-03 — Metro Data Operations

### Objective

Allow admin to control metro lines, stations, coordinates, radius, status, and public assets.

### Included Stories

| Story ID | Story | Priority |
|---|---|---:|
| METRO-01 | Create line | P0 |
| METRO-02 | Update line | P0 |
| METRO-03 | Disable line | P0 |
| METRO-04 | Create station | P0 |
| METRO-05 | Update station metadata/location/radius | P0 |
| METRO-06 | Disable station | P0 |
| METRO-07 | Upload station/stamp asset | P0/P1 |
| METRO-08 | Public active line/station listing | P0 |
| METRO-09 | Station detail | P0 |

### Excluded

- Full CMS.
- Hard delete of stations with historical data.
- Advanced geospatial routing.

### Dependencies

- EPIC-02.
- Asset storage configuration.

### Data Integrity Concerns

- Invalid coordinates.
- Radius too large.
- Station code conflict.
- Asset path leakage.
- Stale public cache.

### Acceptance Gate

- Admin can create active station with valid coordinates.
- Mobile can list active stations without scan secrets.
- Disabled station cannot be collected.

### QA Scope

- CRUD validation.
- Public DTO redaction.
- Cache invalidation after update.
- Coordinate/radius boundary tests.

---

## 6. EPIC-04 — Scan Key & Public Station Contract

### Objective

Support secure NFC/QR resolution without leaking scan secrets.

### Included Stories

| Story ID | Story | Priority |
|---|---|---:|
| SCANKEY-01 | Create NFC scan key | P0 |
| SCANKEY-02 | Rotate/revoke NFC key | P0 |
| SCANKEY-03 | Generate/validate QR dynamic token | P0 if QR fallback in MVP |
| SCANKEY-04 | Mask scan key in admin response | P0 |
| SCANKEY-05 | Redact scan value in logs | P0 |
| SCANKEY-06 | Rate limit invalid scan attempts | P1/P0 public launch |

### Excluded

- Static QR production flow.
- Client-side trusted scan validation.

### Dependencies

- EPIC-03.
- Redis/token TTL if QR dynamic.

### Data Integrity Concerns

- Duplicate scan key.
- QR token replay.
- QR token race consume.
- Key cache stale after rotation.

### Acceptance Gate

- Active NFC key resolves station.
- Revoked key cannot resolve.
- QR token expires and cannot be reused.
- Public APIs expose no scan key.

### QA Scope

- Active/revoked/expired key tests.
- Concurrent QR consume.
- Log redaction checks.

---

## 7. EPIC-05 — Default Campaign & Stamp Design

### Objective

Give collection a campaign scope and visual stamp design while keeping future multi-campaign support.

### Included Stories

| Story ID | Story | Priority |
|---|---|---:|
| CAMP-01 | Seed/create default campaign | P0 |
| CAMP-02 | Auto-select default campaign | P0 |
| CAMP-03 | Link stations to campaign | P0 |
| CAMP-04 | Configure stamp designs | P0/P1 |
| CAMP-05 | Validate campaign active window | P0 |
| CAMP-06 | Admin update campaign status | P1 |

### Excluded

- User-selected campaigns.
- Seasonal campaigns.
- Campaign marketplace.

### Dependencies

- EPIC-03.

### Data Integrity Concerns

- More than one default campaign.
- Station not part of active campaign.
- Stamp design missing.
- Expired campaign collection.

### Acceptance Gate

- Collect flow can resolve default campaign.
- Inactive campaign blocks collect.
- Stamp book can display stamp design.

### QA Scope

- No default campaign.
- Multiple default campaigns.
- Expired campaign.
- Missing stamp design fallback.

---

## 8. EPIC-06 — Scan / GPS / Collection Core

### Objective

Implement the core scan-to-stamp transaction safely.

### Included Stories

| Story ID | Story | Priority |
|---|---|---:|
| COLL-01 | One-step collect endpoint | P0 |
| COLL-02 | Resolve NFC/QR in collect flow | P0 |
| COLL-03 | Validate station/campaign active | P0 |
| COLL-04 | Validate GPS distance | P0 |
| COLL-05 | Validate GPS accuracy | P0/P1 depending final policy |
| COLL-06 | Persist user stamp | P0 |
| COLL-07 | Prevent duplicate stamp | P0 |
| COLL-08 | Idempotent retry by clientRequestId | P0 |
| COLL-09 | Evict stamp book cache after collect | P0 |
| COLL-10 | Publish post-collect event | P0 |
| COLL-11 | Stable mobile error codes | P0 |

### Excluded

- Offline collect.
- Manual admin collect.
- Unlimited repeat collection at same station.

### Dependencies

- EPIC-01.
- EPIC-04.
- EPIC-05.

### Data Integrity Concerns

- Duplicate stamps.
- Retry after timeout.
- Concurrent NFC/QR scan.
- QR consume race.
- Reward event loss.

### Acceptance Gate

- Valid scan creates exactly one stamp.
- Duplicate scan cannot create second stamp.
- Same clientRequestId returns replay.
- GPS out of range rejects collect.
- Mobile receives deterministic response.

### QA Scope

- Happy path NFC.
- Happy path QR.
- Duplicate.
- Retry.
- Concurrent requests.
- Invalid GPS.
- Inactive station/campaign.

---

## 9. EPIC-07 — Stamp Book

### Objective

Provide user-visible collection progress across stations/campaign.

### Included Stories

| Story ID | Story | Priority |
|---|---|---:|
| BOOK-01 | Get my stamp book | P0 |
| BOOK-02 | Merge stations with user stamps | P0 |
| BOOK-03 | Include progress count | P0 |
| BOOK-04 | Include stamp design/asset URL | P0 |
| BOOK-05 | Cache stamp book safely | P1 |
| BOOK-06 | Handle inactive stations policy | P1/Open |

### Excluded

- Public leaderboard.
- Advanced map UX.
- Multi-campaign UI.

### Dependencies

- EPIC-03.
- EPIC-05.
- EPIC-06.

### Data Integrity Concerns

- Double counting.
- Stale cache.
- Station status change after collection.

### Acceptance Gate

- New user sees zero collected.
- After collect, progress updates.
- No scan secrets exposed.

### QA Scope

- Empty book.
- Partial progress.
- Complete progress.
- Cache invalidation.

---

## 10. EPIC-08 — Reward & Voucher Engine

### Objective

Issue rewards once when milestones are reached and allocate vouchers safely if enabled.

### Included Stories

| Story ID | Story | Priority |
|---|---|---:|
| REWARD-01 | Configure milestone | P0 |
| REWARD-02 | Configure reward | P0 |
| REWARD-03 | Evaluate milestone after collect | P0 |
| REWARD-04 | Issue user reward once | P0 |
| REWARD-05 | Return reward summary | P0/P1 |
| VOUCHER-01 | Import voucher pool | P1 if real vouchers |
| VOUCHER-02 | Atomic voucher allocation | P1/P0 if real vouchers |
| VOUCHER-03 | Handle empty voucher pool | P1/P0 if real vouchers |
| REWARD-06 | View my rewards | P0 |

### Excluded

- Partner redemption API.
- Reward marketplace.
- Referral reward payouts.

### Dependencies

- EPIC-06.

### Data Integrity Concerns

- Duplicate rewards.
- Same voucher assigned twice.
- Reward event lost after stamp commit.
- Milestone count mismatch.

### Acceptance Gate

- Milestone reached creates one reward.
- Duplicate evaluation creates no duplicate.
- Voucher allocation is atomic if enabled.
- User can view earned reward.

### QA Scope

- Milestone hit.
- Duplicate job.
- Multiple milestones.
- Empty voucher pool.
- Concurrent voucher allocation.

---

## 11. EPIC-09 — Mobile Integration Contract

### Objective

Give Flutter app deterministic API behavior for auth, scan, GPS, stamp book, reward, retry, and errors.

### Included Stories

| Story ID | Story | Priority |
|---|---|---:|
| MOBILE-01 | Auth API contract | P0 |
| MOBILE-02 | Public station contract | P0 |
| MOBILE-03 | Collect request/response contract | P0 |
| MOBILE-04 | Error code matrix | P0 |
| MOBILE-05 | Retry/idempotency behavior | P0 |
| MOBILE-06 | Stamp book contract | P0 |
| MOBILE-07 | Reward contract | P0 |
| MOBILE-08 | GPS permission/failure behavior | P0 |
| MOBILE-09 | QR fallback behavior | P0 |
| MOBILE-10 | Physical device test matrix | P0 |

### Excluded

- iOS-specific NFC behavior.
- Offline collect queue.

### Dependencies

- EPIC-01, 03, 04, 06, 07, 08.

### Data Integrity Concerns

- Client retry creates duplicate.
- Client shows stale station/campaign state.
- Client handles 409 vs replay incorrectly.

### Acceptance Gate

- Flutter can complete end-to-end scan with NFC.
- Flutter can complete QR fallback.
- Mobile handles all expected error codes.
- Retry after timeout is safe.

### QA Scope

- Physical Android NFC.
- QR fallback.
- GPS denied/out-of-range/poor accuracy.
- Network timeout then retry.

---

## 12. EPIC-10 — Operations, Audit & QA

### Objective

Make MVP debuggable, auditable, testable, and deployable.

### Included Stories

| Story ID | Story | Priority |
|---|---|---:|
| OPS-01 | Audit admin mutations | P0 |
| OPS-02 | Audit scan key rotation | P0 |
| OPS-03 | Audit reward/voucher config | P0 |
| OPS-04 | Basic metrics for scan/reward | P0/P1 |
| OPS-05 | Rate limit auth/scan | P0/P1 |
| OPS-06 | Smoke test checklist | P0 |
| OPS-07 | Integration test migration constraints | P0 |
| OPS-08 | Load test scan hot path | P1 |
| OPS-09 | Production monitoring/logging | P0 public launch |

### Excluded

- Full data warehouse.
- Advanced anomaly detection.

### Dependencies

- All P0 epics.

### Data Integrity Concerns

- Audit logs leaking secrets.
- Metrics double-counting.
- Unobserved production failures.

### Acceptance Gate

- Sensitive operations are audited.
- Smoke test passes after deploy.
- Scan/reward failures are visible in logs/metrics.

### QA Scope

- Secret redaction.
- Error code consistency.
- Regression tests.
- Smoke tests.

---

## 13. EPIC-11 — Monetization Foundation

### Objective

Prepare ad/partner event model without letting monetization corrupt the core collection loop.

### MVP Status

P1 / Phase 2 unless founder explicitly confirms pre-stamp ad in MVP.

### Included Stories

| Story ID | Story | Priority |
|---|---|---:|
| MON-01 | Define ad slot model | P1 |
| MON-02 | Configure advertisement | P1 |
| MON-03 | Return eligible ad decision | P1 |
| MON-04 | Track impression with dedupe | P1 |
| MON-05 | Track click with validation | P1 |
| MON-06 | Frequency cap | P1 |
| MON-07 | Internal report | P2 |

### Excluded

- Full mediation SDK stack.
- Partner dashboard.
- Billing.

### Dependencies

- EPIC-06 stable.

### Data Integrity Concerns

- Fake impressions.
- Duplicate impressions.
- Client-supplied payout data.
- Expired ad cached on client.

### Acceptance Gate

- Fake/expired ad events are rejected.
- Duplicate impression does not double count if dedupe enabled.
- Ad service failure does not block collect unless product explicitly requires ad before stamp.

---

## 14. EPIC-12 — Community / Growth Foundation

### Objective

Capture share/referral/notification hooks for growth without overbuilding social features.

### Included Stories

| Story ID | Story | Priority |
|---|---|---:|
| COMM-01 | Track share event | P1 |
| COMM-02 | Create reward notification | P1 |
| COMM-03 | List notifications | P1/P2 |
| COMM-04 | Generate referral code | P2 |
| COMM-05 | Apply referral code | P2 |
| COMM-06 | Referral abuse prevention | P2/P0 if reward attached |
| COMM-07 | Seasonal campaign hook | P2 |

### Excluded

- Social feed.
- Chat/community forum.
- Referral reward payout in MVP.

### Dependencies

- EPIC-06.
- EPIC-08.

### Data Integrity Concerns

- Fake share events.
- Self-referral.
- Duplicate referral reward.
- Notification inconsistency.

### Acceptance Gate

- Share event can be logged safely.
- Reward notification does not become source of truth.
- Referral rewards are not enabled without abuse controls.

---

## 15. Epic Dependency Graph

```text
EPIC-01 Identity & Session
    ↓
EPIC-02 RBAC & Admin Security
    ↓
EPIC-03 Metro Data Operations
    ↓
EPIC-04 Scan Key & Public Station Contract
    ↓
EPIC-05 Default Campaign & Stamp Design
    ↓
EPIC-06 Scan / GPS / Collection Core
    ↓               ↓
EPIC-07 Stamp Book  EPIC-08 Reward & Voucher Engine
    ↓               ↓
EPIC-09 Mobile Integration Contract
    ↓
EPIC-10 Operations, Audit & QA

After EPIC-06 + EPIC-08 are stable:
    → EPIC-11 Monetization Foundation
    → EPIC-12 Community / Growth Foundation
```

---

## 16. Epic Exit Criteria

The epic list is accepted when:

- every MVP feature maps to an epic;
- each epic has clear dependencies;
- P0/P1/P2 split is accepted;
- excluded work is explicitly rejected for MVP;
- backlog stories can be derived from epics without ambiguity.
