# Stakeholder Demo Script — Exotic Stamp MVP

> Duration: ~20–30 minutes  
> Audience: Product, stakeholders, partners  
> Last updated: 2026-06-25 (M10)

---

## Before you start

### Hardware & network

1. Laptop running backend (`dev` profile) on same Wi‑Fi as demo phone
2. **Physical Android phone** with NFC (recommended) or QR-capable camera
3. APK installed: `mobile/build/app/outputs/flutter-apk/app-release.apk`
4. Built with LAN API:  
   `flutter build apk --release --dart-define=API_HOST=<laptop-LAN-IP>`

### Backend (5 min before demo)

```bash
cd backend
export SPRING_PROFILES_ACTIVE=dev
export JWT_SECRET=your-dev-jwt-secret-at-least-32-chars-long
./mvnw spring-boot:run
# or: docker compose up
```

Verify: `http://localhost:8080/swagger-ui/index.html`

### Demo credentials

| User | Username | Password (dev default) |
|------|----------|------------------------|
| Mobile test | `mobiletest` | `changeme-demo-only` |
| Admin (optional) | `admin` | `changeme-dev-only` |

Seeded scan payloads (first 5 stations): `M1-NFC-001` … `M1-NFC-005` (NFC) or `M1-QR-001` … (QR).

---

## Demo flow

### 1. Login (~2 min)

**Show:** Auth gate, clean branded UI.

1. Open app → complete welcome/onboarding if first launch
2. Tap **Đăng nhập**
3. Enter `mobiletest` / `changeme-demo-only`
4. **Say:** "Session uses JWT + secure storage; logout clears tokens even if server is down."

**Expected:** Navigate to Home tab.

---

### 2. Home dashboard (~2 min)

**Show:** Backend-driven progress, not local fake data.

1. Point out user greeting, collection progress card
2. Recent stamps section (may be empty for fresh user)
3. Scan CTA shortcut

**Say:** "Home composes several APIs — progress, recent stamps, campaign banner. No single summary endpoint yet."

**Fallback:** If partial API failure, show error state (no fabricated stamps).

---

### 3. Stations list (~2 min)

1. Open **Stations** tab
2. Show line filter, station cards
3. Tap a station (e.g. first seeded station)

**Say:** "Collected badges merge stamp-book data — if that fails, badge is hidden rather than faked."

---

### 4. Station detail (~2 min)

1. Hero image, line name, story
2. **Chạm NFC** / collect CTA

**Say:** "Station content is public API; NFC secrets are never exposed in list/detail."

---

### 5. NFC scan or QR fallback (~5 min) — **hero moment**

**NFC path (preferred):**

1. Tap center **Scan** tab
2. Tap **Bắt đầu quét NFC** (or hold phone to tag)
3. Use tag programmed with `M1-NFC-001` (or enter via QR fallback)

**QR fallback (if NFC unavailable):**

1. Tap **Dùng QR fallback**
2. Scan printed QR with payload `M1-QR-001`

**Say:** "Product is NFC-first; QR is fallback only."

---

### 6. GPS verification (~1 min)

1. Location verification screen shows resolved station
2. Confirm location permission if prompted
3. Tap confirm to collect

**Say:** "Client GPS is advisory; backend validates distance (~150 m)."

**Tip:** Stand near seeded station coordinates or use emulator mock location for backup.

---

### 7. Stamp success (~1 min)

1. Success screen: station name, stamp art, progress
2. Note collected count / next milestone hint

**Say:** "Success only after backend confirms — never optimistic local stamp."

---

### 8. Stamp Book update (~2 min)

1. Tap **Xem Sổ stamp** from success (or Book tab)
2. Pull to refresh
3. Show newly collected stamp vs locked slots

**Say:** "Grid state comes from `/collection/stamp-book`."

---

### 9. Rewards / Voucher (~3 min)

1. Open **Rewards** tab
2. Show milestone timeline progress
3. If milestone reached, open voucher detail
4. Show voucher code — **present at counter** flow

**Say:** "Redeem API is disabled (410). Partner validates code manually — present-code-at-counter MVP."

Collect more stamps live if needed: 1 / 3 / 5 stamps unlock demo milestones.

---

### 10. Profile / settings / logout (~2 min)

1. Open **Profile**
2. Show user info, stats (or unavailable if APIs partial)
3. Open **Settings** → logout
4. Confirm returned to login

**Say:** "Profile stats are composed from collection + share count — no dedicated profile summary API."

---

### 11. Photo share (~2 min)

1. From stamp detail or success → **Share** / memories flow
2. Pick photo, overlay stamp preview
3. Native share sheet opens

**Say:** "Share is local + optional analytics event — no image upload, no persisted memories gallery."

---

## Timeout recovery demo (optional, ~2 min)

1. Simulate slow network during collect (throttle) or airplane mode briefly
2. Show **uncertain outcome** screen
3. Tap **Kiểm tra trạng thái** — polls `GET /collection/collect/status`
4. Show resolved success or stamp-book fallback

---

## Known limitations to mention (honest close)

- No Play Store release signing / `com.example` package yet
- Home uses multiple API calls (no `/home/summary`)
- Voucher redeem is view-only; partner scans code manually
- NFC must be validated on real devices
- No in-app ad monetization yet
- Welcome/onboarding screen is functional placeholder

---

## Troubleshooting quick reference

| Issue | Fix |
|-------|-----|
| Cannot reach API | Check `API_HOST` = laptop LAN IP; same Wi‑Fi |
| GPS outside range | Move closer or use station coordinates from Swagger |
| NFC not reading | Enable NFC; try QR fallback with `M1-QR-001` |
| Duplicate stamp | Expected — show duplicate UX, open stamp book |
| Empty rewards | Collect more stamps toward milestones |

---

## Evidence checklist (for demo report)

- [ ] Screenshot: Home loaded
- [ ] Screenshot: Station detail
- [ ] Screenshot/video: NFC or QR scan
- [ ] Screenshot: Collect success
- [ ] Screenshot: Stamp book with new stamp
- [ ] Screenshot: Voucher code screen
- [ ] Screenshot: Photo share sheet
