# 04 — Feature Inventory: Exotic Stamp / Metro Stamp

> Status: Draft v0.1  
> Owner: Product / Tech Lead  
> Purpose: Convert clarified requirements into a structured feature inventory before feature specification, backlog, dependency mapping, and MVP scoping.

---

## 1. Inventory Principles

Features are grouped by business domain, not by UI screen alone.

Each feature should be classified into:

- **Core MVP** — required to prove scan-to-stamp and reward loop.
- **MVP Support** — needed to operate or test MVP safely.
- **Phase 2** — useful after MVP validation.
- **Phase 3** — scale, monetization, partner self-service, enterprise-level capability.
- **Out of Scope** — not planned unless separately approved.

## 2. Domain Map

```text
Identity & Access
├── Authentication
├── User Profile
└── RBAC

Metro Operations
├── Lines
├── Stations
├── Scan Keys
└── Public Assets

Collection
├── Scan Resolve
├── GPS Validation
├── Stamp Collect
├── Stamp Book
└── Idempotency / Anti-cheat

Rewards
├── Campaigns
├── Milestones
├── Rewards
├── Voucher Pool
└── User Rewards

Monetization
├── Pre-stamp Ad Slot
├── Advertisements
├── Impressions / Clicks
├── Affiliate Banners
└── Partner Reporting

Community / Growth
├── Share Events
├── Referral Codes
├── Referrals
├── Notifications
└── Seasonal Campaigns

Operations
├── Audit Logs
├── Analytics
├── Monitoring
├── Rate Limits
└── Release / QA
```

## 3. Feature Classification Summary

| Domain | Feature | Classification | Notes |
|---|---|---|---|
| Auth | Register | Core MVP | Required for user identity. |
| Auth | Login | Core MVP | Required before scan. |
| Auth | Refresh Token | Core MVP | Required for mobile session. |
| Auth | Logout | Core MVP | Required for session lifecycle. |
| Auth | Forgot / Reset Password | MVP Support | Already aligned with existing auth module. |
| User | User Profile | Core MVP | Minimal profile/status. |
| RBAC | Role / Permission | Core MVP | Required for admin security. |
| Metro | Line Management | Core MVP | Admin controls line data. |
| Metro | Station Management | Core MVP | Admin controls station data and location. |
| Metro | Scan Key Management | Core MVP | NFC/QR mapping and rotation. |
| Metro | Public Station Listing | Core MVP | Mobile needs active station data. |
| Metro | Station Detail | Core MVP | Mobile displays station metadata. |
| Metro | Public Asset Upload | MVP Support | Station/stamp images. |
| Collection | NFC Scan Resolve | Core MVP | Primary collection trigger. |
| Collection | QR Dynamic Token Resolve | Core MVP | Required fallback. Static QR rejected. |
| Collection | GPS Validation | Core MVP | Anti-cheat layer. |
| Collection | Stamp Collect | Core MVP | Central business flow. |
| Collection | Stamp Book | Core MVP | Main retention/progress feature. |
| Collection | Idempotency | Core MVP | Required for safe retry and duplicate control. |
| Reward | Default Campaign | Core MVP | Auto-selected for MVP. |
| Reward | Milestone Evaluation | Core MVP | Issues rewards based on progress. |
| Reward | Reward Issue | Core MVP | Must be once per milestone/user. |
| Reward | Voucher Pool | Core MVP if real vouchers | Required for real partner rewards. |
| Reward | Reward Notification | MVP Support | Can be simple. |
| Monetization | Pre-stamp Ad Slot Contract | MVP Support / Phase 2 | Reserve shape, avoid full ad stack too early. |
| Monetization | Impression Tracking | MVP Support / Phase 2 | Needed for future partner trust. |
| Monetization | Affiliate Banner | Phase 2 | After engagement exists. |
| Monetization | Ad Mediation | Phase 2/3 | Do not build before DAU. |
| Monetization | Partner Dashboard | Phase 3 | Build after manual reporting validated. |
| Community | Share Event Tracking | MVP Support | Supports viral loop analytics. |
| Community | Referral Program | Phase 2 | Abuse controls needed. |
| Community | Seasonal Stamps | Phase 2 | Growth spike feature. |
| Operations | Audit Log | Core MVP | Required for admin and sensitive flows. |
| Operations | Basic Analytics | MVP Support | Scan/reward/user counts. |
| Operations | Monitoring / Alerting | MVP Support | Production readiness. |
| Operations | Load Testing | MVP Support | Required for hot paths. |

## 4. Core MVP Inventory

### 4.1 Authentication

