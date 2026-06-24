# 09 — Dependency Map: Exotic Stamp / Metro Stamp

> Status: Draft v0.1  
> Owner: Tech Lead / Backend Lead / Product  
> Purpose: Define implementation order, module dependencies, runtime dependencies, and parallelization boundaries before technical build execution.

---

## 1. Feasibility Check

The MVP is feasible only if the team respects dependency order.

The dangerous mistake is to code visible features first, such as Stamp Book UI, reward screens, or social sharing, before locking the data and scan integrity layers. Exotic Stamp is not a normal content app. Its core risk is not UI delivery. Its core risk is whether the backend can safely prove:

```text
who scanned
what station was scanned
where the user was
which campaign was active
whether the stamp was already collected
whether a reward should be issued
whether a voucher code was safely allocated
```

Therefore, the dependency map must prioritize correctness over demo speed.

---

## 2. High-Level Product Dependency Chain

```text
Business Prerequisites
    ↓
Foundation / Auth / RBAC
    ↓
Metro Data Operations
    ↓
Scan Key Resolution
    ↓
Default Campaign + Stamp Design
    ↓
Collection Core
    ↓
Stamp Book
    ↓
Reward Engine
    ↓
Mobile Integration Contract
    ↓
Monetization Foundation
    ↓
Community / Growth
    ↓
Production Hardening
```

### Critical conclusion

Do **not** code reward, ads, or sharing before collection is deterministic and idempotent.

---

## 3. Business / Operational Dependencies

| Dependency | Required Before | Blocking Level | Reason |
|---|---|---:|---|
| Metro MOU / LOI or equivalent operational permission | Physical NFC/QR launch | Critical | Without physical station permission, NFC/QR deployment cannot be validated in the real environment. |
| Station list and canonical line data | Metro data import | Critical | App cannot build reliable Stamp Book or GPS validation without canonical station data. |
| NFC/QR deployment model | Scan key design | Critical | Static QR, dynamic QR, NFC tag ID, and rotation strategy change the backend contract. |
| Brand reward policy | Reward engine | High | Reward type and voucher fulfillment rules affect schema and transaction behavior. |
| Google Play / Android device target | Mobile integration | High | NFC behavior differs across devices and Android versions. |
| Privacy / analytics policy | Monetization + community | Medium | Impression, location, device, referral, and share tracking require explicit data policy. |

---

## 4. Module Dependency Map

| Module | Depends On | Used By | Notes |
|---|---|---|---|
| `auth` | `user`, `rbac`, Redis/JWT | all secured modules | Must be stable before admin/mobile secured flows. |
| `user` | base/common, mail infra | auth, collection, reward, community | User identity is referenced by UUID across modules. |
| `rbac` | auth/user | admin operations | Admin station/reward/asset control must be permission-gated. |
| `metro` | rbac, public asset infra | collection, mobile, admin | Owns line/station data and scan-key metadata. |
| `collection` | auth/user, metro, campaign data | reward, stamp book, monetization | Owns scan-to-stamp integrity. |
| `reward` | collection, partner/voucher tables | mobile, notification/community | Must be idempotent and atomic. |
| `monetization` | metro, collection, partner data | admin, analytics | Should not control stamp issuance. |
| `community` | auth/user, collection/reward | mobile growth features | Must not be in MVP critical path unless referral is required. |
| `common` | none | all modules | Exceptions, responses, base entity, pagination. |
| `infra` | external systems | application/domain through ports/adapters | Redis, mail, storage, queue, cache. |

---

## 5. Runtime Flow Dependencies

### 5.1 Login flow

```text
Mobile/Admin
    ↓
Auth Controller
    ↓
Auth Command Service
    ↓
User Repository / Role Query
    ↓
JWT Provider + Redis Refresh Token Store
    ↓
Access Token + Refresh Token
```

#### Blocking dependencies

- User repository ready.
- Password hashing ready.
- JWT signing config ready.
- Refresh token persistence/revocation ready.
- RBAC role lookup ready for admin routes.

---

### 5.2 Admin station creation flow

```text
Admin Login
    ↓
RBAC Permission Check
    ↓
Station Controller
    ↓
Metro Command Service
    ↓
Station Domain Validation
    ↓
Station Repository Adapter
    ↓
PostgreSQL + Cache Eviction
```

#### Must happen before scan collection

Collection cannot safely run if station status, GPS coordinates, scan keys, and stamp design references are not controlled by admin or seed data.

---

### 5.3 Scan resolve flow

```text
Mobile NFC/QR Read
    ↓
POST /api/v1/metro/scan/resolve
    ↓
MetroScanResolveService
    ↓
Station Scan Key Lookup
    ↓
Station Public View
```

#### Dependency rule

Resolve is allowed before collect only if it does not mutate collection data.

---

### 5.4 Stamp collect flow

```text
Mobile Scan Request
    ↓
Collection Controller
    ↓
Collection Command Service
    ↓
Resolve station by scan key
    ↓
Validate station active
    ↓
Validate campaign active
    ↓
Validate GPS radius / accuracy
    ↓
Validate duplicate / idempotency
    ↓
Insert user_stamps
    ↓
Evict stamp book cache
    ↓
Publish stamp-collected event
    ↓
Reward evaluation
    ↓
Return stamp + progress + reward summary
```

