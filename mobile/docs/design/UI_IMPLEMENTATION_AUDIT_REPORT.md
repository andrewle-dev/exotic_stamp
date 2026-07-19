# UI Implementation Audit Report

> **Scope:** Read-only comparison of Visily design references + `UI_SCREEN_INVENTORY.md` against Flutter source under `mobile/lib/`.  
> **Date:** 2026-07-15 (decisions amended 2026-07-18)  
> **Product:** Exotic Stamp / Metro Stamp mobile module  
> **Constraints honored:** No code changes. Color HEX, mock images, exact copy, and &lt;8px spacing ignored unless they break layout intent.

### Product decisions (2026-07-18)

| Decision | Status |
|----------|--------|
| Bottom nav | **Home \| Stamp \| Scan FAB \| Stations \| Profile** — Rewards is **not** a tab |
| Rewards | Secondary screen via Home Claim Rewards, Success/Reward CTA, Profile if applicable |
| Collect (MVP) | **NFC only** visible; QR collect remains gated — do not restore via visual polish |
| Rewards not in bottom nav | **Not P0** (intentional) |
| Reward Unlocked after collect | **BACKEND_CONTRACT_REQUIRED** — do not fake unlock |
| Full locale / l10n | Later — not current polish sprint |

See also [`UI_UX_POLISH_AUDIT_REPORT.md`](./UI_UX_POLISH_AUDIT_REPORT.md).

---

## 1. Summary

| Metric | Count / note |
|--------|----------------|
| Expected screens (inventory §5 + SyncWave NFC asset) | **17** design surfaces |
| Inventory core screens (§5) | **16** |
| Screens with Flutter file | **16 / 16** inventory screens |
| Screens with registered route | **16 / 16** (Reward Unlocked has **2** dual routes) |
| Missing screen files | **0** |
| Intentionally hidden (QR collect UI) | **1 policy area** (gated; not deleted) — **MVP: keep gated** |
| Major structural mismatches | Reduced after 2026-07-18 nav decision; remaining: dead reward share route (API), QR design vs NFC product, stub CTAs, shared-card drift |

### Highest-risk issues

1. **P0 — Reward Unlocked / Collect & Share share flow not reachable from collect** — routes exist (`/rewards/share`, `/scan/reward-unlocked`); **blocked by backend gap**: `POST /collection/collect` has no `newlyIssuedReward` / `rewardUnlocked` (rewards issue async via `StampCollectedEvent`). Debug preview only: Profile → API Debug (kDebugMode). Do **not** fake unlock in production.
2. **P1 — Visily `visily-scan.png` is QR-first**; product + code are NFC-only for MVP collect with QR gated off. Treat SyncWave NFC asset as the closer live target for Scan. Do **not** restore QR collect via polish.
3. **P1 — Multiple shell/detail CTAs** — verify stubs vs wired (Stations View Map / filter, Rewards View Milestones, Station Favorite); see §6.
4. **P2 — Shared design-system drift** — `StationCard`, `VoucherCard`, `ProgressCard`, `AppPageScaffold` (as scaffold) largely unused; feature-local duplicates and local AppBars on flow screens. Sprint C: visible consistency only — no broad card refactor.
5. ~~**P0 — Rewards missing from bottom nav**~~ — **RESOLVED as product decision (2026-07-18):** Rewards is intentionally **not** a bottom-nav tab.

### Intentionally hidden

- Stamp-collection **QR UI** (`ScanCapabilities.enableQrFlow`, default `false` / `ENABLE_QR_FLOW`).
- Classification: **INTENTIONALLY_HIDDEN** for collect QR surfaces; routes for QR scanner view remain behind the flag (not a public default path). Voucher redemption “QR” graphic is separate (decorative icon).
- **MVP policy:** NFC is the only visible collect flow. Visual polish must not re-enable QR collect.
---

## 2. Screen Coverage Matrix

