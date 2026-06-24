# 06 — MVP Scope: Exotic Stamp / Metro Stamp

> Status: Draft v0.1  
> Owner: Founder / Product / Tech Lead  
> Purpose: Define exactly what is inside and outside the MVP so implementation does not drift into non-core features.

---

## 1. MVP Objective

The MVP must prove one core loop:

```text
User registers
↓
User opens station/stamp book
↓
User physically visits a metro station
↓
User scans NFC or QR
↓
Backend validates scan + GPS + campaign + duplicate rules
↓
Backend issues stamp exactly once
↓
User sees stamp book progress
↓
User reaches milestone
↓
Backend issues reward exactly once
```

Anything that does not directly support this loop is not core MVP.

---

## 2. MVP Non-Negotiables

| Area | Non-Negotiable Rule |
|---|---|
| Authentication | Scan and reward require authenticated user. |
| Admin Security | Station, scan key, campaign, reward config require RBAC. |
| Station Data | Admin controls lines/stations/assets without redeploy. |
| Scan | NFC primary, QR fallback; static QR is not acceptable for production. |
| GPS | Backend validates distance; client GPS is not trusted. |
| Collection | One stamp per user/station/campaign in MVP. |
| Idempotency | Mobile retry must not create duplicate stamp. |
| Reward | One reward per user/milestone. |
| Voucher | If real voucher is used, allocation must be atomic. |
| API Contract | Mobile must receive deterministic error codes. |
| Observability | Sensitive writes and failures must be auditable. |

---

## 3. MVP Scope Classification

### 3.1 Must Have

These are required to ship MVP safely.

| Domain | Feature | Reason |
|---|---|---|
| Auth | Register/Login/Refresh/Logout | Required identity/session foundation. |
| User | Minimal profile/status | Required for ownership and eligibility. |
| RBAC | Admin role/permission enforcement | Required to protect operations. |
| Metro | Line management | Required station grouping. |
| Metro | Station management | Required physical scan target. |
| Metro | Scan key management | Required NFC/QR mapping and rotation. |
| Metro | Public station API | Required mobile station list/detail. |
| Assets | Station/stamp public assets | Required for stamp book UX. |
| Campaign | One default active campaign | Required collection scope. |
| Collection | NFC scan collect | Primary user action. |
| Collection | QR dynamic fallback | Hardware fallback and operational backup. |
| Collection | GPS validation | Anti-cheat. |
| Collection | Stamp collection persistence | Core product value. |
| Collection | Duplicate prevention | Data integrity. |
| Collection | Idempotency | Mobile retry safety. |
| Collection | Stamp book | Retention/progress loop. |
| Reward | Milestone config/evaluation | Reward loop. |
| Reward | Reward issue once | Data integrity. |
| Reward | User reward view | User must see earned reward. |
| QA | Unit/integration/API tests for core flows | Prevent silent corruption. |
| Ops | Audit logs for sensitive actions | Traceability. |
| Ops | Basic monitoring/smoke test | Production safety. |

### 3.2 Should Have

Ship if time allows, but do not compromise Must Have correctness.

| Domain | Feature | Reason |
|---|---|---|
| Reward | Voucher pool management | Required only if real vouchers in beta/MVP. |
| Community | Reward notification | Useful user feedback, not source of truth. |
| Community | Share event tracking | Supports viral loop analytics. |
| Analytics | Basic admin analytics | Useful for founder/ops. |
| Monetization | Pre-stamp ad slot contract | Reserve future revenue path. |
| GPS | Station-specific GPS policy | Useful for underground/weak GPS stations. |
| Security | Access token denylist | Stronger immediate logout/revoke behavior. |

### 3.3 Could Have

Nice additions only after Must/Should are stable.

| Domain | Feature | Reason |
|---|---|---|
| Community | Notification inbox | Better UX but not core loop. |
| Analytics | Top station dashboard | Useful ops metric. |
| Mobile | Share image overlay | Viral UX; can be client-only. |
| Monetization | Internal partner report | Useful for business validation. |
| Reward | Digital sticker inventory | Can enrich early reward experience. |

### 3.4 Won't Have in MVP

These are explicitly excluded from MVP.

| Feature | Reason |
|---|---|
| Full ad mediation stack | Premature without DAU scale and ad ops. |
| Partner self-service dashboard | Requires reliable reporting and contracts first. |
| Billing/invoicing | Not needed before partner monetization is validated. |
| Payment gateway / wallet | Not needed for scan/reward MVP. |
| Metro card integration | External dependency too heavy. |
| Complex AR engine | Product polish, not core validation. |
| Blockchain/NFT stamp | Adds complexity without business necessity. |
| Multi-city/multi-country | Operational expansion after MVP. |
| iOS App Store release | Out of initial Android-first scope unless separately funded. |
| AI personalization | No immediate requirement. |

