# Exotic Stamp Mobile UI Screen Inventory

> Canonical screen-to-feature implementation map for the Flutter mobile app.
> Use this file before generating or editing Flutter UI code.

---

## 1. Brand System

| Token | HEX | Flutter constant |
|---|---:|---|
| Primary Blue | `#01599D` | `AppColors.primaryBlue` |
| Accent Red | `#E83B28` | `AppColors.accentRed` |
| Background White | `#FFFFFF` | `AppColors.backgroundWhite` |
| Text Primary | `#1D2433` | `AppColors.textPrimary` |
| Text Secondary | `#667085` | `AppColors.textSecondary` |
| Border | `#E4E7EC` | `AppColors.border` |
| Surface | `#F8FAFC` | `AppColors.surface` |

### Brand color enforcement

- **Primary Blue:** `#01599D` — not `#09599E` or other variants.
- **Accent Red:** `#E83B28`
- **Background White:** `#FFFFFF`
- Current Flutter `AppColors.brandBlue` (`#09599E`) is **incorrect** and must be fixed in **M0 Foundation** before feature UI implementation.
- Use `AppColors.primaryBlue`, `AppColors.accentRed`, `AppColors.backgroundWhite` as canonical names going forward.

---

## 2. NFC-First Policy (Strict)

Exotic Stamp is **NFC-first**. For **MVP**, NFC is the **only visible collect flow**. QR collect remains **gated / intentionally hidden** and must not be restored by visual polish tasks.

| Rule | Requirement |
|------|-------------|
| Default scan mode | **NFC** — never QR-first |
| MVP visible collect | **NFC only** |
| Primary UI copy | **"Chạm NFC"** / NFC tap language |
| QR collect | Gated (`ENABLE_QR_FLOW` / `ScanCapabilities.enableQrFlow`); not a public default path |
| QR copy & placement | Do not surface QR collect CTAs while gated; if re-enabled later, secondary fallback only |
| Scan implementation | Do **not** implement scan as QR-first |
| Visual hierarchy | NFC tap affordance dominates; QR must not be the default tab or hero |
| Visual polish | Must **not** restore QR collect UI |

Applies to screens: Welcome, Home (scan CTA), Tap To Collect, Scan, Location Verification, Station Detail (collect CTA), Stamp Collected Success.

---

## 3. Design Asset Paths

All Visily exports live under `docs/design/visily/` (flat — no nested subfolders).

| Screen | Design path |
|--------|-------------|
| Welcome | `docs/design/visily/visily-welcome.png` |
| Home | `docs/design/visily/visily-home.png` |
| Stamp Book | `docs/design/visily/visily-stamp-book.png` |
| Scan | `docs/design/visily/visily-scan.png` |
| Stations List | `docs/design/visily/visily-stations-list.png` |
| Station Detail | `docs/design/visily/visily-station-detail.png` |
| Rewards | `docs/design/visily/visily-rewards.png` |
| Profile | `docs/design/visily/visily-profile.png` |
| Location Verification | `docs/design/visily/visily-location-verification.png` |
| Stamp Collected Success | `docs/design/visily/visily-stamp-collected-success.png` |
| Scan Error | `docs/design/visily/visily-scan-error.png` |
| Stamp Detail | `docs/design/visily/visily-stamp-detail.png` |
| Voucher Detail | `docs/design/visily/visily-voucher-detail.png` |
| Photo Share | `docs/design/visily/visily-photo-share.png` |
| Tap To Collect | `docs/design/visily/visily-tap-to-collect.png` |
| Collect & Share Rewards | `docs/design/visily/visily-collect-&-share-rewards.png` |
| Full deck (PDF) | `docs/design/visily/exotic-stamp-multiscreens.pdf` |

---

## 4. Bottom Navigation

MVP bottom navigation (canonical, 2026-07-18):

```text
Home
Stamp (Book)
Scan FAB (center)
Stations
Profile
```

