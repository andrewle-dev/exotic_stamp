# Mobile Release Checklist — Exotic Stamp MVP

> Last updated: 2026-06-25 (M10)  
> App: `metro_stamp_app` v0.1.0+1

---

## 1. Dependencies

```bash
cd mobile
flutter pub get
```

Key runtime deps: `dio`, `flutter_bloc`, `go_router`, `nfc_manager`, `geolocator`, `mobile_scanner`, `image_picker`, `share_plus`, `flutter_secure_storage`.

Run `flutter pub outdated` periodically; MVP pins conservative versions in `pubspec.yaml`.

---

## 2. Environment / API base URL

Config: `lib/core/config/api_config.dart`

| Target | API base | Command |
|--------|----------|---------|
| Android emulator | `http://10.0.2.2:8080/api/v1` | `flutter run` (default) |
| iOS simulator / desktop | `http://localhost:8080/api/v1` | `flutter run` |
| Physical device (LAN) | `http://<LAN-IP>:8080/api/v1` | `flutter run --dart-define=API_HOST=192.168.x.x` |
| Custom port | same host | `--dart-define=API_PORT=8080` |
| Staging/prod API | HTTPS host | **Future:** add `API_USE_HTTPS` or flavor; today use dart-define host pointing to HTTPS reverse proxy |

**No secrets in `lib/`.** Tokens stored in `flutter_secure_storage` only.

**No token/cookie logs** in production code (M10 audit: PASS).

---

## 3. Android permissions (`AndroidManifest.xml`)

| Permission | Declared | Used by |
|------------|----------|---------|
| `INTERNET` | yes | API client |
| `NFC` | yes | Primary scan flow |
| `ACCESS_FINE_LOCATION` | yes | Collect GPS validation |
| `ACCESS_COARSE_LOCATION` | yes | Collect GPS validation |
| `CAMERA` | yes | QR fallback (`mobile_scanner`) |

Photo share (`image_picker`): uses system photo picker on modern Android; no extra storage permission required for picker flow.

`android:usesCleartextTraffic="true"` — **dev/LAN HTTP only**. Remove or use network security config for production HTTPS.

---

## 4. App identity & branding

| Item | Current | Demo OK? | Store ready? |
|------|---------|----------|--------------|
| `android:label` | `Exotic Stamp` | yes | yes |
| `applicationId` | `com.example.metro_stamp_app` | yes | **no** — change before Play Store |
| App icon | `@mipmap/ic_launcher` | partial | **run icon generator** |
| Logo asset | `assets/logo/ExoticStamp_logo.png` referenced in pubspec | **missing file** | add logo + `dart run flutter_launcher_icons` |
| Splash | default `launch_background.xml` (white) | yes | customize for polish |
| Version | `0.1.0+1` | yes | bump for releases |

```bash
# After adding assets/logo/ExoticStamp_logo.png:
dart run flutter_launcher_icons
```

---

## 5. Build commands

### Debug (development)

```bash
cd mobile
flutter pub get
flutter run
# or
flutter build apk --debug
```

Output: `build/app/outputs/flutter-apk/app-debug.apk`

### Staging / LAN demo (release binary, debug signing)

```bash
flutter build apk --release \
  --dart-define=API_HOST=192.168.1.100 \
  --dart-define=API_PORT=8080
```

Uses **debug signing** per `android/app/build.gradle.kts` (acceptable for internal demo, not Play Store).

### Production release APK

```bash
flutter build apk --release \
  --dart-define=API_HOST=api.yourdomain.com \
  --dart-define=API_PORT=443
# Note: ApiConfig currently builds http:// — HTTPS production requires small config extension
```

### Obfuscation (supported — M10 verified)

```bash
flutter build apk --release \
  --obfuscate \
  --split-debug-info=build/symbols
```

Output: `build/app/outputs/flutter-apk/app-release.apk` (~77 MB)  
Symbols: `build/symbols/` (keep for crash deobfuscation)

### App Bundle (Play Store — future)

```bash
flutter build appbundle --release --obfuscate --split-debug-info=build/symbols
```

Requires release signing config (not configured in MVP).

---

## 6. Install on physical device

```bash
# USB debugging enabled
adb install -r build/app/outputs/flutter-apk/app-release.apk

# Or sideload APK file directly
```

Ensure phone and backend host are on same Wi‑Fi when using `API_HOST=<LAN-IP>`.

---

## 7. Release build settings (`android/app/build.gradle.kts`)

| Setting | Value |
|---------|-------|
| `compileSdk` | Flutter default |
| `minSdk` | Flutter default (typically 21+) |
| `targetSdk` | Flutter default |
| Java | 17 |
| Release signing | **debug keys** (TODO: release keystore) |

---

## 8. Smoke test checklist (post-install)

See also: `mobile/docs/qa/MVP_E2E_TEST_PLAN.md`

- [ ] Login with `mobiletest` / demo password (dev backend)
- [ ] Home loads progress + recent stamps (no fake data on error)
- [ ] Stations list + detail
- [ ] NFC tap or QR fallback → GPS → collect
- [ ] Collect timeout → **Kiểm tra trạng thái** (collect status endpoint)
- [ ] Stamp book refresh shows new stamp
- [ ] Rewards + voucher detail (code from backend)
- [ ] Profile + logout clears session
- [ ] Photo share opens native share sheet

---

## 9. Known device limitations

| Limitation | Notes |
|------------|-------|
| NFC | **Must test on physical Android** — emulator has no NFC |
| iOS NFC | May be disabled on test builds; QR fallback |
| GPS | Emulator mock location; real device needed for accuracy tests |
| HTTP cleartext | Required for local LAN demo; not for public prod |
| `com.example` package | Internal demo only |

### Recommended NFC test devices

- Samsung A-series (NFC)
- Xiaomi Redmi (NFC)
- Oppo Reno (NFC)

### GPS test notes

- Stand within station zone (~150 m per backend config)
- Deny permission → collect must not proceed
- Weak GPS → low-accuracy or outside-range errors from backend

---

## 10. Pre-demo checklist

- [ ] Backend running (`dev` profile) with seed data
- [ ] `flutter test` green
- [ ] APK built and installed on demo phone
- [ ] `API_HOST` points to laptop LAN IP
- [ ] NFC tag or QR payload matches seeded station (`M1-NFC-001` etc.)
- [ ] Demo user credentials ready
- [ ] Voucher milestone collected for rewards demo (optional)
