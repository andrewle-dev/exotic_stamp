# UI/UX Polish Audit Report

> **Scope:** Read-only premium UX / wording / gradient / spacing / consistency audit of Flutter mobile UI under `mobile/lib/`.  
> **Date:** 2026-07-18 (decisions amended 2026-07-18)  
> **Product:** Exotic Stamp / Metro Stamp mobile module  
> **Constraints honored:** No Flutter or backend code changes. No auto-fixes. Report only.  
> **Related:** Structural coverage vs Visily inventory is in [`UI_IMPLEMENTATION_AUDIT_REPORT.md`](./UI_IMPLEMENTATION_AUDIT_REPORT.md). This report focuses on **user-facing polish**.

### Final product decisions (locked)

| Decision | Status |
|----------|--------|
| Bottom nav | **Home \| Stamp \| Scan FAB \| Stations \| Profile** — no Rewards tab |
| Rewards | **Secondary screen** — entry from Home “Claim Rewards”, Success/Reward CTA when available, and Profile if applicable. `AppScreenHeader.secondary` is correct. |
| Collect flow (MVP) | **NFC only** visible. QR collect stays gated/`INTENTIONALLY_HIDDEN`. Visual polish must **not** restore QR collect UI. |
| Rewards not in bottom nav | **Not P0** — intentional product choice |
| Reward Unlocked after collect | **BACKEND_CONTRACT_REQUIRED** — do not fake unlock in UI |
| Full locale / l10n | **Later** — not current polish sprint |
| Sprint A | Remove technical/dev-facing wording + hide backend/debug leakage |
| Sprint B | Targeted gradients only (high-value cards / CTA / scan / success) |
| Sprint C | Visible component consistency only — no broad card refactor |

### Research notes

| Source | Status |
|--------|--------|
| [`README.md`](./README.md), [`UI_SCREEN_INVENTORY.md`](./UI_SCREEN_INVENTORY.md) | Used |
| [`UI_IMPLEMENTATION_AUDIT_REPORT.md`](./UI_IMPLEMENTATION_AUDIT_REPORT.md) | Used for nav/reachability context |
| Flutter source under `lib/` | Inspected |
| Visily PNGs / PDF under `docs/design/visily/` | **Not present in workspace** at audit time (only markdown under `docs/design/`). Pixel-match vs Visily = **NEEDS_MANUAL_DECISION** until assets are restored |
| Locale strategy (EN vs VI primary) | **Later / not polish sprint** — app mixes both; Sprint A may soften only *technical* mixed strings, not full unification |

Brand tokens used for gradient recommendations:

| Token | HEX |
|-------|-----|
| `primaryBlue` | `#01599D` |
| `accentRed` | `#E83B28` |
| `backgroundWhite` | `#FFFFFF` |
| `blueTint` / `surface` / muted grays | `#EAF3FB` / `#F8FAFC` / `#667085` |

---

## 1. Executive Summary

### Overall production readiness

The app is **usable as an MVP shell** (tabs, scan flow, stamp book, rewards, profile) but is **not yet premium / publish-ready**. Several screens still read as form/admin or API-integration scaffolds: technical “server/backend” copy, English chrome next to Vietnamese body text, flat progress cards, and a few empty/celebration states that feel unfinished.

### Top 5 roughest screens

1. **Personal Information** — `EDITABLE DETAILS` / `READ-ONLY` / “backend update is not supported yet”
2. **Rewards** — “server-issued” / “from the server” empty & pending copy (nav placement is intentional secondary)
3. **Location Verification** — “máy chủ” / “xác minh bởi backend”; stub map graphic
4. **Stations List** — English section headers vs Vietnamese empty states; plain directory rows
5. **Reward Unlocked Share** — bare empty state; celebration under-polished; production collect path still blocked on API (do not fake unlock)

### Top 5 polish opportunities

1. Soft brand gradients on **Home** and **Rewards** progress cards (Sprint B)
2. Cool blue atmosphere behind **NFC Scan** pulse (without noisy overlays)
3. Celebration wash on **Stamp Collected Success** and **Reward Unlocked Share**
4. Friendlier empty-state decoration (Rewards / Stamp Book) — subtle only
5. CTA polish: Home collect CTA subtle red gradient; Station Detail CTA tone (wording in A; gradient in B)