**Rewards is not a bottom-nav tab.** It is a **secondary screen** reached from:

- Home → Claim Rewards shortcut
- Stamp Collected Success / Reward CTA when available (reward unlock still backend-contract dependent — do not fake)
- Profile entry if applicable

Implementation target:

```text
lib/features/app_shell/presentation/screens/main_shell_screen.dart
lib/features/app_shell/presentation/widgets/bottom_nav_bar.dart
```

Rules:

- Scan can be center action / floating action depending on design.
- Active tab uses `#01599D`.
- Scan/collect action may use `#E83B28`.
- Do not put backend calls in the shell widget.
- Do not add a Rewards tab without a new product decision.
- MVP collect UI is **NFC only**; QR collect stays gated and must not be restored by visual polish.

---

## 5. Screen Inventory

### 01. Welcome / Onboarding

| Field | Value |
|---|---|
| Design file | `docs/design/visily/visily-welcome.png` |
| Route | `/welcome` |
| Feature | `onboarding` |
| Flutter screen | `WelcomeScreen` |
| Source path | `lib/features/onboarding/presentation/screens/welcome_screen.dart` |
| Purpose | Explain app value before auth or first use. |
| API | None |

States:

- first_launch
- completed_onboarding

Implementation notes:

- NFC-first copy: “Chạm NFC để nhận Stamp”.
- Do not make QR the hero mechanic.

---

### 02. Home Dashboard

| Field | Value |
|---|---|
| Design file | `docs/design/visily/visily-home.png` |
| Route | `/home` |
| Feature | `home` |
| Flutter screen | `HomeScreen` |
| Source path | `lib/features/home/presentation/screens/home_screen.dart` |
| Purpose | User dashboard: progress, recent stamps, CTA to scan, station/reward shortcuts. |
| API | `GET /api/v1/mobile/home` or composed from collection/reward/station APIs |

UI sections:

- Sponsor banner / offer banner
- Collection progress card
- Recently collected stamps
- Main scan CTA
- Shortcut cards
- Social proof strip

States:

- loading
- loaded
- empty_recent_stamps
- network_error
- unauthenticated

Backend contract required:

```text
Home summary DTO:
- userDisplayName
- collectedCount
- totalStations
- nextReward
- recentStamps[]
- activeBanner
- socialProof
```

---

### 03. Stamp Book

| Field | Value |
|---|---|
| Design file | `docs/design/visily/visily-stamp-book.png` |
| Route | `/stamp-book` |
| Feature | `stamp_book` |
| Flutter screen | `StampBookScreen` |
| Source path | `lib/features/stamp_book/presentation/screens/stamp_book_screen.dart` |
| Purpose | Grid of collected/uncollected station stamps. |
| API | `GET /api/v1/mobile/stamp-book?lineId={lineId}` |

UI sections:

- Collection status card
- Line filters
- Stamp grid
- Empty/help footer

States:

- loading
- loaded
- empty
- line_filtering
- error

Critical rule:

- Collected/uncollected state must come from backend, not local inference.

---

### 04. NFC / QR Scan

| Field | Value |
|---|---|
| Design file | `docs/design/visily/visily-scan.png` |
| Route | `/scan` |
| Feature | `scan` |
| Flutter screen | `ScanScreen` |
| Source path | `lib/features/scan/presentation/screens/scan_screen.dart` |
| Purpose | Collect station stamp. NFC is primary; for MVP NFC is the only visible collect flow. QR collect is gated. |
| API | `POST /api/v1/mobile/collection/collect` or backend-defined equivalent |

Input to backend:

```json
{
  "scanType": "NFC",
  "scanKey": "...",
  "latitude": 10.0,
  "longitude": 106.0,
  "accuracyMeters": 25,
  "deviceFingerprint": "...",
  "idempotencyKey": "uuid"
}
```

States:

- waiting_nfc
- qr_fallback_available
- checking_gps
- resolving_station
- pre_stamp_ad
- collecting
- success
- duplicate
- expired_qr
- invalid_tag
- gps_outside_range
- network_error
- permission_denied

