# MVP End-to-End Test Plan — Exotic Stamp Mobile (M9B)

> Manual and automated QA checklist for fullstack integration before M10.
> Last updated: 2026-06-25 (M9B)

---

## 1. Setup prerequisites

| Item | Requirement |
|------|-------------|
| Flutter SDK | Project `pubspec.yaml` constraint (run `flutter doctor`) |
| Backend | Spring Boot API running and reachable from device/emulator |
| Database | Seeded metro lines, stations, active campaign, NFC/QR scan keys |
| Test user | Registered account with known credentials |
| Android emulator | API reachable at `http://10.0.2.2:8080` (default `ApiConfig`) |
| Physical Android | Same LAN; run with `--dart-define=API_HOST=<LAN-IP>` |
| NFC tags | Programmed with backend-registered scan payloads |
| GPS | Emulator extended controls or physical device at/near station |

### Backend run (typical)

```bash
# From repo root — adjust to your backend README
cd backend
./mvnw spring-boot:run
# or: docker compose up
```

Verify Swagger: `http://localhost:8080/swagger-ui/index.html`

### Mobile run

```bash
cd mobile
flutter pub get
dart run build_runner build --delete-conflicting-outputs   # if code gen needed
flutter run
```

**Physical device (LAN):**

```bash
flutter run --dart-define=API_HOST=192.168.1.100 --dart-define=API_PORT=8080
```

**Staging/production:** configure `API_HOST` / `API_PORT` via `--dart-define` or CI flavor; do not hardcode secrets in `lib/`.

---

## 2. Seed data requirements

Minimum backend seed for meaningful E2E:

- [ ] At least one **active campaign** with stations assigned
- [ ] Metro **line(s)** with **stations** (coordinates within GPS validation radius)
- [ ] **NFC scan key** linked to a station (and optional QR fallback key)
- [ ] User account **not** having collected target station (for success path)
- [ ] Optional: user with **partial** stamp book for progress/rewards UI
- [ ] Optional: issued **voucher** for rewards detail path

---

## 3. Automated test coverage (run in CI / locally)

```bash
cd mobile
dart format .
flutter analyze
flutter test
```

| Area | Test location | What it verifies |
|------|---------------|------------------|
| Auth | `test/features/auth/` | Login/register/logout mapping, token storage |
| Home | `test/features/home/` | Composed dashboard, partial failure handling |
| Stations | `test/features/stations/` | Collected badge unknown on stamp-book merge fail |
| Scan | `test/features/scan/` | NFC-first flow, no local stamp on timeout, collect status |
| Stamp book | `test/features/stamp_book/` | Backend-driven grid, refresh |
| Rewards | `test/features/rewards/rewards_integrity_test.dart` | No `/redeem` in production |
| Profile | `test/features/profile/profile_integrity_test.dart` | No mock profile strings |
| Memories | `test/features/memories/memories_integrity_test.dart` | Share-events only, no upload |

### Integration tests

`integration_test/` is **not present** in this repo. Full device E2E (NFC + GPS + live API) requires:

- `integration_test` package + `integration_test/app_test.dart`
- Running backend on reachable host
- Physical NFC hardware

**M9B decision:** document manual device checklist below; add `integration_test` in M10 if backend test env is stable.

---

## 4. Android emulator checklist

### Auth

| # | Steps | Expected | Evidence |
|---|-------|----------|----------|
| A1 | Register new user | 201/200, navigates to app | _screenshot_ |
| A2 | Logout → login | Session restored from backend | _screenshot_ |
| A3 | Kill app, reopen | Refresh restores session if token valid | _screenshot_ |
| A4 | Logout | Token + cookies cleared locally even if backend logout fails | _logs off_ |

### Home

| # | Steps | Expected | Evidence |
|---|-------|----------|----------|
| H1 | Open Home tab | Real user name, progress from `/collection/progress` | _screenshot_ |
| H2 | Stop backend mid-load | Error/empty sections — **no fake stamps or counts** | _screenshot_ |

### Stations

| # | Steps | Expected | Evidence |
|---|-------|----------|----------|
| S1 | Open Stations | Lines + stations from `/metro/*` | _screenshot_ |
| S2 | Open station detail | Hero, story, CTA from backend | _screenshot_ |
| S3 | Collected badge | Matches stamp-book merge; if merge fails → **unknown** (no fake uncollected) | _screenshot_ |

### Scan (emulator — QR fallback if no NFC)