### Top 5 wording issues (Sprint A)

1. `EDITABLE DETAILS` / `READ-ONLY` (Personal Information)
2. `server-issued rewards` / `reward data from the server` (Rewards)
3. `xác minh bởi backend` / `Phạm vi ga (máy chủ)` (Location Verification)
4. Raw `failure.backendCode` shown in `AppErrorState`
5. Help Center / Profile stats / auth messages mentioning “backend” / “server”

### Recommended implementation order

1. **Sprint A** — remove technical/dev-facing wording + hide backend/debug leakage  
2. **Sprint B** — targeted gradients only (progress cards, collect CTA, scan backdrop, success/celebration)  
3. **Sprint C** — visible header/button/empty consistency (no broad card refactor)  
4. **Later** — full l10n, Visily pixel pass, reward-unlock API wiring, QR collect (stays gated)  

---

## 2. Screen-by-screen Audit Matrix

| Screen | Current UX quality | Main issues | Gradient opportunity | Wording cleanup needed | Priority | Suggested action |
|--------|-------------------|-------------|----------------------|------------------------|----------|------------------|
| Home | Acceptable | EN/VI mix (`STAMP NOW!` + VI sub); flat progress card; English shortcuts | Yes (progress + CTA) | Yes (technical only in Sprint A) | P1 wording / P2 gradient | Keep NFC-only CTA; soft progress/CTA gradients in Sprint B; full locale later |
| Stamp Book | Acceptable | EN title + “Collection Status” vs VI empty/footer | Yes (empty wash only) | Mild | P2 / Later locale | Soft empty decoration in B; full label unify later |
| Stations List | Rough | EN section headers vs VI empty; plain rows | No | Mild (Later locale) | P2 | Avoid gradients on rows; locale unify later |
| Station Detail | Acceptable | ALL-CAPS EN CTA; EN actions; hero scrim already present | No (hero scrim exists) | Yes (CTA tone) | P1 | Friendlier CTA label in Sprint A; locale later |
| Rewards | Rough | Technical empty/pending copy; secondary header **intentional** (not a tab) | Yes (progress card) | Yes | P0 wording / P2 gradient | Rewrite empty/pending (A); keep secondary header; soft progress gradient (B) |
| Profile | Acceptable | Stats empty mentions backend; API Debug in debug builds | No | Yes | P1 | Soften stats empty; keep API Debug debug-only |
| Personal Information | Placeholder-like | Caps form taxonomy; backend phone copy | No | Yes | P0 | Replace section labels + helper text for humans |
| Privacy & Security | Acceptable / Rough | `ACCOUNT SECURITY` / `SESSIONS` caps | No | Yes | P2 | Softer section titles |
| Help Center | Rough | Static FAQ; “server” / “backend” in body | No | Yes | P1 | Rewrite FAQ in user language |
| Tap To Collect | Acceptable | Local brand mark; fake page-dot indicator | No | No (copy mostly OK) | P2 | Real steps or remove fake indicator; shared logo (Sprint C) |
| NFC Scan | Acceptable | Pulse OK; “iOS test” technical; QR fallback copy exists but QR collect gated | Yes (backdrop) | Yes | P1 | Soften “iOS test”; hide QR collect CTAs (keep gated); scan backdrop in B |
| NFC unsupported / error states | Acceptable | Same screen states; must not surface QR as primary MVP collect | Yes (shared with Scan) | Yes | P1 | Friendlier unsupported copy; hide “iOS test”; do not restore QR collect |
| Location Verification | Rough | Backend/máy chủ copy; stub map | No | Yes | P0 | Rewrite GPS explanation (A); map placeholder later |
| Stamp Collected Success | Acceptable | Solid VI; flat card; no celebration atmosphere; unlock CTA not wired | Yes | No (minor) | P2 | Soft celebration gradient (B); do not fake reward unlock |
| Scan Error | Acceptable | Mostly VI; generic icon; close vs back inconsistency | No | Mild | P2 | Align chrome (C); optional help strip |
| Stamp Detail | Good / Acceptable | Hero gradient exists; minor EN “stamps” in progress | Already has | Mild | Later | Locale tweak later |
| Voucher Detail | Acceptable | Blue hero gradient exists; “máy chủ” pending; redeem-code QR icon placeholder (not collect QR) | Already has (hero) | Yes | P1 | Soften pending copy (A); optional redeem-code QR polish later |
| Photo Share | Acceptable | Overlay `Verified via NFC` EN on VI screen | No | Mild | Later / P2 | Match overlay language later |
| Reward Unlocked Share | Rough | Bare empty `Center(Text)`; under-polished celebration; production path API-blocked | Yes | Mild | P2 empty polish / API later | Polish celebration + empty (B/C); do **not** fake unlock from collect |

