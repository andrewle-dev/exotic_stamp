# 02 — Product Analysis: Exotic Stamp / Metro Stamp

> Status: Draft v0.1  
> Owner: Founder / Product / Tech Lead  
> Purpose: Analyze feasibility, value, risks, constraints, monetization logic, and failure modes before writing detailed feature specs.

---

## 1. Executive Summary

Exotic Stamp is feasible as an MVP, but only if the team treats it as a hybrid product:

```text
Mobile app
+ backend platform
+ physical station infrastructure
+ anti-cheat system
+ reward / brand operation
+ growth loop
```

The technical MVP is straightforward at the CRUD/API layer, but risky in the scan, reward, and monetization layers.

The highest-risk areas are:

1. physical deployment dependency with Metro stations;
2. NFC compatibility and QR fallback integrity;
3. GPS reliability in metro environments;
4. duplicate scan and reward race conditions;
5. voucher allocation correctness;
6. fake impression / fake scan abuse;
7. unclear partner/revenue readiness.

## 2. Feasibility Check

### 2.1 Technical feasibility

| Area | Feasibility | Comment |
|---|---:|---|
| Auth / RBAC / User | High | Standard backend domain. Already partially implemented. |
| Metro line/station admin | High | Standard CRUD with media and scan-key constraints. |
| NFC scan reading | Medium | Depends on Android device support and tag format. Requires physical device testing. |
| QR fallback | High technically, Medium security | Easy to build; unsafe if static. Must be dynamic, expiring, one-time-use. |
| GPS validation | Medium | Haversine validation is simple; real-world accuracy is unstable near underground stations. |
| Collection persistence | High | Must be protected by DB constraints and idempotency behavior. |
| Reward engine | Medium | Easy if digital-only; risky when voucher pool is real and concurrent. |
| Monetization tracking | Medium | Event ingest is simple; trustworthy reporting is harder. |
| Social/referral growth | Medium | Straightforward technically; abuse prevention adds complexity. |
| Production hardening | Medium | Requires rate limit, monitoring, audit, load testing. |

### 2.2 Business feasibility

| Requirement | Feasibility | Risk |
|---|---:|---|
| Metro cooperation | Unknown | Critical blocker. Without station access, product loses real-world mechanic. |
| Brand rewards | Unknown | Without attractive rewards, retention and commercial value weaken. |
| User acquisition | Medium/Unknown | Depends on station traffic, marketing, social loop, and partner channels. |
| In-app ads | Medium | Requires enough DAU and careful UX placement. |
| Affiliate / partnership | Medium | Needs partner operations and reporting credibility. |

### 2.3 Operational feasibility

| Operation | Required capability |
|---|---|
| Install NFC tags / QR screens | Physical deployment and maintenance process. |
| Replace compromised scan keys | Admin operation + cache invalidation + audit trail. |
| Manage station coordinates/radius | Admin control with validation and review. |
| Manage reward/voucher inventory | Voucher import, lock, issue, expiry, redemption tracking. |
| Support users with failed scan | Support tooling or admin audit view. |
| Report to partners | Trusted data, deduped events, exportable metrics. |

## 3. Product Value Analysis

### 3.1 User value

Users get:

- a collection goal tied to real station visits;
- instant feedback after scan;
- visible progress across stations;
- rewards when hitting milestones;
- shareable achievements.

The product is only sticky if the reward loop is credible. A stamp-only app without meaningful rewards will likely decay after novelty fades.

### 3.2 Founder / operator value

The operator gets:

- measurable station-level engagement;
- user retention data;
- brand partnership inventory;
- ad placement opportunities;
- social growth hooks;
- potential expansion to other venues/cities.

### 3.3 Brand partner value

Brands get:

- users physically near metro stations;
- campaign exposure before/after stamp collection;
- voucher redemption measurement;
- sponsored rewards or stamps;
- station-specific targeting.

This value depends on data integrity. If scan/impression metrics are easy to fake, brand trust collapses.

## 4. Market / Growth Analysis

### 4.1 Growth loop

```text
Station visit
↓
Stamp collect
↓
Progress / reward
↓
Share / referral
↓
New user joins
↓
More scans
↓
More partner value
```

The loop is plausible, but not automatic. It requires:

- clear onboarding;
- visible station progress;
- fast scan feedback;
- social share assets that look good;
- rewards people actually want;
- station-level marketing or partner promotion.