| Item | Description |
|---|---|
| Register | Create account by email/password. |
| Login | Authenticate and receive access/refresh token. |
| Refresh Token | Maintain mobile session securely. |
| Logout | End session. |
| Password Reset | Recover account. |
| Token Security | Reuse attack handling, device-level policy, optional access-token denylist. |

### 4.2 RBAC / Admin Access

| Item | Description |
|---|---|
| Roles | Admin/user role separation. |
| Permissions | Fine-grained admin endpoint protection. |
| Assign/Revoke Role | Admin user management. |
| Protected Admin APIs | `@PreAuthorize` or equivalent on sensitive endpoints. |

### 4.3 Metro Data

| Item | Description |
|---|---|
| Line CRUD | Admin creates and manages metro lines. |
| Station CRUD | Admin creates and manages stations. |
| Station Coordinates | Latitude, longitude, radius, accuracy policy. |
| Station Status | Active/inactive/draft if needed. |
| Scan Keys | NFC tag id, QR token/key, status, rotation metadata. |
| Station Assets | Image/stamp design/public media. |
| Public Station API | Mobile reads active stations without secrets. |

### 4.4 Collection Core

| Item | Description |
|---|---|
| Scan Resolve | Resolve NFC/QR value to station. |
| GPS Validate | Server validates distance from station. |
| Default Campaign Select | Backend selects active default campaign. |
| Collect Stamp | Persist valid user stamp. |
| Duplicate Guard | Prevent duplicate user/station/campaign stamp. |
| Idempotency | Safe retry behavior for mobile timeouts. |
| Stamp Book | Return collected/uncollected stations and progress. |
| Post-Collect Event | Trigger reward evaluation and optional analytics. |

### 4.5 Reward Core

| Item | Description |
|---|---|
| Milestone Config | Required stamp count and reward mapping. |
| Reward Evaluation | Determine if user qualifies after collect. |
| Reward Issue | Create user reward once. |
| Voucher Allocation | Assign voucher code once if applicable. |
| Reward View | User can view issued reward. |
| Reward Notification | Notify user of reward issuance. |

### 4.6 Operations / QA

| Item | Description |
|---|---|
| Audit Logs | Sensitive admin and scan/reward events. |
| Error Codes | Stable error contract for mobile. |
| Basic Metrics | Scan success/fail, top stations, reward count. |
| Rate Limit | Protect scan/auth endpoints. |
| Device Testing | Android NFC/GPS test matrix. |
| Smoke Test | Auth, station, collect, reward, stamp book. |

## 5. Phase 2 Inventory

| Feature | Description | Dependency |
|---|---|---|
| Affiliate Banner Swiper | Home banner placements with click tracking. | Auth, public assets, monetization module. |
| Pre-stamp Ad Activation | Show ad before stamp issue and track impression. | Collection, monetization event integrity. |
| Rewarded Video | Bonus content/sticker after ad view. | Ad SDK, reward rules. |
| Referral Program | Invite friend and reward user. | Auth, community, abuse checks. |
| Seasonal Campaigns | Limited-time stamps and rewards. | Campaign model, reward engine. |
| Notification Inbox | App inbox for reward and campaign events. | Community/notification module. |
| Advanced Admin Analytics | Charts for scans, DAU/MAU, top stations. | Event tracking and aggregation. |

## 6. Phase 3 Inventory

| Feature | Description | Dependency |
|---|---|---|
| Partner Dashboard | Brands view campaign performance. | Reliable analytics, partner management. |
| Billing / Invoicing | Charge partners for ads/campaigns. | Partner contracts, payment/accounting flow. |
| Full Ad Mediation | AdMob/AppLovin/Unity/IronSource/Pangle stack. | DAU scale, ad ops, privacy compliance. |
| Enterprise Campaign Tools | Multi-brand, multi-city campaigns. | Mature campaign engine. |
| Data Warehouse / BI | Analytical reporting at scale. | Stable event schema. |
| iOS App | iPhone support and App Store release. | Android MVP validation, Apple account. |
| Payment / Wallet | MoMo/VNPay/ZaloPay integrations. | Monetization use case. |

## 7. Out-of-Scope Inventory

Unless explicitly approved, these should not be included in MVP:

| Feature | Reason |
|---|---|
| Full marketplace | Premature before user/partner demand. |
| Complex AR engine | Nice-to-have; not needed for scan/reward validation. |
| Metro card integration | Requires deep external integration. |
| Blockchain/NFT stamps | Adds complexity without MVP necessity. |
| AI personalization | No immediate requirement. |
| Multi-country expansion | Requires local operations and partner model. |
| Full financial ledger | Not needed unless payments/billing go live. |