**Shell note (decided):** Bottom nav is **Home \| Stamp \| Scan FAB \| Stations \| Profile**. Rewards is a **secondary** destination (Home Claim Rewards, Success/Reward CTA when available, Profile if applicable). Secondary header is correct. Missing Rewards tab is **not P0**.

---

## 3. User-facing Wording Issues

Suggested replacements follow each screen’s **dominant** language for Sprint A technical cleanup. Full EN vs VI product unification is **Later** (not current polish sprint).

| Screen | Current text | Problem | Suggested replacement | Priority |
|--------|--------------|---------|----------------------|----------|
| Personal Information | `EDITABLE DETAILS` | Screaming caps; form/admin taxonomy | EN: `Your details` · VI: `Thông tin của bạn` | P0 |
| Personal Information | `READ-ONLY` | Backend/form jargon | EN: `Account details` · VI: `Thông tin tài khoản` | P0 |
| Personal Information | `Phone number is read-only — backend update is not supported yet.` | Exposes backend limitation | EN: `Phone number can’t be changed in the app. Contact support if you need an update.` · VI: `Số điện thoại không thể đổi trong ứng dụng. Liên hệ hỗ trợ nếu cần cập nhật.` | P0 |
| Personal Information | `Email cannot be changed from the app.` | Acceptable but blunt | Soften slightly: `Email is managed with your account and can’t be edited here.` | P2 |
| Rewards | `Keep collecting stamps to unlock server-issued rewards.` | “server-issued” is internal | EN: `Keep collecting stamps to unlock rewards.` · VI: `Tiếp tục thu stamp để mở khóa phần thưởng.` | P0 |
| Rewards | `Pull down to refresh reward data from the server.` | Technical | EN: `Pull down to refresh your rewards.` · VI: `Kéo xuống để làm mới phần thưởng.` | P0 |
| Rewards | `A reward is pending fulfillment from the server. Pull to refresh.` | Internal fulfillment language | EN: `Your reward is almost ready. Pull to refresh.` · VI: `Phần thưởng sắp sẵn sàng. Kéo xuống để làm mới.` | P0 |
| Location Verification | `Phạm vi ga (máy chủ)` | Infrastructure tone | `Phạm vi ga` or `Khu vực thu stamp` | P0 |
| Location Verification | `… xác minh bởi backend` | English “backend” in VI UI | `… được hệ thống xác minh` or `… xác nhận khi thu stamp` | P0 |
| Location Verification | `Máy chủ sẽ quyết định phạm vi hợp lệ — không lưu kết quả cục bộ.` | Over-explains architecture | `Ứng dụng dùng GPS để kiểm tra bạn đang ở gần ga trước khi thu stamp.` | P0 |
| Help Center | `GPS is checked by the server before a stamp is collected.` | Server jargon | `We check your location near the station before collecting a stamp.` / VI equivalent | P1 |
| Help Center | `Milestones and vouchers unlock from backend progress.` | Backend jargon | `Milestones and vouchers unlock as you collect stamps.` | P1 |
| Profile (stats empty) | `Stats will appear when the backend provides profile data.` | Developer empty state | EN: `Your stamp stats will show up here once you’ve started collecting.` · VI: `Thống kê stamp sẽ hiện ở đây khi bạn bắt đầu thu thập.` | P1 |
| Auth errors | `Không thể kết nối máy chủ. Kiểm tra backend và mạng.` | “backend” for end users | `Không thể kết nối. Kiểm tra mạng và thử lại.` | P1 |
| Shared `AppErrorState` | Raw `failure.backendCode` under message | Exposes API codes to users | Hide in production; show only in debug, or map to friendly text | P0 |
| NFC Scan | `NFC tạm tắt (iOS test)` | Internal test label | `NFC tạm không khả dụng trên thiết bị này` (or hide state from release) | P1 |
| NFC Scan | `Dùng QR fallback` / `QR fallback` | Dev jargon; QR collect is gated for MVP | Hide QR collect CTAs/copy in default builds; do **not** restore QR collect via polish | P1 |
| NFC Scan | `Bật NFC… hoặc dùng QR fallback.` | Suggests QR collect path | NFC-only help: e.g. `Bật NFC trong Cài đặt để tiếp tục.` (no QR collect prompt) | P1 |
| Scan Error presentation | `… hoặc dùng QR fallback.` | Same | NFC-only retry copy; omit QR collect suggestion while gated | P1 |
| Home | `STAMP NOW!` + VI subtitle | EN/VI clash; shouty | Soften shouty tone if easy; full locale unify **Later** | Later / P2 |
| Home shortcuts | `Nearby Stations`, `Claim Rewards`, … | EN on mixed app | Keep Claim Rewards as Rewards entry; full locale **Later** | Later |
| Stations List | `Nearby Stations`, `View Map`, `Station Directory` | EN vs VI empty states | Full locale **Later** | Later |
| Station Detail | `COLLECT STATION STAMP` | ALL-CAPS EN; producty | Soften to friendlier NFC collect CTA (dominant language of screen) | P1 |
| Station Detail | `Directions`, `Virtual Tour` | EN actions next to VI errors | Full locale **Later** | Later |
| Stamp Book | `Collection Status` / title `Stamp Book` | EN chrome vs VI body | Full locale **Later** | Later |
| Privacy & Security | `ACCOUNT SECURITY`, `SESSIONS` | Caps admin tone | `Account security`, `Devices & sessions` (or VI equivalents) | P2 |
| Voucher Detail | `Phần thưởng đang chờ mã voucher từ máy chủ…` | Soft infrastructure tone | `Mã voucher đang được chuẩn bị. Vui lòng quay lại sau.` | P1 |
| Photo Share | `Verified via NFC` | EN badge on VI screen | `Đã xác thực NFC` (or EN if share asset is EN-only) | P2 |
| Photo Share | `Ghi nhận sự kiện không thành công.` | Slightly technical | `Không ghi nhận được lượt chia sẻ. Bạn vẫn có thể chia sẻ ảnh.` | P3 |
| Profile menu | `API Debug` | Dev tool (debug-gated) | Keep gated; never ship label in release builds | P0 (release gate) |
| Profile menu | `NFC Tag Writer` | Admin/internal tool | Keep admin-gated; consider “Admin tools” grouping | P2 |
| Scan flow (cubit) | `… Có thể backend vẫn đang xử lý — …` | Backend in user-facing message | `… Hệ thống có thể vẫn đang xử lý — thử lại sau hoặc mở Sổ stamp.` | P1 |