### 4.2 Monetization streams

| Stream | MVP readiness | Notes |
|---|---:|---|
| Brand partnership / sponsored reward | High strategic priority | Best early monetization if partners are secured. |
| Pre-stamp banner / CPM | Medium | Needs careful UX and verified impression rules. |
| In-app ads / mediation | Phase 2 | Better after DAU exists. Avoid bloating MVP. |
| Affiliate swiper | Phase 2 | Needs click tracking and partner operations. |
| Seasonal campaigns | Phase 2/3 | Strong for spikes, not stable baseline. |
| Partner dashboard | Phase 3 | Build only after manual partner reporting is validated. |

## 5. Revenue Reality Check

Revenue assumptions depend heavily on:

- MAU/DAU scale;
- scans per user per day;
- brand partner pre-commitment;
- ad fill rate / eCPM;
- campaign seasonality;
- operational ability to redeem vouchers.

Critical issue: building monetization infrastructure too early is wasteful if user volume is not proven. However, completely ignoring event tracking in MVP is also bad, because retrofitting reliable analytics later will corrupt historical metrics.

Recommended compromise:

```text
MVP:
- reserve monetization domain model;
- track basic impression/click/share events safely;
- keep partner dashboard manual/internal.

Phase 2:
- enable ad placements and affiliate banners.

Phase 3:
- partner dashboard, billing, advanced reporting.
```

## 6. Technical Risk Analysis

### 6.1 Scan risk

| Risk | Impact | Mitigation |
|---|---:|---|
| NFC unsupported on device | High UX failure | QR dynamic fallback. |
| NFC tag cloned | High fraud risk | Rotatable scan keys, scan anomaly detection, admin revoke. |
| QR screenshot reused | High fraud risk | TTL + one-time token + server invalidation. |
| App retry creates duplicate stamp | High data corruption | DB unique constraint + idempotency behavior. |
| GPS inaccurate | Medium/High | Station-specific radius, fallback policy, device testing. |

### 6.2 Reward risk

| Risk | Impact | Mitigation |
|---|---:|---|
| Reward issued twice | Critical | Unique `(user_id, milestone_id)` + transactional/atomic issue. |
| Voucher code duplicated | Critical | Row lock / atomic status transition. |
| Voucher pool empty | High | Pending fulfillment status or fallback reward. |
| Async reward event lost | High | Outbox/job retry or resilient event processing. |

### 6.3 Monetization risk

| Risk | Impact | Mitigation |
|---|---:|---|
| Fake impression spam | Critical for partner trust | Server-generated ad slot/session token. |
| Client manipulates campaign/ad IDs | High | Backend validates all ad IDs and contexts. |
| Duplicate click/impression events | Medium | Deduplication window and event fingerprinting. |
| Ads hurt scan UX | Medium | Frequency cap, pre/post-stamp A/B test. |

### 6.4 Admin risk

| Risk | Impact | Mitigation |
|---|---:|---|
| Admin misconfigures station coordinates | High | Validation, preview map, audit log, staged publish. |
| Admin exposes scan secrets | Critical | Redact logs/API responses. |
| Admin deletes active station/campaign | High | Soft delete/status-based deactivation. |

## 7. Data Integrity Analysis

### 7.1 Required invariants

| Invariant | Enforcement |
|---|---|
| One user cannot collect duplicate station stamp in same campaign | DB unique constraint + domain validation. |
| One user cannot receive same milestone reward twice | DB unique constraint + atomic issue. |
| One voucher code cannot be assigned to multiple users | Row lock / atomic update. |
| Scan key maps to one active station at a time | Unique active key constraint or application enforcement. |
| Expired/inactive campaign cannot accept collection | Domain rule + DB state check. |
| Client cannot decide reward eligibility | Backend-only reward evaluation. |
| Client cannot decide ad payout fields | Backend-only monetization config. |

### 7.2 Race conditions to handle

1. NFC and QR request arrive simultaneously for same user/station/campaign.
2. User retries request after timeout while first request commits successfully.
3. Two reward evaluation jobs run concurrently after same collection event.
4. Two users reach same reward while only one voucher remains.
5. Admin disables campaign while scan requests are in flight.
6. Cache returns stale station/campaign state after admin update.

### 7.3 Recommended consistency policy