| Expected screen | Design reference asset | Flutter file found | Route found | Status | Severity | Notes |
|-----------------|------------------------|--------------------|-------------|--------|----------|-------|
| Welcome / Onboarding | `visily-welcome.png` | `lib/features/onboarding/presentation/screens/welcome_screen.dart` | `/welcome` | COMPLETE_ENOUGH | P2 | Hierarchy matches. Page indicator implies 3 pages but only one page; Skip = Next = complete onboarding. |
| Home | `visily-home.png` | `lib/features/home/presentation/screens/home_screen.dart` | `/home` (shell) | COMPLETE_ENOUGH | P2 | Sections present (banner, progress, recent, CTA, shortcuts, social). CTA correctly NFC (design shows QR line). Loading/error/empty handled. Recent stamps not tappable. |
| Stamp Book | `visily-stamp-book.png` | `lib/features/stamp_book/presentation/screens/stamp_book_screen.dart` | `/stamp-book` (shell) | COMPLETE_ENOUGH | P2 | Status card + line filters + grid + footer. Header search navigates to Stations (not in-book search). QR footer gated. |
| Scan (inventory / Visily QR mock) | `visily-scan.png` | `lib/features/scan/presentation/screens/scan_screen.dart` | `/scan` (shell) | INTENTIONALLY_HIDDEN *(QR)* / IMPLEMENTED_BUT_VISUALLY_OFF *(vs this PNG)* | P1 | Design is dark, QR-first with mode toggle + sponsor card. Code is NFC-first light UI; QR behind flag. Prefer SyncWave asset for current product. |
| NFC Scan (extra asset) | `visily-syncwave-nfc-scan.png` | Same `scan_screen.dart` | `/scan` | COMPLETE_ENOUGH | P2 | Closest match to live NFC scan. Missing some mock pieces (NFC Active pill, dual info cards, FAQ link, protocol footer). Not listed in inventory table §3. |
| Tap To Collect | `visily-tap-to-collect.png` | `lib/features/scan/presentation/screens/tap_to_collect_screen.dart` | `/scan/tap-to-collect` | COMPLETE_ENOUGH | P2 | Layout intent matches; primary FAB lands here. Local `_BrandMark` / AppBar (not `AppScreenHeader`). Fake multi-step indicator. |
| Location Verification | `visily-location-verification.png` | `lib/features/scan/presentation/screens/location_verification_screen.dart` | `/scan/location-verification` | COMPLETE_ENOUGH | P2 | Illustration + status cards + CTAs present. Map is abstract placeholder. Title copy differs slightly (“Xác minh” vs design “Xác nhận”). |
| Stamp Collected Success | `visily-stamp-collected-success.png` | `lib/features/scan/presentation/screens/stamp_collected_success_screen.dart` | `/scan/success` | COMPLETE_ENOUGH | P2 | Hero stamp + progress + CTAs (Book / Share / Scan next). Share is text share, not Photo Share. Local `_ProgressCard`. |
| Scan Error | `visily-scan-error.png` | `lib/features/scan/presentation/screens/scan_error_screen.dart` | `/scan/error` | IMPLEMENTED_BUT_VISUALLY_OFF | P1 | Design mock is a **catalog of 3 cases** on one canvas; app correctly shows **one phase-driven error**. Missing help/report footer block from mock. AppBar title differs. |
| Stations List | `visily-stations-list.png` | `lib/features/stations/presentation/screens/stations_list_screen.dart` (`StationsScreen` typedef) | `/stations` (shell) | COMPLETE_ENOUGH | P1 | Nearby hero + directory present. **View Map** and header **filter** are no-ops. Does not use shared `StationCard`. |
| Station Detail | `visily-station-detail.png` | `lib/features/stations/presentation/screens/station_detail_screen.dart` | `/stations/:stationId` | COMPLETE_ENOUGH | P2 | Hero, social proof, actions, history, nearby, red collect CTA. Favorite is no-op. No `AppScreenHeader`. |
| Rewards | `visily-rewards.png` | `lib/features/rewards/presentation/screens/rewards_screen.dart` | `/rewards` (secondary) | COMPLETE_ENOUGH *(nav decided)* | P1 *(wording/polish)* | **Decided 2026-07-18:** not a bottom-nav tab. Secondary via Home Claim Rewards, Success/Reward CTA, Profile if applicable. Secondary header OK. |
| Profile | `visily-profile.png` | `lib/features/profile/presentation/screens/profile_screen.dart` | `/profile` (shell) | COMPLETE_ENOUGH | P2 | Avatar/stats/invite/memories/achievements/menu + version footer. Visily bottom nav here is 5-tab (no Rewards) — matches chrome. Some menu items stubbed. |
| Stamp Detail | `visily-stamp-detail.png` | `lib/features/stamp_book/presentation/screens/stamp_detail_screen.dart` | `/stamps/:stationId` (MVP param) | MISSING_ROUTE *(inventory stampId)* / COMPLETE_ENOUGH *(UI)* | P2 | Inventory route `/stamps/:stampId`; code uses **stationId** by design (documented). Structure broadly matches. AppBar local. |
| Voucher Detail | `visily-voucher-detail.png` | `lib/features/rewards/presentation/screens/voucher_detail_screen.dart` | `/rewards/vouchers/:voucherId` | IMPLEMENTED_BUT_VISUALLY_OFF | P1 | Hero/terms/redeem present. `VoucherCodeQrSection` uses **icon placeholder**, not generated QR of code. |
| Photo Share | `visily-photo-share.png` | `lib/features/memories/presentation/screens/photo_share_screen.dart` | `/memories/create` | COMPLETE_ENOUGH | P2 | Preview, stamp selector, toggles, Share/Save present. Reachable from Stamp Detail. Manual visual pass recommended for overlay fidelity. |
| Collect & Share Rewards / Reward Unlocked | `visily-collect-&-share-rewards.png` | `lib/features/rewards/presentation/screens/reward_unlocked_share_screen.dart` | `/rewards/share`, `/scan/reward-unlocked` | **BACKEND_CONTRACT_REQUIRED** *(unreachable from success)* | P0 | Screen + dual routes registered. Collect API has **no** reward unlock field (`StampCollectResponse`: stamp + progress + `isNew` only). Rewards issue **async** after collect. Integration TODOs on success screen + entity; debug preview via API Debug. |
| Login / Auth (out of Visily inventory) | — | `login_screen.dart`, etc. | `/login`, … | UNKNOWN_NEEDS_MANUAL_CHECK | — | Not in Visily PNG set; out of primary audit surface. |