| # | Steps | Expected | Evidence |
|---|-------|----------|----------|
| SC1 | Valid resolve → GPS OK → collect | Success screen with backend stamp data | _screenshot_ |
| SC2 | Invalid tag / QR | Resolve error — **collect not called** | _screenshot_ |
| SC3 | Duplicate station | Duplicate UI, not crash | _screenshot_ |
| SC4 | Deny location permission | Collect **not** called | _screenshot_ |
| SC5 | GPS outside range | Backend `GPS_OUT_OF_RANGE` message | _screenshot_ |
| SC6 | Simulate timeout (slow proxy) | Uncertain outcome + **Kiểm tra trạng thái** CTA | _screenshot_ |
| SC7 | After timeout, tap status check | SUCCESS → success; DUPLICATE → duplicate; NOT_FOUND → still uncertain | _screenshot_ |

### Stamp book

| # | Steps | Expected | Evidence |
|---|-------|----------|----------|
| B1 | Open Stamp book | Grid from `/collection/stamp-book` | _screenshot_ |
| B2 | Pull to refresh | Refetches backend | _screenshot_ |
| B3 | Locked stamp | No fake `collectedAt` timestamp | _screenshot_ |
| B4 | After scan success | Stamp book refresh shows new stamp | _screenshot_ |

### Rewards

| # | Steps | Expected | Evidence |
|---|-------|----------|----------|
| R1 | Open Rewards | `/rewards/my` + milestones from backend | _screenshot_ |
| R2 | Milestone progress | No locally invented rewards | _screenshot_ |
| R3 | Voucher detail | Code only from backend response | _screenshot_ |
| R4 | Redeem button | **Not** calling redeem API (disabled MVP) | _network log_ |

### Profile

| # | Steps | Expected | Evidence |
|---|-------|----------|----------|
| P1 | Open Profile | `/users/me` data only | _screenshot_ |
| P2 | Stats API fail | Neutral unavailable — **not fake 0** | _screenshot_ |
| P3 | Logout with backend down | Local session still cleared | _screenshot_ |

### Memories

| # | Steps | Expected | Evidence |
|---|-------|----------|----------|
| M1 | Share photo with overlay | Native share sheet opens | _screenshot_ |
| M2 | Share with tracking API down | Share still works (non-blocking) | _screenshot_ |
| M3 | No persisted fake memories list | No local gallery DB of memories | _N/A_ |
| M4 | No image upload | Only `POST /community/share-events` metadata | _network log_ |

---

## 5. Android physical NFC checklist

| # | Steps | Expected | Evidence |
|---|-------|----------|----------|
| N1 | NFC enabled, tap tag at station | NFC read → location verification → collect | _video_ |
| N2 | NFC disabled | QR fallback offered, NFC copy primary on return | _screenshot_ |
| N3 | Samsung / Xiaomi / Oppo (if available) | Tag read within 3s | _notes_ |
| N4 | Weak GPS | Low accuracy or outside range per backend | _screenshot_ |
| N5 | Airplane mode during collect | Uncertain + status check / stamp book CTA | _screenshot_ |

---

## 6. GPS checklist

| # | Condition | Expected |
|---|-----------|----------|
| G1 | Permission granted, high accuracy | Proceeds to resolve + collect |
| G2 | Permission denied | Stops before resolve/collect |
| G3 | Location services off | Service disabled message |
| G4 | Emulator mock location at station | Collect succeeds if within radius |
| G5 | Mock location far from station | `GPS_OUT_OF_RANGE` from backend |

---

## 7. Known limitations (M9B)

| ID | Area | Limitation | Priority |
|----|------|------------|----------|
| L1 | Home | No `GET /home/summary`; composed from 4–5 calls | P2 |
| L2 | Profile | No dedicated profile summary; stats partial | P2 |
| L3 | Stamp detail | No `GET /collection/my-stamps/{id}`; merge workaround | P2 |
| L4 | Stations | Collected flag client-merged from stamp-book | P2 |
| L5 | Rewards | Redeem API returns 410; UI read-only | P1 (by design) |
| L6 | Collect | `rewardUnlocked` / `nextReward` not on collect response | P2 |
| L7 | Integration | No `integration_test/` package yet | P1 for M10 |
| L8 | iOS | NFC may be disabled on test builds; QR fallback | P2 |

---

## 8. API contract reference

- Contract: `docs/api/MOBILE_API_CONTRACT.md`
- OpenAPI: `docs/api/openapi.json`
- Collect status (timeout recovery): `GET /api/v1/collection/collect/status?idempotencyKey={uuid}` — **implemented in mobile M9B**

---

## 9. Sign-off

| Role | Name | Date | Pass/Fail |
|------|------|------|-----------|
| Mobile dev | | | |
| Backend dev | | | |
| QA | | | |

**M9B mobile gate:** all automated tests green + manual SC1–SC7 and N1 on at least one physical NFC device before M10.