Critical rules:

- Default scan mode must be **NFC**, not QR.
- MVP: NFC is the **only visible collect flow**; QR collect stays gated — do not restore via visual polish.
- Primary copy: **"Chạm NFC"**; do not surface QR collect CTAs while gated.
- Do not implement scan as QR-first.
- Do not mark collection success until backend confirms.
- Every collect request must include idempotency key.
- NFC copy and UX must dominate any future QR fallback.

---

### 05. Stations List

| Field | Value |
|---|---|
| Design file | `docs/design/visily/visily-stations-list.png` |
| Route | `/stations` |
| Feature | `stations` |
| Flutter screen | `StationsListScreen` |
| Source path | `lib/features/stations/presentation/screens/stations_list_screen.dart` |
| Purpose | Discover stations, filter by line, see distance and collected state. |
| API | `GET /api/v1/mobile/stations?lineId=&nearby=&query=` |

States:

- loading
- loaded
- empty_search
- gps_disabled
- error

---

### 06. Station Detail

| Field | Value |
|---|---|
| Design file | `docs/design/visily/visily-station-detail.png` |
| Route | `/stations/:stationId` |
| Feature | `stations` |
| Flutter screen | `StationDetailScreen` |
| Source path | `lib/features/stations/presentation/screens/station_detail_screen.dart` |
| Purpose | Station content, nearby places, social proof, collect CTA. |
| API | `GET /api/v1/mobile/stations/{stationId}` |

UI sections:

- Hero station image
- Line/station label
- Social proof
- Direction/favorite/virtual tour actions
- Station story
- Nearby places
- Collect station stamp CTA

States:

- loading
- loaded
- not_found
- inactive_station
- error

---

### 07. Rewards

| Field | Value |
|---|---|
| Design file | `docs/design/visily/visily-rewards.png` |
| Route | `/rewards` |
| Feature | `rewards` |
| Flutter screen | `RewardsScreen` |
| Source path | `lib/features/rewards/presentation/screens/rewards_screen.dart` |
| Purpose | Milestone progress and available vouchers. |
| API | `GET /api/v1/mobile/rewards` |
| Navigation | **Secondary screen — not a bottom-nav tab** (decided 2026-07-18) |

Entry points:

- Home → Claim Rewards shortcut
- Stamp Collected Success / Reward CTA when available (unlock celebration still **BACKEND_CONTRACT_REQUIRED** — do not fake)
- Profile entry if applicable

Chrome:

- Use secondary / back header (`AppScreenHeader.secondary`), not a top-level tab header.
- Bottom nav remains Home \| Stamp \| Scan FAB \| Stations \| Profile (no Rewards highlight required).

States:

- loading
- loaded
- no_rewards_yet
- reward_pending
- error

Critical rules:

- Reward availability must come from backend.
- Claim/redeem cannot be local-only.
- Do not add Rewards to bottom nav without a new product decision.

---

### 08. Profile

| Field | Value |
|---|---|
| Design file | `docs/design/visily/visily-profile.png` |
| Route | `/profile` |
| Feature | `profile` |
| Flutter screen | `ProfileScreen` |
| Source path | `lib/features/profile/presentation/screens/profile_screen.dart` |
| Purpose | Profile, stats, memories, achievements, settings entry. |
| API | `GET /api/v1/mobile/profile` |

States:

- loading
- loaded
- unauthorized
- error

---

### 09. Location Verification

| Field | Value |
|---|---|
| Design file | `docs/design/visily/visily-location-verification.png` |
| Route | `/scan/location-verification` |
| Feature | `scan` |
| Flutter screen | `LocationVerificationScreen` |
| Source path | `lib/features/scan/presentation/screens/location_verification_screen.dart` |
| Purpose | GPS permission/checking state before stamp collection. |
| API | Usually none directly; coordinates are sent to collect API. |

States:

- requesting_permission
- checking_accuracy
- gps_high_accuracy
- gps_low_accuracy
- outside_station_area
- permission_denied
- service_disabled

Critical rule:

- Client GPS is advisory input. Backend must validate final range.

---

### 10. Stamp Collected Success

| Field | Value |
|---|---|
| Design file | `docs/design/visily/visily-stamp-collected-success.png` |
| Route | `/scan/success` |
| Feature | `scan` / `stamp_book` |
| Flutter screen | `StampCollectedSuccessScreen` |
| Source path | `lib/features/scan/presentation/screens/stamp_collected_success_screen.dart` |
| Purpose | Confirmation after successful backend collection. |
| API | No new API; renders collect response. |

Required response data:

- stationName
- stampImageUrl
- collectedAt
- collectedCount
- totalStations
- nextReward
- newlyIssuedReward nullable

> **API gap (2026-07-17):** `POST /api/v1/collection/collect` / `StampCollectResponse` does **not** return `newlyIssuedReward` or `nextReward`. Rewards are issued asynchronously (`StampCollectedEvent`). Success screen must not fake unlock until the contract adds a field (or an agreed poll/push path).
Actions:

- View Stamp Book
- Share
- Scan Next

---

### 11. Scan Error

| Field | Value |
|---|---|
| Design file | `docs/design/visily/visily-scan-error.png` |
| Route | `/scan/error` |
| Feature | `scan` |
| Flutter screen | `ScanErrorScreen` |
| Source path | `lib/features/scan/presentation/screens/scan_error_screen.dart` |
| Purpose | Backend-driven scan failure states. |
| API | Renders failed collect response / domain error. |

Error states:

- `QR_EXPIRED`
- `GPS_OUTSIDE_RANGE`
- `STAMP_DUPLICATE`
- `NFC_INVALID`
- `STATION_INACTIVE`
- `CAMPAIGN_INACTIVE`
- `NETWORK_ERROR`

Rules:

- Duplicate stamp is not a crash. Show useful copy and actions.
- Expired QR is fallback-specific; avoid making it the main product language.

---

### 12. Stamp Detail

| Field | Value |
|---|---|
| Design file | `docs/design/visily/visily-stamp-detail.png` |
| Route | `/stamps/:stampId` |
| Feature | `stamp_book` |
| Flutter screen | `StampDetailScreen` |
| Source path | `lib/features/stamp_book/presentation/screens/stamp_detail_screen.dart` |
| Purpose | Detail of a collected stamp. |
| API | `GET /api/v1/mobile/stamps/{stampId}` |

States:

- loading
- loaded
- not_found
- error

---

### 13. Voucher Detail

| Field | Value |
|---|---|
| Design file | `docs/design/visily/visily-voucher-detail.png` |
| Route | `/rewards/vouchers/:voucherId` |
| Feature | `rewards` |
| Flutter screen | `VoucherDetailScreen` |
| Source path | `lib/features/rewards/presentation/screens/voucher_detail_screen.dart` |
| Purpose | Show voucher details and redeem code. |
| API | `GET /api/v1/mobile/rewards/vouchers/{voucherId}` + `POST /api/v1/mobile/rewards/vouchers/{voucherId}/redeem` |

States:

- available
- redeeming
- redeemed
- expired
- unavailable
- error

Critical rule:

- Redeem success must be confirmed by backend. Do not mark used locally only.

---

### 14. Photo Share

| Field | Value |
|---|---|
| Design file | `docs/design/visily/visily-photo-share.png` |
| Route | `/memories/create` |
| Feature | `memories` |
| Flutter screen | `PhotoShareScreen` |
| Source path | `lib/features/memories/presentation/screens/photo_share_screen.dart` |
| Purpose | Create shareable memory by adding stamp overlay to a station photo. |
| API | Optional: `POST /api/v1/mobile/share-events` after user shares. |

States:

- picking_photo
- editing_overlay
- saving
- sharing
- saved
- share_failed