**Statuses used:** `COMPLETE_ENOUGH` · `IMPLEMENTED_BUT_VISUALLY_OFF` · `PARTIAL` · `MISSING_ROUTE` · `MISSING_SCREEN` · `INTENTIONALLY_HIDDEN` · `UNKNOWN_NEEDS_MANUAL_CHECK`

---

## 3. Major Visual Mismatches

### 3.1 Bottom navigation — Rewards not a tab (**decided**)

| | |
|--|--|
| **Screen** | App shell / all tab screens |
| **Product decision (2026-07-18)** | Rewards is **not** a bottom navigation tab. Canonical chrome: **Home \| Stamp \| Scan FAB \| Stations \| Profile**. |
| **Implementation** | `BottomNavBar`: Home · Stamp · *(FAB gap)* · Stations · Profile. Matches decision. |
| **Rewards entry** | Secondary screen: Home “Claim Rewards”, Success/Reward CTA when available, Profile if applicable. `AppScreenHeader.secondary` is correct. |
| **Visily note** | Some Visily frames show Rewards selected / 6 tabs; Home/Profile/Stations frames often show 5 tabs without Rewards. Prefer chrome that matches this decision over Rewards-selected mock. |
| **Severity** | **Resolved — not P0.** Do not add Rewards tab in polish sprints. |

### 3.2 Scan — Visily QR-first vs NFC-first product