---

## 4. Gradient Opportunities

Do **not** apply gradients to form fields, settings rows, basic list rows, logout, or every card.

| Screen/component | Suggested gradient | Brand colors/tokens | Expected benefit | Risk level | Priority |
|------------------|--------------------|---------------------|------------------|------------|----------|
| Home — collection progress card | Top→bottom or soft diagonal | `primaryBlue` → `blueTint` / white text on blue variant | Progress feels like a hero, not a flat box | Low | P2 |
| Home — collect CTA (`HomeCollectCta`) | Subtle left→right or top→bottom | `accentRed` → slightly deeper red (derive from `#E83B28`, not neon) | CTA feels premium without new layout | Low–Medium | P2 |
| Rewards — progress card | Soft vertical | `blueTint` → `blueSurface` with `primaryBlue` accents | Matches Home progress language; less “filled container” | Low | P2 |
| NFC Scan — backdrop behind `NfcPulseCircle` | Soft radial or vertical wash | `blueSurface` / `blueTint` → `backgroundWhite` | Scan feels intentional atmosphere, not blank scaffold | Medium (must keep text contrast) | P2 |
| Stamp Collected Success — celebration area | Soft vertical wash behind stamp card | `blueTint` → `backgroundWhite` | Success feels celebratory without redesign | Low–Medium | P2 |
| Reward Unlocked Share — celebration header | Soft wash + existing circle accent | `primaryBlue` tint → white | Milestone moment feels special | Low–Medium | P2 |
| Rewards / Stamp Book — empty state background | Very subtle decorative wash | `surface` → white | Empty states less stark | Low | P3 |
| Voucher Detail — hero | Already has blue `LinearGradient` | Keep `primaryBlue` family | Already premium enough | — | — (maintain) |
| Stamp Detail — hero | Already has gradient | Keep | Already premium enough | — | — (maintain) |
| Station Detail — hero | Dark scrim gradient exists | Keep photo + scrim | Do not stack extra card gradients | — | — (maintain) |
| Home banner / social proof | Already use `LinearGradient` | Keep brand-aligned | Do not add competing gradients nearby | — | — (maintain) |