| Flow | Consistency requirement |
|---|---|
| Stamp collection | Strong consistency. Must not duplicate. |
| Reward issuance | Strong consistency for eligibility and voucher allocation. |
| Notification | Eventual consistency acceptable. |
| Analytics counters | Eventual consistency acceptable. |
| Ad impression logs | Append-only with dedupe strategy. |
| Stamp book read | Cache allowed, but invalidated immediately after collection. |

## 8. Security / Abuse Analysis

### 8.1 Threat model

| Threat | Example |
|---|---|
| Replay attack | Reuse old QR token or request payload. |
| GPS spoofing | Mock location near station. |
| NFC cloning | Copy tag content to another tag. |
| Multi-account abuse | Create many accounts to farm rewards. |
| Voucher theft | Guess or expose voucher code. |
| Partner metric fraud | Fake impressions/clicks. |
| Admin misuse | Modify rewards/campaigns without audit. |

### 8.2 Minimum controls

- Authenticated scan endpoint.
- QR token TTL and one-time consumption.
- Server-side GPS validation.
- Duplicate constraints.
- Rate limit scan endpoints.
- Device metadata and fingerprint capture.
- Audit logs for admin and sensitive scan/reward actions.
- Sensitive values redacted from logs and DTOs.
- Partner-facing reports based on backend-validated events only.

## 9. Architecture Fit

Current backend architecture is appropriate if the team follows it strictly:

```text
presentation -> application -> domain <- infrastructure
```

Recommended bounded contexts:

| Module | Responsibility |
|---|---|
| auth | Register, login, token lifecycle, password reset. |
| user | User profile/status. |
| rbac | Roles, permissions, admin access. |
| metro | Lines, stations, scan keys, public station data. |
| collection | Scan validation, stamp collection, stamp book. |
| reward | Milestones, rewards, voucher pool, user rewards. |
| monetization | Ads, affiliate banners, impressions, clicks. |
| community | Referrals, share events, notifications. |

Technical failure mode: allowing application services to directly use JPA repositories or placing business logic in controllers will destroy modular boundaries and make the project harder to test.

## 10. MVP Scope Recommendation

### 10.1 Technical MVP

Must prove:

```text
Auth
↓
Admin creates metro line/station/stamp/reward data
↓
Mobile scans NFC/QR with GPS
↓
Backend validates and persists stamp
↓
User sees Stamp Book progress
↓
Reward is issued exactly once
```

### 10.2 Commercial MVP

Must prove:

```text
At least one real or simulated brand reward
+ scan/reward analytics
+ shareable result
+ partner-reportable metrics
```

Do not overbuild full ad mediation, billing, partner self-service, or marketplace before proving scan retention.

## 11. Cost / Timeline Risk

The old estimate of a small junior team shipping broad mobile/backend/admin/social/ads/deploy scope is aggressive. The high-risk work is not API boilerplate; it is field reliability and data integrity.

Risk multipliers:

- NFC physical testing;
- underground GPS behavior;
- QR abuse prevention;
- voucher/reward concurrency;
- production monitoring;
- partner reporting trust;
- mobile retry behavior.

A realistic plan must include buffer for:

- device testing;
- station testing;
- anti-cheat review;
- migration rollback;
- security review;
- production smoke test.

## 12. Recommendation

Proceed, but only with this order:

```text
1. Lock Product Overview / Product Analysis
2. Clarify business prerequisites and blockers
3. Lock Requirement Clarification
4. Write Feature Specifications
5. Build Backlog and Dependency Map
6. Implement by stage
7. Run data-integrity-heavy QA
8. Release controlled beta before public launch
```

Do not start new implementation stages before scan/reward/idempotency requirements are written down and accepted.

## 13. Go / No-Go Criteria

### Go

Proceed to feature specification if:

- initial station scope is known;
- scan policy is agreed;
- reward policy is agreed;
- default campaign behavior is agreed;
- duplicate/idempotency behavior is agreed;
- MVP vs Phase 2 monetization is separated.

### No-Go

Do not proceed to implementation if:

- no realistic way to deploy NFC/QR at stations;
- no decision on QR dynamic token policy;
- no decision on GPS fallback;
- no decision on voucher shortage behavior;
- no decision on whether ads are in MVP or Phase 2;
- no acceptance criteria for collection and reward correctness.