| | |
|--|--|
| **Screen** | Scan |
| **Design expects** | `visily-scan.png`: dark UI, circular QR viewport, QR CODE / NFC TAG toggle with **QR active**, sponsored card, flash controls. |
| **Implementation** | Light NFC pulse UI (`NfcPulseCircle`), AppBar “Quét NFC”, QR only if `ENABLE_QR_FLOW=true`. Aligns better with `visily-syncwave-nfc-scan.png`. |
| **Why it matters** | Pixel-matching `visily-scan.png` would violate NFC-first policy in inventory §2. |
| **Suggested fix direction** | Treat SyncWave PNG as canonical for live Scan; mark `visily-scan.png` as legacy/fallback reference; optionally document gated QR layout separately. |
| **Severity** | **P1** (product vs mock conflict — do not “fix” toward QR-first without product change) |

### 3.3 Scan Error — catalog mock vs single-state screen

| | |
|--|--|
| **Screen** | Scan Error |
| **Design expects** | Scrollable list of TRƯỜNG HỢP 01–03 cards + help/report footer. |
| **Implementation** | One full-screen error from `ScanErrorPresentation.forPhase` + primary/secondary actions. |
| **Why it matters** | Interaction model is correct for production; visual card-page layout differs. Help/report footer from mock absent. |
| **Suggested fix direction** | Keep single-state UX; restyle card/CTA/footer to match one case from Visily; add help strip if product wants it. |
| **Severity** | **P1** |

### 3.4 Voucher Detail — QR presentation

| | |
|--|--|
| **Screen** | Voucher Detail |
| **Design expects** | Real QR for counter scan + promo code ticket block. |
| **Implementation** | Code + copy; `Icons.qr_code_2_rounded` placeholder (not a barcode widget). |
| **Why it matters** | Redemption backup path looks incomplete in-store. (This is voucher QR, not stamp-collect QR — not covered by `enableQrFlow`.) |
| **Suggested fix direction** | Render QR from redeem code when available; keep gather independent of collect QR flag. |
| **Severity** | **P1** |

### 3.5 Collect & Share / Reward Unlocked

| | |
|--|--|
| **Screen** | Reward Unlocked Share |
| **Design expects** | Celebration / share milestone after unlock (`visily-collect-&-share-rewards.png`). Inventory also expects `newlyIssuedReward` on collect success. |
| **Implementation** | Screen exists with celebration + share + view reward CTAs. Routes registered. **Not wired from success** because collect response cannot signal unlock. |
| **Support classification (2026-07-17)** | **NOT_SUPPORTED** — see §3.5.1 |
| **Why it matters** | Milestone celebration/share missing from MVP collect→reward loop. |
| **Suggested fix direction** | Backend must add sync `newlyIssuedReward` (or equivalent) on collect **or** mobile must poll `GET /rewards/my` after success with clear pending UX. Prefer preferred route `/scan/reward-unlocked` + `RewardUnlockedSharePayload` via `extra`. |
| **Severity** | **P0** (blocked on API) |

#### 3.5.1 Collect → reward unlock — integration gap

| Field | Status |
|-------|--------|
| Classification | **NOT_SUPPORTED** |
| Backend DTO | `StampCollectResponse` (`stampId`, `stationId`, `stationName`, `lineId`, `campaignId`, `stampDesignUrl`, `collectedAt`, `isNew`, `collectMethod`, `progress`) |
| Missing for unlock UX | `newlyIssuedReward` / `rewardUnlocked` / milestone + voucher payload on collect |
| Reward issuance today | Async after commit: `StampCollectedEvent` → `RewardEvaluationService` (not in HTTP collect body) |
| Flutter model today | `CollectStampResult`: stamp, progress, `isNew`, `nextRewardHint`, `sponsorAd` — no unlock object |
| Contract doc | `docs/api/MOBILE_API_CONTRACT.md` § collect: `rewardUnlocked` **MISSING** |
| Mobile interim | TODO on `StampCollectedSuccessScreen` + `CollectStampResult`; **debug-only** preview: Profile → API Debug → “Preview Reward Unlocked” |
| Must not do | Infer unlock from local stamp count; fake voucher ownership; show unlock on duplicate (`isNew: false`) |
### 3.6 Stations — missing interactive pattern from mock