Rules:

- Creating a memory image must not alter collection/reward data.
- Share event tracking should be best-effort and non-blocking.

---

### 15. Tap To Collect

| Field | Value |
|---|---|
| Design file | `docs/design/visily/visily-tap-to-collect.png` |
| Route | `/scan/tap-to-collect` |
| Feature | `scan` |
| Flutter screen | `TapToCollectScreen` |
| Source path | `lib/features/scan/presentation/screens/tap_to_collect_screen.dart` |
| Purpose | NFC-first instruction screen before reading tag. |
| API | None until NFC payload is read. |

States:

- nfc_supported
- nfc_not_supported
- nfc_disabled
- waiting_for_tag
- tag_read
- read_error

---

### 16. Collect & Share Rewards

| Field | Value |
|---|---|
| Design file | `docs/design/visily/visily-collect-&-share-rewards.png` |
| Route | `/rewards/share` or `/scan/reward-unlocked` |
| Feature | `rewards` / `memories` |
| Flutter screen | `RewardUnlockedShareScreen` |
| Source path | `lib/features/rewards/presentation/screens/reward_unlocked_share_screen.dart` |
| Purpose | Milestone reward celebration and sharing. |
| API | Renders collect/reward response; optional share event tracking. |

> **Reachability gap (2026-07-17):** Routes registered (`/rewards/share`, `/scan/reward-unlocked`) but production collect cannot open this screen — no unlock payload on collect. Prefer `/scan/reward-unlocked` once backend supports it. Debug preview: Profile → API Debug (debug builds only). **Do not fake unlock in UI.** Reward Unlocked remains **BACKEND_CONTRACT_REQUIRED** (confirmed 2026-07-18 polish decisions).

States:

- reward_unlocked
- reward_pending_fulfillment
- share_ready
- share_failed

---

## 6. Feature Folder Map

```text
lib/features/
├── app_shell/
├── auth/
├── onboarding/
├── home/
├── stations/
├── scan/
├── stamp_book/
├── rewards/
├── memories/
└── profile/
```

---

## 7. API Contract Dependency Map

| Feature | Needs API now? | Backend source module | Notes |
|---|---:|---|---|
| auth | Yes | `auth`, `user` | Existing backend has auth endpoints. |
| onboarding | No | none | Local first-launch state only. |
| home | Yes | `collection`, `reward`, `metro`, `monetization` | May need composed mobile summary endpoint. |
| stations | Yes | `metro` | Lines/stations public APIs exist but may need mobile DTO. |
| scan | Yes | `metro`, `collection`, `reward`, `monetization` | Most critical contract. NFC-first. |
| stamp_book | Yes | `collection`, `metro` | Needs collected/uncollected grid data. |
| rewards | Yes | `reward` | Voucher states must be backend-driven. |
| memories | Partial | `community` | Share tracking optional for MVP. |
| profile | Yes | `user`, `community`, `collection` | User stats, memories, achievements. |

---

## 8. Backend-Driven State Policy

Flutter must not decide business outcomes. It only renders backend-confirmed states.

Forbidden local decisions:

- Marking stamp as collected.
- Unlocking reward.
- Redeeming voucher.
- Accepting GPS as valid.
- Treating NFC tag as valid.
- Counting ad impression as monetizable if collection failed.

Allowed local decisions:

- First-launch onboarding complete.
- UI tab selected.
- Temporary form validation.
- Optimistic local image preview before upload/share.

---

## 9. Minimum Mobile API Contract Required Before Integration

Ask backend to generate or confirm:

```text
1. Auth login/register/refresh/logout/me
2. Mobile home summary
3. Lines + stations list
4. Station detail
5. NFC/QR station resolve if still separate
6. Collect stamp endpoint with idempotency
7. Stamp book endpoint
8. Stamp detail endpoint
9. Rewards summary
10. Voucher detail/redeem endpoint
11. Share event tracking endpoint
12. Profile summary endpoint
```