## 8. Dependency Map — High Level

```text
Auth
↓
User / RBAC
↓
Admin Metro Data
↓
Station Scan Keys + Public Station API
↓
Default Campaign
↓
Scan Resolve
↓
GPS Validation
↓
Stamp Collection
↓
Stamp Book
↓
Reward Milestone
↓
Voucher Allocation
↓
Notification / Share / Analytics
↓
Monetization / Partner Reporting
```

Hard rule:

```text
Do not build monetization, referral, or partner dashboard before scan/collection/reward correctness is stable.
```

## 9. Epic List Draft

### Epic 1 — Identity & Access

Stories:

- Register
- Login
- Refresh token
- Logout
- Forgot password
- Reset password
- User session validation
- Admin permission enforcement

### Epic 2 — Metro Admin Foundation

Stories:

- Create line
- Update line
- Disable line
- Create station
- Update station
- Disable station
- Upload station asset
- Rotate scan key
- List public stations

### Epic 3 — Scan & Collection

Stories:

- Resolve NFC scan
- Resolve QR token
- Validate GPS
- Collect stamp
- Prevent duplicate stamp
- Handle idempotent retry
- Return collection result
- Return stamp book

### Epic 4 — Campaign & Reward

Stories:

- Seed/select default campaign
- Configure milestone
- Evaluate milestone after collect
- Issue user reward once
- Allocate voucher once
- Handle voucher shortage
- View user reward
- Notify user

### Epic 5 — Mobile Integration Contract

Stories:

- Define scan request/response
- Define error codes
- Define retry behavior
- Define offline/no-network behavior
- Define stamp book contract
- Define reward contract
- Define auth refresh behavior

### Epic 6 — Operations & QA

Stories:

- Audit admin changes
- Rate limit scan/auth
- Add basic metrics
- Add smoke test checklist
- Add device test matrix
- Add load test for scan path
- Add production monitoring

### Epic 7 — Monetization Foundation

Stories:

- Define ad slot model
- Track impression event
- Track click event
- Protect event dedupe
- Add affiliate banner model
- Add internal partner report

### Epic 8 — Community / Growth

Stories:

- Track share event
- Generate referral code
- Apply referral code
- Reward referral after condition
- Create notification inbox
- Seasonal campaign hooks

## 10. Suggested MVP Backlog Cut

### Must ship

```text
Auth
RBAC
Metro Line/Station Admin
Station Public API
NFC/QR Scan Resolve
GPS Validation
Stamp Collection
Stamp Book
Default Campaign
Milestone Reward
Voucher-safe Reward Issue if real vouchers
Audit Logs
Basic Tests
Mobile API Contract
```

### Should ship if time allows

```text
Share Event Tracking
Basic Notification
Basic Admin Analytics
Pre-stamp Ad Slot Contract
Idempotency Replay Response
Station-specific GPS policy
```

### Defer

```text
Affiliate Swiper
Full Ad Mediation
Partner Dashboard
Referral Rewards
Seasonal Campaigns
Billing
iOS
Payment Gateway
```

## 11. Feature Specification Priority Order

Write feature specs in this exact order:

1. Authentication / Session
2. RBAC / Admin Security
3. Line Management
4. Station Management
5. Scan Key Management
6. Public Station Data
7. Default Campaign
8. Scan Resolve
9. GPS Validation
10. Stamp Collection / Idempotency
11. Stamp Book
12. Milestone Reward
13. Voucher Allocation
14. Notification / Share Tracking
15. Monetization Foundation
16. Analytics / Audit / Monitoring

Reason:

```text
Each later feature depends on data and rules from the previous features.
```

## 12. Data Integrity Flags by Feature

| Feature | Critical data risk |
|---|---|
| Auth | Token reuse, stolen refresh token, weak logout policy. |
| Station | Wrong coordinates, leaked scan keys, stale cache. |
| Scan Resolve | QR replay, NFC clone, invalid key enumeration. |
| Collection | Duplicate stamps, retry race, stale campaign state. |
| Reward | Duplicate reward, lost event, wrong milestone count. |
| Voucher | Same voucher assigned to multiple users. |
| Monetization | Fake impressions/clicks, duplicate events. |
| Referral | Self-referral, farmed accounts, duplicate referral reward. |
| Analytics | Double counting, stale aggregation, partner trust loss. |

## 13. Exit Criteria for Feature Inventory

This document is accepted when:

- each feature is classified into MVP/Phase 2/Phase 3/out-of-scope;
- all core dependencies are visible;
- feature spec writing order is accepted;
- MVP backlog cut is approved;
- deferred features are explicitly not allowed to leak into MVP implementation.