| | |
|--|--|
| **Screen** | Stations List |
| **Design expects** | Filter action, View Map, sortable directory. |
| **Implementation** | Search + line chips + nearby hero + rows; View Map `onPressed: () {}`; filter action unused; “Sorted by distance” non-interactive. |
| **Why it matters** | Apparent secondary actions feel broken. |
| **Suggested fix direction** | Wire or hide stubs until map/filter exist. |
| **Severity** | **P1** |

---

## 4. Missing / Partial Screens

| Item | Status | Detail |
|------|--------|--------|
| Missing screen **files** | None | All 16 inventory screens have a Dart screen class. |
| Rewards (nav discoverability) | **COMPLETE_ENOUGH *(policy)*** | Secondary screen by design (2026-07-18); not a bottom-nav tab |
| Reward Unlocked / Collect & Share | **BACKEND_CONTRACT_REQUIRED** | Routes registered; collect response has no unlock signal; debug preview only. |
| Stamp Detail route param | Inventory mismatch | Inventory: `/stamps/:stampId`. Code: `/stamps/:stationId` (documented MVP). |
| Multi-page Welcome / Tap onboarding | **PARTIAL** | Visual page dots without real pages. |
| SyncWave NFC asset | Not in inventory §3 table | Present on disk; useful as NFC Scan reference. |
| QR Scan UI (collect) | **INTENTIONALLY_HIDDEN** | Code retained behind `ScanCapabilities.enableQrFlow`. |

---

## 5. Shared Component Consistency

### Headers

| Pattern | Where |
|---------|--------|
| `AppScreenHeader` (brand/title) | Home, Stamp Book, Stations, Rewards, Profile |
| Local Material `AppBar` / custom chrome | Welcome, Tap To Collect, Scan, Location Verification, Scan Error, Station Detail, Stamp Detail, Voucher Detail, Photo Share, Reward Unlocked, Success (no AppBar) |
| Duplicated `_BrandMark` | Welcome, Tap To Collect (not `AppLogo`) |
| Deprecated | `home_top_bar.dart` still in tree |

### Footers / version

| Pattern | Where |
|---------|--------|
| `AppVersionFooter` | Home, Stamp Book (via footer), Stations, Rewards, Profile |
| No version footer | Scan flow screens, detail/share overlays (acceptable; design often omits) |
| Hardcoded “Metro Stamp Collector v…” | Not found as screen hardcode; design shows it — app uses package_info via footer |

### Bottom nav

- Notched bar + red Scan FAB matches Visily center-scan idea.
- **Canonical (2026-07-18):** Home · Stamp · Scan FAB · Stations · Profile. Rewards is **not** a tab (secondary screen).
- FAB → `/scan/tap-to-collect` (not `/scan` directly) — extra instructional step; aligns with Tap To Collect asset.
- MVP collect: **NFC only**; QR collect remains gated.

### Cards / buttons / tiles

| Shared widget | Usage |
|---------------|--------|
| `StampTile` | Stamp Book — **used** |
| `StationCard` | **No feature call sites** — stations use `NearbyStationHeroCard` / `StationDirectoryRow` |
| `VoucherCard` | **Unused** — Rewards uses `RewardVoucherCard` |
| `ProgressCard` | **Unused** — duplicated as `HomeCollectionCard` / `RewardsProgressCard` / success `_ProgressCard` / `StampCollectionProgressCard` |
| `AppButton` / `PrimaryButton` / scan-local buttons | Mixed; scan flow prefers `ScanPrimaryButton` / `ScanOutlineButton` |
| `AppPageScaffold` | Only `shellBottomInset` padding constant — not used as page wrapper |