**Not recommended:** Personal Information cards, Privacy rows, Stations directory rows, Profile menu tiles, Danger/logout buttons, dense GPS status cards (flat clarity is better).

---

## 5. Layout/Spacing Issues

| Screen | Issue | Suggested fix direction | Priority |
|--------|-------|-------------------------|----------|
| Rewards | Uses `AppScreenHeader.secondary` (back + title, no logo) | **Decided correct** — Rewards is a secondary/pushed destination, not a bottom-nav tab. Keep secondary header. | — (accepted) |
| Rewards / shell | No bottom-nav tab for Rewards | **Decided intentional** — entry via Home Claim Rewards, Success/Reward CTA, Profile if applicable. Not P0. | — (accepted) |
| Stamp Collected Success | No AppBar/close chrome; large `Spacer`s | Add optional close or ensure exit CTAs are obvious; tighten vertical rhythm vs Visily when assets available | P2 |
| Location Verification | Abstract map placeholder above dense GPS cards | Reduce placeholder height or replace with clearer illustration; balance card spacing | P2 |
| Station Detail | Sticky red CTA + EN ALL-CAPS weight | Keep sticky pattern; shorten label; ensure bottom inset clears gesture area | P2 |
| Tap To Collect / Welcome | Fake multi-step page dots without real pages | Remove dots or implement real steps | P2 |
| Scan Error | Close (`Icons.close`) vs `AppBackButton` on sibling scan screens | Standardize back/close pattern across scan flow | P3 |
| Scan flow screens | Mix of local `AppBar` vs shared header factories | Acceptable for modal flow; document as intentional or migrate to `AppScreenHeader.secondary` | P3 |
| Tab screens + `AppVersionFooter` | Generally padded with `shellBottomInset` | Keep; verify no footer collision with notch FAB on small devices (**NEEDS_MANUAL_DECISION** with device QA) | P3 |
| Home | Shortcut grid + social proof + version can feel long | Keep sections; avoid adding more chrome; optional spacing tighten after Visily compare | P3 |
| Reward Unlocked Share (empty) | Bare centered text, no illustration/CTA hierarchy | Use `AppEmptyState` + primary action (e.g. back to Rewards) | P1 |

---

## 6. Icon and Visual Language Issues