---

## 4. MVP Boundary by Actor

### 4.1 End User MVP

| Included | Excluded |
|---|---|
| Register/login | Social login |
| View active lines/stations | Offline station cache as source of truth |
| View stamp book | Complex map navigation |
| Scan by NFC | Background scan |
| Scan by QR fallback | Static QR production flow |
| GPS-based validation | Manual check-in without scan |
| Receive stamp | Unlimited repeat collection at same station |
| Receive milestone reward | Partner redemption integration if not ready |
| View own rewards | Transfer reward to other user |
| Share via mobile native share if simple | Referral reward payout |

### 4.2 Admin MVP

| Included | Excluded |
|---|---|
| Manage lines | Full content CMS |
| Manage stations | Bulk geospatial import unless needed |
| Manage station status | Hard delete historical station data |
| Manage scan keys | Public scan key exposure |
| Upload station/stamp assets | Advanced image processing |
| Configure default campaign | Multi-campaign marketplace |
| Configure milestones | A/B tested reward rules |
| Import voucher codes if real vouchers | Partner self-service upload |
| View basic metrics | Full BI dashboard |

### 4.3 Brand Partner MVP

| Included | Excluded |
|---|---|
| Partner reward represented in admin data | Partner login/dashboard |
| Voucher pool managed by internal admin | Automated contract/billing |
| Manual report from admin/ops | Real-time ROI dashboard |

---

## 5. MVP Runtime Scope

### 5.1 Core Runtime Flow

```text
POST /auth/login
↓
GET /public/lines + /public/stations
↓
GET /collection/stamp-book
↓
POST /collection/collect
    - authenticate user
    - resolve NFC/QR
    - select default campaign
    - validate station active
    - validate GPS
    - validate duplicate/idempotency
    - persist user_stamp
    - evict stamp book cache
    - evaluate reward
    - return collection result
↓
GET /rewards/me
```

### 5.2 Admin Runtime Flow

```text
Admin login
↓
Create/update line
↓
Create/update station
↓
Upload public asset
↓
Configure scan key
↓
Activate station
↓
Configure default campaign/milestones
↓
Import vouchers if needed
↓
Smoke test scan flow
```

---

## 6. MVP Data Scope

### 6.1 Required Data Entities

| Entity | Required in MVP? | Notes |
|---|---:|---|
| users | Yes | Existing module. |
| roles / permissions | Yes | Existing/current direction. |
| lines | Yes | Metro grouping. |
| stations | Yes | Scan target. |
| scan keys / station scan fields | Yes | Hot path. |
| campaigns | Yes | One default campaign. |
| campaign_stations | Yes | Preserve future expansion. |
| stamp_designs | Yes | Stamp book visual. |
| user_stamps | Yes | Core collection data. |
| milestones | Yes | Reward rules. |
| rewards | Yes | Reward config. |
| user_rewards | Yes | Issued rewards. |
| voucher_pool | Conditional | Required for real vouchers. |
| notifications | Should | Reward notification. |
| share_events | Should | Growth analytics. |
| advertisements | Could/Phase 2 | Only if monetization foundation is activated. |
| ad_impressions | Could/Phase 2 | Do not fake revenue data. |

### 6.2 Critical Constraints

| Constraint | Reason |
|---|---|
| unique user email/username | Prevent account ambiguity. |
| unique line code | Stable metro reference. |
| unique station code within line | Operational correctness. |
| unique active scan key | Prevent ambiguous scan resolution. |
| valid lat/lng/radius checks | Prevent invalid GPS logic. |
| unique `(user_id, station_id, campaign_id)` | Prevent duplicate stamp. |
| unique `(user_id, client_request_id)` | Idempotency. |
| unique `(user_id, milestone_id)` or campaign-scoped equivalent | Prevent duplicate reward. |
| unique voucher code and one assignment | Prevent voucher corruption. |

---

## 7. MVP API Scope

### 7.1 Required Mobile APIs

| API Area | Required Endpoint Type |
|---|---|
| Auth | register, login, refresh, logout, reset password |
| Public Metro | list lines, list stations, station detail |
| Collection | collect by scan, stamp book |
| Reward | list my rewards, reward detail |
| Share | log share event if included |
| Notification | list/read notifications if included |

### 7.2 Required Admin APIs