### Stamp / station duplication

- Home recent stamps: `RecentStampCard` (no navigation) vs Stamp Book `StampTile`.
- Station list/detail cards are feature-local, parallel to unused shared `StationCard`.

### Error / empty / loading

| Area | Loading | Empty | Error |
|------|---------|-------|-------|
| Home | Yes | Recent stamps empty | Yes (+ partial banner) |
| Stamp Book | Yes | Yes | Yes |
| Stations | Yes | Search/line empty | Yes (+ GPS banner) |
| Rewards | Yes | noRewardsYet / empty vouchers | Yes |
| Profile | Yes | — | unauthorized / error |
| Scan flow | Mid-phase loading | N/A | Dedicated error/success routes |
| Station / Stamp / Voucher detail | Yes | notFound variants | Yes |

---

## 6. Routing Gaps

### Screens existing but weakly reachable

| Screen | Gap |
|--------|-----|
| Rewards | Secondary (not a tab); Home “Claim Rewards”, Success/Reward CTA, Profile if applicable |
| Reward Unlocked Share | Dual routes; production wire blocked until collect returns unlock; debug preview via API Debug |
| Scan (`/scan`) | Reachable via Tap To Collect “Tiếp theo”; FAB skips straight entry to Tap To Collect |

### Routes missing / mismatched vs inventory

| Inventory | Implementation | Notes |
|-----------|----------------|-------|
| `/stamps/:stampId` | `/stamps/:stationId` | Documented MVP gap |
| `/rewards/share` **or** `/scan/reward-unlocked` | Both registered | Orphaned until wired |
| Auth routes | Present | Outside Visily PNG inventory |

### CTAs not wired (stubs)

| Location | Control |
|----------|---------|
| Stations list | ~~“View Map”~~ / ~~Filter~~ — wired (funnel + external map) |
| Stations header | Funnel opens filter/sort sheet |
| Rewards | ~~“View Milestones”~~ — opens milestones bottom sheet |
| Station Detail | ~~Favorite~~ — **hidden** (no favorite-station API) |
| Voucher Detail | Redeem mutation **disabled** (410); UI shows **Redeem code** only |
| Rewards voucher card | Favorite star hidden unless callback provided |

### Unused shared widgets (P2 — keep for now)

| Widget | Note |
|--------|------|
| `StationCard` | Unused — feature uses local directory/hero cards |
| `VoucherCard` | Unused — Rewards uses `RewardVoucherCard` |
| `ProgressCard` | Unused — duplicated as feature progress cards |

Do **not** delete yet; consolidate in a dedicated shared-card cleanup pass.
| Rewards | “View Milestones” |
| Station detail | Favorite |
| Profile menu | At least one `onTap: () {}` stub |

### Dead / legacy routes

| Item | Note |
|------|------|
| `/auth` | Redirects to `/login` |
| QR fallback view | Alive in code, inactive unless feature flag |
| Deprecated `home_top_bar` / `AppBottomNavBar` typedef | Cleanup candidates (not deleted in this audit) |

---

## 7. QR / NFC Scan State

| Topic | Finding |
|-------|---------|
| NFC screens present | Tap To Collect, Scan (NFC view), Location Verification, Success, Error — wired in flow |
| QR stamp collect UI | **Hidden by default** — `ScanCapabilities.enableQrFlow` (`lib/core/config/scan_capabilities.dart`), env `ENABLE_QR_FLOW` |
| QR classification | **INTENTIONALLY_HIDDEN** for collect UI |
| Legacy QR code retained? | Yes — `_QrFallbackView`, `MobileScannerController`, cubit `onQrPayloadRead` / phases |
| Old QR UI still visible (default build)? | **No** on Scan/Home CTA/Stamp Book footer. Home CTA uses NFC copy/icon. |
| Visily still shows QR | Yes — Home CTA subtitle, Stamp Book footer, `visily-scan.png`, Scan Error case 01 |
| DEAD_CODE_PRESENT? | Collect QR not exposed via default routes; still compilable behind flag → not ACTIVE_LEGACY in UI; flag path is intentional retention |
| Voucher QR | Unrelated decorative/icon QR on voucher detail — still visible; does **not** use `enableQrFlow` |
| SyncWave asset | Extra NFC Scan reference on disk; should be added to inventory §3 if adopted as canonical |

