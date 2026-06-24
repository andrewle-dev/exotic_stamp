# 01 — Product Overview: Exotic Stamp / Metro Stamp

> Status: Draft v0.1  
> Owner: Founder / Product / Backend Lead  
> Purpose: Establish the canonical product intent before feature specification and implementation planning.

---

## 1. Product Identity

**Product name:** Exotic Stamp / Metro Stamp  
**Product type:** Mobile gamification platform for metro passengers  
**Primary platform:** Mobile app, Android-first MVP  
**Backend:** Spring Boot-based API server  
**Admin:** Web/admin interface for operating metro lines, stations, rewards, campaigns, and later monetization.

## 2. Product Vision

Exotic Stamp turns every metro trip into a digital stamp-collection journey. Users check in at metro stations using NFC or QR, collect digital stamps, complete station sets, share progress, and receive rewards from brand partners.

The product is not just a stamp app. It is intended to become a physical-location engagement platform combining:

- metro station check-in;
- gamified collection;
- reward milestones;
- social sharing;
- partner vouchers;
- advertising and affiliate inventory.

## 3. Business Goal

The product should create a repeatable loop:

```text
User rides metro
↓
User scans NFC / QR at station
↓
User receives e-stamp
↓
User progresses toward reward
↓
User shares collection / reward
↓
More users join
↓
Brands gain measurable exposure
↓
Metro Stamp monetizes via partner / ads / affiliate channels
```

The MVP must prove that the scan-to-stamp loop is reliable, cheat-resistant, and valuable enough for users and brand partners.

## 4. Core Problem

Metro passengers currently use stations as transit points only. There is no consumer-facing digital engagement layer that turns station visits into repeat behavior, social sharing, or brand-reward interaction.

The product attempts to solve this by:

1. making stations collectible;
2. turning each visit into a visible progress event;
3. giving users a reason to revisit stations;
4. giving brands a way to reach users at physical metro locations;
5. giving the operator/founder a measurable engagement platform.

## 5. Primary Users / Actors

| Actor | Role | Core Need |
|---|---|---|
| End User | Metro passenger / collector | Collect stamps, view progress, receive rewards, share achievements. |
| Admin | Internal operator | Manage lines, stations, scan keys, rewards, campaigns, and uploaded assets. |
| Brand Partner | Reward / ad sponsor | Reach metro users through vouchers, campaigns, ads, and affiliate placements. |
| Founder / Operator | Business owner | Track growth, retention, DAU/MAU, scan volume, reward redemption, partner value. |
| Developer / QA | Engineering team | Build, verify, and release reliable scan, reward, and anti-cheat flows. |

## 6. User Journey — MVP

### 6.1 First-time use

```text
Open app
↓
Register / login
↓
View available metro lines and stations
↓
View empty Stamp Book
↓
Choose station to collect
```

### 6.2 Collect stamp

```text
Arrive at station
↓
Tap NFC tag or scan QR fallback
↓
App sends scan key + GPS + device metadata to backend
↓
Backend validates station, campaign, GPS, duplicate status, and scan-key validity
↓
Backend persists user stamp
↓
Backend evaluates milestone reward
↓
App receives stamp result, progress, and reward if achieved
```

### 6.3 Post-collection loop

```text
User sees stamp animation
↓
User sees updated Stamp Book
↓
User gets reward if milestone reached
↓
User can share stamp/stamp book/photo overlay
↓
User continues collecting remaining stations
```

## 7. MVP Feature Set

### 7.1 Must-have MVP features

| Feature | Description |
|---|---|
| Authentication | Register, login, refresh token, logout, password reset, verified user identity. |
| Metro Line / Station Management | Admin creates and manages metro lines, stations, coordinates, media, status, scan keys. |
| NFC / QR Scan Resolve | Backend resolves NFC tag or QR token into a valid station. |
| GPS Validation | Backend validates user location against station radius. |
| Stamp Collection | User collects station stamp if all rules pass. |
| Stamp Book | User views collected/uncollected stations and progress by line/campaign. |
| Reward Milestones | User receives rewards when reaching configured collection thresholds. |
| Voucher Pool | Voucher rewards must be allocated safely without duplicate issuance. |
| Public Assets | Admin can upload station/stamp images for mobile app display. |
| Basic Admin APIs | Internal management for station/reward/campaign data. |
| Audit / Logs | Critical admin and scan operations must be traceable. |

### 7.2 Should-have MVP features

| Feature | Description |
|---|---|
| Social Share Tracking | Track when users share stamp book or reward. |
| Basic Notification | Notify user when reward is issued. |
| Pre-stamp Ad Slot | Reserve contract/API shape for future monetization event tracking. |
| Basic Analytics | Count scans, active users, top stations, rewards issued. |

### 7.3 Post-MVP features