| Screen/component | Current icon/style | Problem | Suggested icon/style | Priority |
|------------------|--------------------|---------|----------------------|----------|
| Bottom nav — Stamp | FontAwesome stamp; active = inactive | No active-state differentiation; mixed FA + Material | Distinct filled/outline pair; keep stamp metaphor | P2 |
| Bottom nav — Scan FAB | `Icons.center_focus_weak_rounded` | Reads QR/focus more than NFC-only MVP collect | Prefer NFC-forward icon when Visily assets available; do not imply QR collect | P2 |
| Bottom nav — Stations | List bulleted | Fine but generic | Keep unless Visily specifies map/pin | P3 |
| Home collect CTA | `Icons.nfc_rounded` | Good NFC signal | Keep | — |
| Profile — API Debug | `Icons.bug_report_outlined` | Correct for debug; must stay gated | Keep; never show in release | P0 (gate) |
| Empty states (shared) | `Icons.inbox_outlined` default | Generic “empty box” | Feature-specific: stamp / gift / station pin | P2 |
| Rewards empty | `Icons.card_giftcard_outlined` | Acceptable but plain | Keep + soft empty wash | P3 |
| Voucher Detail redeem-code QR | `Icons.qr_code_2_rounded` placeholder | Looks unfinished in-store (voucher redeem graphic — **not** stamp-collect QR) | Optional: generate QR from redeem code later; do not conflate with gated collect QR | P2 / Later |
| Scan Error | Generic error outline icon | Functional but not branded | Soften with phase-specific illustration if design allows | P3 |
| Tap To Collect | Local bolt `_BrandMark` | One-off vs shared logo | Use shared `AppLogo` / brand asset | P2 |
| Stamp active/inactive (`AppIcons`) | Same FA stamp for both | Active tab unclear | Separate active glyph/weight | P2 |

---

## 7. Dev/Backend-looking UI

UI that feels intended for developers, backend, or admin rather than normal users:

### Must hide or rewrite for production users

| Item | Where | Notes |
|------|-------|-------|
| Raw `backendCode` under error message | `lib/shared/widgets/error_state.dart` (`AppErrorState`) | **P0** — API codes visible whenever `failure.backendCode` is set |
| `EDITABLE DETAILS` / `READ-ONLY` | Personal Information | Form/admin taxonomy |
| “backend update is not supported yet” | Personal Information phone helper | Explicit backend limitation |
| “server-issued rewards” / “from the server” / “pending fulfillment from the server” | Rewards empty + pending banner | Integration language |
| “xác minh bởi backend” / “Phạm vi ga (máy chủ)” / architecture GPS sentence | Location Verification | Exposes validation architecture |
| “backend progress” / “checked by the server” | Help Center FAQ | Help should be user-facing |
| “when the backend provides profile data” | Profile stats empty | Dev placeholder |
| “Kiểm tra backend và mạng” | Auth cubit connection errors | Internal word in user toast/message |
| “Có thể backend vẫn đang xử lý” | Scan flow cubit message | Same |
| `NFC tạm tắt (iOS test)` | Scan screen status | Test-build label |

### Gated / internal tools (acceptable if correctly gated)

| Item | Where | Notes |
|------|-------|-------|
| **API Debug** | Profile menu → `RouteNames.apiDebug` | Shown when `kDebugMode` + DI initialized. Must remain **absent in release**. |
| **NFC Tag Writer** | Profile menu | Admin role gate. Internal ops tool; not consumer UX. |
| Reward Unlocked preview via API Debug | Debug only | Do not use as production unlock path |

### Not user-visible (do not treat as UI defects)

- Code comments / TODOs on success screen about reward unlock  
- Redeem mutation omitted due to backend 410 (comment-only; UI shows copy/code path)  
- Favorite station omitted (no API) — already hidden  

### Flow gap (not wording-only)

- **Reward Unlocked Share** routes exist but production collect cannot open unlock celebration until backend exposes unlock payload (or agreed poll). Classification: **BACKEND_CONTRACT_REQUIRED** — do not fake in UI polish sprint.

---

## 8. Prioritized Implementation Plan

### Sprint A — remove technical/dev-facing wording + hide debug leakage

1. Rewrite Personal Information section labels and phone helper (P0).  
2. Rewrite Rewards empty / pending banners — remove server language (P0).  
3. Rewrite Location Verification GPS / zone copy (P0).  
4. Stop showing raw `backendCode` in `AppErrorState` for production users (P0).  
5. Soften Help Center, Profile stats empty, auth/scan “backend” messages (P1).  
6. Soften Scan “iOS test”; ensure QR **collect** CTAs/copy stay hidden while gated (P1).  
7. Soften Station Detail collect CTA tone if it reads producty/ALL-CAPS (P1) — **not** full app locale pass.  
8. Confirm release builds never show API Debug (P0 gate check).  
9. Do **not** add Rewards to bottom nav; do **not** restore QR collect UI.

### Sprint B — targeted gradients only (high-value surfaces)