**Recommendation note:** Keep QR implementation gated for MVP; do not delete; do **not** restore via visual polish. NFC is the only visible collect flow.

---

## 8. Prioritized Fix Plan

### P0 — must address before publish-quality MVP (non-nav)

1. **Wire Reward Unlocked Share** — **blocked**: add `newlyIssuedReward` (or poll strategy) on backend first; then success CTA → `/scan/reward-unlocked` with payload. Do not fake in production.
2. **Audit collect→success→reward** path — confirm once API exposes unlock; until then only debug preview is reachable.
3. **User-facing technical leakage** — see polish Sprint A in [`UI_UX_POLISH_AUDIT_REPORT.md`](./UI_UX_POLISH_AUDIT_REPORT.md) (`backendCode`, server/backend copy). Not a nav task.

~~Decide + implement Rewards in chrome~~ — **Done as decision:** Rewards is secondary, not a tab.

### P1 — visual / layout alignment

1. Canonicalize Scan against **SyncWave NFC** (+ document `visily-scan.png` as legacy; do not restore QR collect).
2. Restyle Scan Error single-state screen to Visily case card language; add optional help footer.
3. Optional later: voucher **redeem-code** QR graphic (not stamp-collect QR).
4. Verify Stations / Rewards CTAs are wired or hidden (View Map, filter, View Milestones).
5. Ensure Rewards secondary entry points remain clear (Home Claim Rewards, etc.) — do **not** add bottom-nav tab.

### P2 — shared component cleanup (visible consistency only in polish Sprint C)

1. Do **not** broad-adopt unused `StationCard` / `VoucherCard` / `ProgressCard` in polish Sprint C; consolidate only where users see inconsistency.
2. Standardize overlay headers (scan/detail) vs `AppScreenHeader` / back patterns where drift is visible.
3. Replace duplicate `_BrandMark` with `AppLogo` where appropriate.
4. Make Home recent stamps open Stamp Detail when collected.
5. Resolve Welcome / Tap page indicators (real carousel **or** remove dots).
6. Full EN/VI unification — **Later** (not current polish sprint).

### P3 — polish

1. Stamp Book search icon semantics (search vs jump to Stations).
2. Success Share → optional Photo Share handoff.
3. Location Verification title copy / map illustration fidelity.
4. Inventory update: SyncWave asset path; stamp detail param notes.
5. Remove deprecated `home_top_bar` when safe.

---

## 9. Files to Review Manually

### Design references

- `mobile/docs/design/UI_SCREEN_INVENTORY.md`
- `mobile/docs/design/visily/*.png` (all listed assets)
- `mobile/docs/design/visily/visily-syncwave-nfc-scan.png` *(extra vs inventory table)*
- `mobile/docs/design/visily/exotic-stamp-multiscreens.pdf`

### Shell / routing

- `lib/features/app_shell/presentation/screens/main_shell_screen.dart`
- `lib/features/app_shell/presentation/widgets/bottom_nav_bar.dart`
- `lib/app/router/app_router.dart`
- `lib/app/router/route_names.dart`
- `lib/core/config/scan_capabilities.dart`

### Feature screens (primary)