| Feature | Description |
|---|---|
| Affiliate Banner Swiper | Partner banners with click tracking and affiliate links. |
| Advertising Platform | Direct ad inventory, sponsored stamps, impression/click reports. |
| Referral Program | Invite codes, referral rewards, leaderboard. |
| Seasonal Campaigns | Limited-time stamp sets and brand campaigns. |
| Partner Dashboard | Brand partner self-service reporting. |
| iOS App | iOS support after Android validation and Apple Developer setup. |
| Payment / Wallet Integration | MoMo, VNPay, ZaloPay, or premium features. |

## 8. Out of Scope — Initial MVP

Unless explicitly approved, the following are not part of the first technical MVP:

- payment gateway integration;
- full partner self-service dashboard;
- full ad mediation stack;
- iOS App Store release;
- professional UI/UX redesign;
- direct metro-card integration;
- multilingual support;
- machine-learning personalization;
- high-scale data warehouse / BI platform.

## 9. Business Prerequisites

The project should not be treated as a normal app-only project. It depends on offline/physical access.

### 9.1 Critical prerequisites

| Prerequisite | Why it matters | Risk if missing |
|---|---|---|
| Metro cooperation / MOU / LOI | Needed for NFC/QR placement and legitimate station presence. | Product cannot operate in real station environment. |
| Brand partner pre-commitment | Needed for meaningful rewards and early revenue. | App becomes a game with weak reward value. |
| Physical NFC / QR deployment plan | Needed to test real scan behavior. | MVP cannot validate core mechanic. |
| Device testing plan | NFC/GPS behavior varies by device. | High production failure rate. |
| Anti-cheat policy | Brands will not trust fake scans/impressions. | Revenue credibility collapses. |

## 10. Product Success Metrics

### 10.1 MVP validation metrics

| Metric | Target / Interpretation |
|---|---|
| Scan success rate | High enough that NFC/QR does not block normal users. |
| Duplicate scan rejection correctness | Duplicate attempts must not create extra stamps. |
| GPS false rejection rate | Must be acceptable, especially near underground/covered stations. |
| Stamp Book engagement | Users should return to view progress. |
| Reward issuance accuracy | Rewards must issue exactly once per eligible milestone. |
| Share rate | Proxy for viral loop. |

### 10.2 Commercial metrics

| Metric | Meaning |
|---|---|
| DAU / MAU | Growth and retention base. |
| Scans per active user | Core engagement depth. |
| Reward redemption rate | Brand partner value. |
| Ad impressions per scan | Monetization inventory. |
| Partner campaign conversion | Business viability. |

## 11. Non-negotiable Product Constraints

1. **No duplicate stamp for the same user/station/campaign unless the business explicitly enables repeatable collection.**
2. **No duplicate reward for the same milestone/user.**
3. **No duplicate voucher code allocation.**
4. **No static QR as the primary production scan method.**
5. **No trust in client-side GPS alone.**
6. **No sensitive scan key exposure in public APIs or logs.**
7. **No monetization metric that can be forged entirely from the client.**

## 12. Product Decision Notes

### 12.1 Campaign model

Current MVP assumption:

```text
One default campaign is auto-selected for normal collection.
```

Future extension:

```text
Allow multiple campaigns and repeatable collections using user_id + station_id + campaign_id + timestamp / event type.
```

### 12.2 Collection uniqueness

Current MVP assumption:

```text
A user can collect one valid stamp per station per campaign.
```

Future extension:

```text
Support many collection events per user/station/campaign for quests such as:
- 10 consecutive days at one station;
- 100 total collections;
- limited-time seasonal events.
```

### 12.3 Anti-cheat strategy

Minimum MVP anti-cheat:

```text
NFC/QR scan key
+ GPS server-side radius validation
+ duplicate constraint
+ authenticated user
+ audit metadata
```

## 13. Open Questions

| Question | Owner | Required before coding? |
|---|---|---:|
| Is there real Metro permission for NFC/QR deployment? | Founder / BD | Yes |
| Which stations are included in the first MVP? | Product / Admin | Yes |
| What radius is acceptable per station? | Product / Ops | Yes |
| Are underground stations allowed to use relaxed GPS or NFC-only fallback? | Product / Engineering | Yes |
| Are rewards real vouchers or placeholder digital rewards for MVP? | Founder / BD | Yes |
| Should duplicate scan return `409 Conflict` or replay previous success response? | Backend / Mobile | Yes |
| Is pre-stamp ad display in MVP or only reserved for Phase 2? | Founder / Product | Yes |
| What data is allowed to be shared with brand partners? | Founder / Legal | Before partner reporting |

---

## 14. Exit Criteria for Product Overview

This document is considered accepted when:

- product vision is stable;
- MVP scope is not confused with Phase 2/3;
- business prerequisites are explicitly acknowledged;
- scan/reward/data integrity constraints are accepted;
- unresolved questions are either answered or marked as blockers.