| API Area | Required Endpoint Type |
|---|---|
| Line | CRUD/status |
| Station | CRUD/status/asset |
| Scan Key | create/rotate/revoke/masked view |
| Campaign | default campaign view/update if admin-managed |
| Milestone/Reward | CRUD/status |
| Voucher | import/list/status if real vouchers |
| Analytics | basic counts if included |

### 7.3 Required Error Codes

| Error Code | MVP Required? |
|---|---:|
| UNAUTHENTICATED | Yes |
| FORBIDDEN | Yes |
| VALIDATION_ERROR | Yes |
| SCAN_KEY_INVALID | Yes |
| SCAN_KEY_EXPIRED | Yes for QR |
| SCAN_KEY_USED | Yes for QR |
| STATION_INACTIVE | Yes |
| CAMPAIGN_INACTIVE | Yes |
| GPS_OUT_OF_RANGE | Yes |
| GPS_ACCURACY_TOO_LOW | Yes if accuracy enforced |
| STAMP_ALREADY_COLLECTED | Yes |
| IDEMPOTENCY_CONFLICT | Yes |
| REWARD_ALREADY_ISSUED | Yes/internal |
| VOUCHER_POOL_EMPTY | Yes if real vouchers |
| RATE_LIMITED | Yes |

---

## 8. MVP Acceptance Gates

MVP is not accepted until all gates pass.

### Gate 1 — Admin Setup

- Admin can create/update line.
- Admin can create/update station with coordinates/radius.
- Admin can upload/select station/stamp asset.
- Admin can configure active scan key.
- Admin can configure/seed default campaign.
- Admin can configure milestones/rewards.

### Gate 2 — User Scan Loop

- User registers/logs in.
- User sees station list/stamp book.
- User scans valid NFC and receives stamp.
- User scans valid QR fallback and receives stamp.
- Duplicate scan cannot create duplicate stamp.
- Same request retry is idempotent.
- Out-of-range GPS is rejected.

### Gate 3 — Reward Loop

- Reaching milestone issues reward once.
- Duplicate reward job does not issue second reward.
- Voucher allocation is atomic if enabled.
- Empty voucher pool does not corrupt stamp collection.
- User can view reward.

### Gate 4 — Security / Integrity

- Normal user cannot access admin APIs.
- Scan keys are never exposed in public DTO/logs.
- Voucher codes are not leaked to unassigned users.
- Rate limiting exists for auth/scan.
- Audit logs exist for high-impact admin actions.

### Gate 5 — QA / Release

- Unit tests cover domain rules.
- Integration tests cover persistence constraints.
- API tests cover auth/scan/collect/reward.
- At least one Android NFC physical device test passes.
- Smoke test passes after deploy.
- Monitoring/logging is configured.

---

## 9. MVP Open Decisions

These must be resolved before final implementation stage planning.

| Decision | Recommended Default | Blocking? |
|---|---|---:|
| Exact station list | Use first pilot line/stations only | Yes |
| Default campaign scope | One global default campaign | Yes |
| Scan endpoint design | One-step collect endpoint | Yes |
| GPS radius | Station-specific, default 100m | Yes |
| GPS accuracy threshold | Default 50m, configurable | Yes |
| Underground fallback | Station-specific override | Yes |
| Duplicate retry response | Same `clientRequestId` returns replay; different request returns 409 | Yes |
| Reward milestones | 3/7/14 if line has 14 stations | Yes |
| Real voucher in MVP | Use mock first unless partner is locked | Yes |
| Voucher shortage | Pending fulfillment, do not fail collect | Yes if vouchers |
| Pre-stamp ad in MVP | Contract only; full activation Phase 2 | Medium |
| Email verification before scan | Required public launch; optional beta | Medium |
| Offline collect | Not supported | Yes |

---

## 10. Explicit MVP Rejection Rules

The following implementation requests should be rejected unless scope is reopened:

- Build partner dashboard before scan/reward correctness is stable.
- Build full ad mediation before DAU proof.
- Build referral rewards before abuse prevention exists.
- Build static QR production flow.
- Allow client to choose campaign/reward/payout.
- Hard delete station with historical stamps.
- Accept collect without server-side GPS validation.
- Allow duplicate stamps for same user/station/campaign in MVP.
- Return scan secrets in mobile DTO.

---

## 11. MVP Exit Criteria

MVP scope is accepted when:

- Must Have scope is approved;
- Should/Could items are explicitly optional;
- Won't Have list is accepted;
- all open decisions have an owner and deadline;
- API contracts can be generated from scope;
- stage plan can estimate effort without hidden product ambiguity.