- `lib/features/onboarding/presentation/screens/welcome_screen.dart`
- `lib/features/home/presentation/screens/home_screen.dart`
- `lib/features/home/presentation/widgets/*` (esp. `home_collect_cta.dart`, `home_shortcut_grid.dart`, `home_recent_stamps_section.dart`)
- `lib/features/stamp_book/presentation/screens/stamp_book_screen.dart`
- `lib/features/stamp_book/presentation/screens/stamp_detail_screen.dart`
- `lib/features/scan/presentation/screens/scan_screen.dart`
- `lib/features/scan/presentation/screens/tap_to_collect_screen.dart`
- `lib/features/scan/presentation/screens/location_verification_screen.dart`
- `lib/features/scan/presentation/screens/stamp_collected_success_screen.dart`
- `lib/features/scan/presentation/screens/scan_error_screen.dart`
- `lib/features/scan/presentation/utils/scan_error_presentation.dart`
- `lib/features/stations/presentation/screens/stations_list_screen.dart`
- `lib/features/stations/presentation/screens/station_detail_screen.dart`
- `lib/features/rewards/presentation/screens/rewards_screen.dart`
- `lib/features/rewards/presentation/screens/voucher_detail_screen.dart`
- `lib/features/rewards/presentation/screens/reward_unlocked_share_screen.dart`
- `lib/features/rewards/presentation/widgets/voucher_detail_sections.dart`
- `lib/features/memories/presentation/screens/photo_share_screen.dart`
- `lib/features/profile/presentation/screens/profile_screen.dart`

### Shared design system

- `lib/shared/widgets/app_screen_header.dart`
- `lib/shared/widgets/app_version_footer.dart`
- `lib/shared/widgets/app_page_scaffold.dart`
- `lib/shared/widgets/stamp_tile.dart`
- `lib/shared/widgets/station_card.dart` *(unused)*
- `lib/shared/widgets/voucher_card.dart` *(unused)*
- `lib/shared/widgets/progress_card.dart` *(unused)*
- `lib/shared/widgets/app_button.dart` / `app_action_buttons.dart`

### Manual UI checklist (device/emulator)

1. Bottom nav: highlight states for all shell branches including `/rewards`.
2. FAB → Tap To Collect → Scan → GPS → Success / Error happy paths.
3. Confirm QR UI never appears with default build flags.
4. Claim voucher → detail → redeem affordances (and QR readability).
5. Milestone unlock: does Reward Unlocked ever appear?
6. Side-by-side: Home / Stamp Book / Stations / Profile vs matching Visily PNGs.

---

## Appendix A — Expected checklist (built for this audit)

From `UI_SCREEN_INVENTORY.md` §5 + Visily folder:

1. Welcome / onboarding  
2. Home  
3. Stamp Book  
4. Tap To Collect / NFC Scan entry  
5. Scan (NFC primary; QR if design — currently gated)  
6. SyncWave NFC Scan *(asset present; maps to Scan)*  
7. Location Verification  
8. Stamp Collected Success  
9. Scan Error  
10. Stations List  
11. Station Detail  
12. Rewards  
13. Profile  
14. Stamp Detail  
15. Voucher Detail  
16. Photo Share  
17. Collect & Share Rewards / Reward Unlocked Share  

## Appendix B — Inventory ↔ router quick map

| Inventory route | Router constant / path | Wired? |
|-----------------|------------------------|--------|
| `/welcome` | `RouteNames.welcome` | Yes |
| `/home` | shell | Yes |
| `/stamp-book` | shell | Yes |
| `/scan` | shell + nested | Yes |
| `/scan/tap-to-collect` | nested | Yes (FAB primary) |
| `/scan/location-verification` | nested | Yes (flow listener) |
| `/scan/success` | nested | Yes |
| `/scan/error` | nested | Yes |
| `/stations` | shell | Yes |
| `/stations/:stationId` | root overlay | Yes |
| `/rewards` | shell branch | Yes (weak chrome) |
| `/profile` | shell | Yes |
| `/stamps/:stampId` | `/stamps/:stationId` | Param name mismatch |
| `/rewards/vouchers/:voucherId` | yes | Yes |
| `/memories/create` | yes | Yes (from stamp detail) |
| `/rewards/share` / `/scan/reward-unlocked` | both | **Registered only** |

---

*End of audit report. No Flutter source files were modified; only this markdown report was added.*