1. Soft gradients: Home progress card, Rewards progress card (P2).  
2. Subtle Home collect CTA red gradient (P2).  
3. NFC Scan backdrop wash behind pulse (P2).  
4. Success + Reward Unlocked celebration atmosphere (P2).  
5. Soft empty-state decoration on Rewards / Stamp Book only if subtle (P3).  
6. Do **not** gradient settings rows, list rows, logout, or every card. Do **not** restore QR collect.

### Sprint C — visible component consistency (no broad card refactor)

1. Keep Rewards as secondary screen with `AppScreenHeader.secondary` (decided).  
2. Standardize detail/flow back chrome only where inconsistency is visible (`AppBackButton` vs close) (P2–P3).  
3. Align scan primary buttons with shared button radius/padding where mismatch is visible (P2).  
4. Stamp nav active/inactive icon pair; optional NFC-forward Scan FAB icon (P2).  
5. Prefer shared empty/error widgets with feature-specific icons where empty looks raw (P2).  
6. Replace Tap To Collect local brand mark with shared logo if visible drift (P2).  
7. Do **not** broad-adopt unused `StationCard` / `VoucherCard` / `ProgressCard` — no broad card refactor in this sprint.

### Later / not current polish sprint

- Full Flutter l10n (ARB) and single locale product decision.  
- Visily pixel-pass once `docs/design/visily/*.png` + PDF are available.  
- Wire Reward Unlocked from collect (**BACKEND_CONTRACT_REQUIRED** — do not fake).  
- Real map on Location Verification.  
- Optional voucher **redeem-code** QR graphic polish (distinct from collect QR).  
- Broad card redesign of every list row.  
- Re-enabling QR collect (product decision only; stays gated for MVP).

---

## 9. Files to Review Manually

### Design / docs

- [`mobile/docs/design/README.md`](./README.md)
- [`mobile/docs/design/UI_SCREEN_INVENTORY.md`](./UI_SCREEN_INVENTORY.md)
- [`mobile/docs/design/UI_IMPLEMENTATION_AUDIT_REPORT.md`](./UI_IMPLEMENTATION_AUDIT_REPORT.md)
- `mobile/docs/design/visily/*` — **restore assets for visual QA** (**NEEDS_MANUAL_DECISION** until present)

### Theme / shared chrome

- `lib/app/theme/app_colors.dart`
- `lib/app/theme/app_spacing.dart`
- `lib/app/theme/app_icons.dart`
- `lib/app/theme/app_typography.dart` / `app_text_styles.dart`
- `lib/shared/widgets/app_screen_header.dart`
- `lib/shared/widgets/app_back_button.dart`
- `lib/shared/widgets/app_version_footer.dart`
- `lib/shared/widgets/app_action_buttons.dart`
- `lib/shared/widgets/empty_state.dart` / `error_state.dart` / `loading_state.dart`
- `lib/shared/widgets/progress_card.dart` / `station_card.dart` / `voucher_card.dart` (shared but underused)

### Shell / navigation

- `lib/features/app_shell/presentation/screens/main_shell_screen.dart`
- `lib/features/app_shell/presentation/widgets/bottom_nav_bar.dart`
- `lib/features/app_shell/presentation/widgets/shell_nav_metrics.dart`

### Home

- `lib/features/home/presentation/screens/home_screen.dart`
- `lib/features/home/presentation/widgets/home_header.dart`
- `lib/features/home/presentation/widgets/home_collection_card.dart`
- `lib/features/home/presentation/widgets/home_collect_cta.dart`
- `lib/features/home/presentation/widgets/home_shortcut_grid.dart`
- `lib/features/home/presentation/widgets/home_banner_carousel.dart`
- `lib/features/home/presentation/widgets/home_recent_stamps_section.dart`
- `lib/features/home/presentation/widgets/home_social_proof_strip.dart`

### Stamp Book

- `lib/features/stamp_book/presentation/screens/stamp_book_screen.dart`
- `lib/features/stamp_book/presentation/screens/stamp_detail_screen.dart`
- `lib/features/stamp_book/presentation/widgets/stamp_book_header.dart`
- `lib/features/stamp_book/presentation/widgets/stamp_book_summary_card.dart`
- `lib/features/stamp_book/presentation/widgets/stamp_book_footer.dart`
- `lib/features/stamp_book/presentation/widgets/stamp_detail_sections.dart`