#### Hard dependency

Reward evaluation must not execute before the stamp insert is committed or transactionally safe.

---

### 5.5 Reward issue flow

```text
Stamp Collected Event
    ↓
Reward Command Service
    ↓
Count eligible stamps
    ↓
Find matching active milestones
    ↓
Deduplicate user_rewards
    ↓
Allocate voucher if required
    ↓
Insert user_rewards
    ↓
Create notification
```

#### Hard dependency

Voucher allocation must use database-level protection. Service-level checks alone are not sufficient.

---

### 5.6 Monetization tracking flow

```text
Ad Slot Request
    ↓
Monetization Query Service
    ↓
Select active creative
    ↓
Return creative to client
    ↓
Client displays ad
    ↓
Client sends impression/click event
    ↓
Backend validates creative status + context
    ↓
Append tracking event
    ↓
Batch aggregate later
```

#### Dependency rule

Monetization must be observational in MVP. It must not block stamp collection unless a deliberate pre-stamp ad gate is confirmed.

---

## 6. Build Order by Dependency

### 6.1 Strict sequence

| Order | Work Item | Must Finish Before |
|---:|---|---|
| 1 | Security config, auth, users, RBAC | Any admin or mobile secured endpoint |
| 2 | Metro line/station schema + admin APIs | Scan resolve, stamp book, GPS validation |
| 3 | Scan key management | Scan resolve, collect stamp |
| 4 | Default campaign + campaign station mapping | Collect stamp |
| 5 | Stamp design mapping | Stamp book and collect response |
| 6 | Collection write path | Reward engine, stamp book correctness |
| 7 | Stamp book query path | Mobile collection UX |
| 8 | Reward issue path | Milestone screen, voucher fulfillment |
| 9 | Mobile contract hardening | Flutter integration |
| 10 | Monetization tracking | Partner proof, ads MVP |
| 11 | Community/share/referral | Growth features |
| 12 | Production hardening | Go-live |

---

## 7. Work That Can Run in Parallel

| Parallel Track | Can Start When | Notes |
|---|---|---|
| Admin UI wireframe | After Feature Spec | Do not integrate until RBAC + metro API stable. |
| Flutter auth screens | After Auth API contract | Can use mock data for station list. |
| Station/stamp visual assets | After MVP scope | Does not block backend if asset URL fields are fixed. |
| Reward copy/content | After milestone policy | Does not require voucher engine implementation. |
| Load test scripts | After API paths are drafted | Can start before final implementation. |
| OpenAPI documentation | During endpoint implementation | Must stay synchronized with controller behavior. |

---

## 8. Work That Must Not Run in Parallel Prematurely

| Work | Do Not Start Before | Reason |
|---|---|---|
| Reward issuance implementation | Collection idempotency decided | Otherwise duplicate reward behavior will be wrong. |
| Voucher pool integration | Reward transaction model decided | Risk of double allocation. |
| Pre-stamp ad gate | Collection flow decision locked | Ads can introduce failure modes into core scan UX. |
| Referral reward | Reward engine stable | Referral reward is another reward path and can duplicate logic. |
| Partner dashboard | Monetization tracking schema stable | Otherwise reports will be fake or inconsistent. |

---

## 9. Data Integrity Dependency Matrix

| Data Invariant | Enforced By | Dependent Features |
|---|---|---|
| One user collects one station once per campaign unless repeat collection is explicitly allowed | DB unique constraint + command service validation | Collection, Stamp Book, Reward |
| One user receives one reward per milestone | DB unique constraint + reward service idempotency | Reward, Voucher, Notification |
| One voucher code allocated to one user reward | Row-level lock / atomic update | Reward, Partner Operations |
| Scan key maps to one active station | DB unique constraint + scan key status | Scan Resolve, Collection |
| QR token is one-time use and TTL-bound | Redis atomic consume | QR fallback, Anti-cheat |
| Impression/click references valid active creative | FK + service validation | Monetization reports |
| Referral target user is referred once | DB unique constraint | Referral program |

---

## 10. Edge Cases / Failure Modes

1. **Duplicate scan race**  
   Two requests reach backend at the same time. Service-level `exists` check can pass in both threads. DB unique constraint must be the final authority.

2. **Reward issued twice from async event retry**  
   Event listener may run twice. `user_rewards` uniqueness and idempotent reward service are mandatory.

3. **Voucher allocation collision**  
   Two users hit milestone when only one voucher remains. Row lock or atomic `UPDATE ... WHERE status = 'AVAILABLE'` is required.

4. **Station data changed while user is scanning**  
   If station is deactivated between resolve and collect, collect must validate fresh station state.

5. **Cache stale after admin update**  
   Station scan-key cache can route scans to old state if cache eviction is missing.

6. **Mobile retry after timeout**  
   User may receive a network timeout after backend committed. Retry must not create a second stamp or reward.

---

## 11. Dependency Acceptance Gate

Batch 3 dependency map is accepted only when:

- every P0 story has a known upstream dependency;
- collection and reward are not scheduled before metro/campaign data is ready;
- data invariants have DB-level enforcement points;
- async reward behavior is explicitly idempotent;
- mobile API contract declares retry behavior;
- no Phase 2 monetization/community work blocks P0 scan-to-stamp release.