### Stations

- `lib/features/stations/presentation/screens/stations_list_screen.dart`
- `lib/features/stations/presentation/screens/station_detail_screen.dart`
- `lib/features/stations/presentation/widgets/stations_header.dart`
- `lib/features/stations/presentation/widgets/nearby_station_hero_card.dart`
- `lib/features/stations/presentation/widgets/station_directory_row.dart`
- `lib/features/stations/presentation/widgets/station_detail_hero.dart`
- `lib/features/stations/presentation/widgets/station_collect_cta.dart`
- `lib/features/stations/presentation/widgets/station_action_row.dart`

### Scan flow

- `lib/features/scan/presentation/screens/tap_to_collect_screen.dart`
- `lib/features/scan/presentation/screens/scan_screen.dart`
- `lib/features/scan/presentation/screens/location_verification_screen.dart`
- `lib/features/scan/presentation/screens/stamp_collected_success_screen.dart`
- `lib/features/scan/presentation/screens/scan_error_screen.dart`
- `lib/features/scan/presentation/widgets/nfc_pulse_circle.dart`
- `lib/features/scan/presentation/utils/scan_error_presentation.dart`
- `lib/features/scan/presentation/widgets/scan_action_buttons.dart`

### Rewards

- `lib/features/rewards/presentation/screens/rewards_screen.dart`
- `lib/features/rewards/presentation/screens/voucher_detail_screen.dart`
- `lib/features/rewards/presentation/screens/reward_unlocked_share_screen.dart`
- `lib/features/rewards/presentation/widgets/rewards_screen_header.dart`
- `lib/features/rewards/presentation/widgets/rewards_progress_card.dart`
- `lib/features/rewards/presentation/widgets/reward_voucher_card.dart`
- `lib/features/rewards/presentation/widgets/voucher_detail_sections.dart`
- `lib/features/rewards/presentation/widgets/reward_unlocked_sections.dart`

### Profile / settings / help

- `lib/features/profile/presentation/screens/profile_screen.dart`
- `lib/features/profile/presentation/screens/personal_information_screen.dart`
- `lib/features/profile/presentation/screens/privacy_security_screen.dart`
- `lib/features/profile/presentation/screens/help_center_screen.dart`
- `lib/features/profile/presentation/widgets/profile_header.dart`
- `lib/features/profile/presentation/widgets/profile_menu_section.dart`
- `lib/features/profile/presentation/widgets/profile_stats_row.dart`
- `lib/features/profile/presentation/widgets/profile_sections.dart`
- `lib/features/debug/presentation/screens/api_debug_screen.dart` (debug only)
- `lib/features/admin_nfc/presentation/screens/nfc_tag_writer_screen.dart` (admin only)

### Memories / share

- `lib/features/memories/presentation/screens/photo_share_screen.dart`
- `lib/features/memories/presentation/widgets/photo_share_preview.dart`
- `lib/features/memories/presentation/widgets/photo_picker_placeholder.dart`

### Auth (wording spill)

- `lib/features/auth/presentation/cubit/auth_cubit.dart` (connection messages mentioning “backend”)

---

## Appendix — Product decisions

| Topic | Status |
|-------|--------|
| Bottom nav | **Decided:** Home \| Stamp \| Scan FAB \| Stations \| Profile |
| Rewards placement | **Decided:** secondary screen (not a tab); entry from Home Claim Rewards, Success/Reward CTA, Profile if applicable |
| NFC vs QR collect (MVP) | **Decided:** NFC only visible; QR collect gated — do not restore via polish |
| Rewards missing from bottom nav | **Decided:** not P0 |
| Primary UI language (VI vs EN) | **Later** — not current polish sprint |
| Scan FAB icon (focus vs NFC) | **NEEDS_MANUAL_DECISION** pending Visily assets (P2) |
| Visily pixel fidelity | **NEEDS_MANUAL_DECISION** — assets missing from workspace at audit time |
| Reward unlock after collect | **BACKEND_CONTRACT_REQUIRED** — do not fake unlock in UI |

---

*End of report. No source code was modified.*
