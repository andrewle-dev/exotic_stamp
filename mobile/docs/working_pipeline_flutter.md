# WORKING PIPELINE FLUTTER - EXOTIC STAMP MOBILE

> Enterprise-grade Flutter pipeline for the Exotic Stamp / Metro Stamp mobile app.
> This file is the operating manual for AI agents and developers working inside `mobile/`.

---

## 1. Scope & Boundary

This document governs the **Flutter mobile app** only.

System shape:

```text
ExoticStamp/
├── backend/    Spring Boot API, DDD modules, DB, Redis, Swagger
├── mobile/     Flutter end-user app
├── docs/       product, architecture, delivery, API contracts
└── infra/      Docker, deployment, infra scripts
```

Mobile app responsibility:

- User authentication UI and session handling.
- NFC-first stamp collection flow.
- GPS permission/checking UI.
- Stamp Book, Stations, Rewards, Profile, Photo Share.
- Rendering backend-confirmed collection/reward/voucher states.

Mobile app must **not** own backend business logic.

---

## 2. Product Rules

Exotic Stamp is **NFC-first**.

Correct runtime flow:

```text
User opens station / scan flow
→ App reads NFC tag
→ App gets GPS + device metadata
→ App sends collect request with idempotency key
→ Backend validates station/campaign/GPS/duplicate
→ Backend persists user_stamps
→ Backend evaluates reward/voucher
→ App renders success/error from backend response
```

QR is fallback only.

Do not make QR visually or architecturally dominant unless the specific screen is fallback handling.

---

## 3. Design Source Rules

Design references live under:

```text
mobile/docs/design/
├── README.md
├── UI_SCREEN_INVENTORY.md
└── visily/
    ├── exotic-stamp-multiscreens.pdf
    └── exotic-stamp-multiscreens/*.png
```

Rules:

- Use Visily PNG/PDF as visual reference only.
- Do not paste Visily exported code into Flutter production code.
- Implement screens from `UI_SCREEN_INVENTORY.md`.
- If design changes, update `UI_SCREEN_INVENTORY.md` before coding.

---

## 4. Brand Tokens

Flutter must centralize tokens.

Recommended location:

```text
lib/app/theme/app_colors.dart
lib/app/theme/app_text_styles.dart
lib/app/theme/app_spacing.dart
lib/app/theme/app_radius.dart
lib/app/theme/app_theme.dart
```

Required colors:

```dart
class AppColors {
  static const primaryBlue = Color(0xFF01599D);
  static const accentRed = Color(0xFFE83B28);
  static const backgroundWhite = Color(0xFFFFFFFF);

  static const textPrimary = Color(0xFF1D2433);
  static const textSecondary = Color(0xFF667085);
  static const border = Color(0xFFE4E7EC);
  static const surface = Color(0xFFF8FAFC);
}
```

Forbidden:

- Inline random colors in screen widgets.
- Using Visily's inconsistent generated blue/red values.
- Creating one-off button styles per screen.

---

## 5. Flutter Source Structure

Use feature-first Clean Architecture / DDD-lite.

```text
mobile/
├── docs/
│   ├── design/
│   ├── api/
│   └── working_pipeline_flutter.md
│
├── lib/
│   ├── main.dart
│   │
│   ├── app/
│   │   ├── app.dart
│   │   ├── bootstrap.dart
│   │   ├── env/
│   │   │   ├── app_env.dart
│   │   │   └── flavor.dart
│   │   ├── router/
│   │   │   ├── app_router.dart
│   │   │   └── route_names.dart
│   │   └── theme/
│   │       ├── app_colors.dart
│   │       ├── app_text_styles.dart
│   │       ├── app_spacing.dart
│   │       ├── app_radius.dart
│   │       └── app_theme.dart
│   │
│   ├── core/
│   │   ├── config/
│   │   ├── di/
│   │   ├── errors/
│   │   │   ├── app_exception.dart
│   │   │   ├── failure.dart
│   │   │   └── error_mapper.dart
│   │   ├── network/
│   │   │   ├── api_client.dart
│   │   │   ├── auth_interceptor.dart
│   │   │   ├── error_interceptor.dart
│   │   │   └── retry_policy.dart
│   │   ├── storage/
│   │   │   ├── secure_token_storage.dart
│   │   │   └── local_preferences.dart
│   │   ├── permissions/
│   │   ├── nfc/
│   │   │   ├── nfc_reader.dart
│   │   │   ├── nfc_payload_parser.dart
│   │   │   └── nfc_availability.dart
│   │   ├── location/
│   │   │   ├── location_service.dart
│   │   │   └── location_permission_service.dart
│   │   └── utils/
│   │
│   ├── shared/
│   │   ├── widgets/
│   │   │   ├── app_button.dart
│   │   │   ├── app_text_field.dart
│   │   │   ├── app_error_view.dart
│   │   │   ├── app_empty_state.dart
│   │   │   ├── app_loading_view.dart
│   │   │   └── app_network_image.dart
│   │   ├── extensions/
│   │   └── formatters/
│   │
│   ├── features/
│   │   ├── app_shell/
│   │   ├── auth/
│   │   ├── onboarding/
│   │   ├── home/
│   │   ├── stations/
│   │   ├── scan/
│   │   ├── stamp_book/
│   │   ├── rewards/
│   │   ├── memories/
│   │   └── profile/
│   │
│   ├── l10n/
│   └── gen/
│
├── test/
├── integration_test/
├── assets/
├── android/
├── ios/
├── pubspec.yaml
├── pubspec.lock
├── analysis_options.yaml
└── .gitignore
```

---

## 6. Feature Module Structure

Every feature must use this structure unless explicitly justified.

```text
features/{feature}/
├── data/
│   ├── datasources/
│   │   ├── {feature}_remote_datasource.dart
│   │   └── {feature}_local_datasource.dart
│   ├── models/
│   │   ├── {entity}_model.dart
│   │   └── requests/
│   │       └── {action}_request.dart
│   └── repositories/
│       └── {feature}_repository_impl.dart
│
├── domain/
│   ├── entities/
│   │   └── {entity}.dart
│   ├── repositories/
│   │   └── {feature}_repository.dart
│   ├── value_objects/
│   └── usecases/
│       └── {action}_{entity}_usecase.dart
│
└── presentation/
    ├── bloc/ or cubit/
    │   ├── {feature}_cubit.dart
    │   └── {feature}_state.dart
    ├── screens/
    └── widgets/
```

Dependency direction:

```text
presentation → domain
presentation → application/state
application/state → domain
repository_impl → datasource + model + domain
model ↔ DTO mapping only
```

Forbidden:

- Domain importing Flutter.
- Domain importing Dio/Retrofit/Hive.
- Widget calling Dio directly.
- Screen parsing raw JSON.
- Data model leaking into UI if a domain entity exists.

---

## 7. Feature Ownership Map

| Feature | Screens | Backend module |
|---|---|---|
| `auth` | login, register, forgot password | `auth`, `user` |
| `onboarding` | welcome/onboarding | none |
| `home` | home dashboard | `collection`, `reward`, `metro`, `monetization` |
| `stations` | stations list, station detail | `metro` |
| `scan` | tap to collect, scan, GPS check, success/error | `metro`, `collection`, `reward`, `monetization` |
| `stamp_book` | stamp book, stamp detail | `collection`, `metro` |
| `rewards` | rewards, voucher detail, reward unlocked | `reward` |
| `memories` | photo share, share history | `community` |
| `profile` | profile, settings | `user`, `collection`, `community` |

---

## 8. API Integration Strategy

Do not let the mobile agent scan the entire backend for every UI task. That wastes tokens and causes wrong assumptions.

Recommended workflow:

```text
Root repo / backend agent:
  Generate backend/docs/api/MOBILE_API_CONTRACT.md
  Export backend/docs/api/openapi.json from /v3/api-docs

Mobile repo / mobile agent:
  Copy or reference docs/api/MOBILE_API_CONTRACT.md
  Implement Flutter data models and datasources from contract only
```

Use root repo only when:

- Generating or updating API contract.
- Cross-checking endpoint names from backend source.
- Debugging integration mismatch.
- Changing backend + mobile together.

Use `mobile/` window only when:

- Building UI.
- Wiring Flutter state.
- Implementing mobile datasources from an approved API contract.
- Running Flutter tests/builds.

---

## 9. API Client Rules

Recommended stack:

- `dio` for HTTP client.
- `retrofit` optional for generated API clients.
- `freezed` + `json_serializable` for immutable models.
- `flutter_secure_storage` for refresh/access token storage.
- `go_router` for routing.
- `flutter_bloc`/Cubit for state management.

Every remote datasource must:

- Send access token through interceptor.
- Never log access token, refresh token, OTP, voucher secret, or NFC raw secret.
- Map backend `ApiResponse<T>` and `ErrorResponse` consistently.
- Convert backend errors into typed `Failure`.
- Time out instead of infinite loading.

---

## 10. Backend-Driven State Rules

Flutter must not decide final business outcomes.

Forbidden local state mutations:

- `stamp.collected = true` after NFC read but before backend success.
- `reward.claimed = true` before backend reward issue/claim confirmation.
- `voucher.status = used` before backend redeem confirmation.
- `gpsValid = true` as final validation.
- `nfcValid = true` as final validation.

Allowed local state:

- Loading animation.
- First-launch onboarding complete.
- Input validation before submit.
- Local photo preview.
- Temporary selected tab/filter.

---

## 11. NFC Collection Pipeline - Flutter

```text
Tap scan CTA
→ Check NFC availability
→ If unavailable, show fallback option
→ Read NFC NDEF payload
→ Parse tag payload safely
→ Request GPS permission / current location
→ Create idempotencyKey
→ POST collect request
→ Render backend response
```

Required collect request fields:

```text
scanType: NFC | QR
scanKey: string
latitude: number
longitude: number
accuracyMeters: number
idempotencyKey: string
clientTimestamp: ISO-8601
appVersion: string
deviceFingerprint: string
```

Required failure handling:

- NFC unsupported.
- NFC disabled.
- Invalid NDEF payload.
- GPS permission denied.
- GPS service disabled.
- GPS outside station area.
- Duplicate stamp.
- Expired QR fallback token.
- Network timeout after possible backend success.

Timeout case rule:

```text
If collect request times out, do not assume failure or success.
Use idempotency key to retry or call status endpoint if backend provides one.
```

---

## 12. Routing Map

```text
/welcome
/login
/register
/forgot-password
/home
/stamp-book
/stamps/:stampId
/scan
/scan/tap-to-collect
/scan/location-verification
/scan/success
/scan/error
/stations
/stations/:stationId
/rewards
/rewards/vouchers/:voucherId
/rewards/share
/memories/create
/profile
/profile/settings
```

Use route constants. Do not hardcode route strings across widgets.

---

## 13. Development Pipeline

```text
flutter pub get
→ dart run build_runner build --delete-conflicting-outputs
→ flutter analyze --fatal-infos
→ flutter test --coverage
→ flutter build apk --debug/staging
→ manual device test for NFC + GPS
```

For release:

```text
flutter build apk --release --obfuscate --split-debug-info=./symbols
flutter build appbundle --release --obfuscate --split-debug-info=./symbols
flutter build ipa --release
```

---

## 14. Testing Strategy

### Unit tests

- Usecases.
- Repository mapping.
- Error mapper.
- NFC payload parser.
- Idempotency key generator.

### Widget tests

- Home loading/loaded/error.
- Stamp Book empty/loaded.
- Scan error states.
- Voucher available/used/expired states.
- Profile unauthenticated state.

### Integration tests

- Login → home.
- Station detail → tap collect → GPS → collect success.
- Duplicate scan renders duplicate state.
- Voucher detail prevents redeem if expired/used.

### Manual device tests

Required before claiming scan flow done:

- Samsung A-series NFC.
- Xiaomi Redmi NFC.
- Oppo Reno NFC.
- GPS weak/denied/allowed.
- Offline and request timeout.

---

## 15. Code Review Checklist

Before accepting AI-generated Flutter code:

- [ ] Follows feature folder structure.
- [ ] No direct Dio call inside widgets/screens.
- [ ] No business result decided locally.
- [ ] Colors use `AppColors` only.
- [ ] Strings are ready for l10n or centralized constants.
- [ ] Loading/empty/error states exist.
- [ ] API errors map to typed failures.
- [ ] Token storage uses secure storage.
- [ ] No sensitive logs.
- [ ] Domain layer imports no Flutter/Dio/Hive.
- [ ] Tests added for non-trivial state logic.
- [ ] Screen matches `UI_SCREEN_INVENTORY.md`.

---

## 16. AI Agent Prompt Header

Paste this at the start of mobile code generation tasks:

```text
You are working in Exotic Stamp Flutter mobile app.
Read and follow:
- mobile/docs/working_pipeline_flutter.md
- mobile/docs/design/README.md
- mobile/docs/design/UI_SCREEN_INVENTORY.md
- mobile/docs/api/MOBILE_API_CONTRACT.md if API integration is required

Do not paste generated Visily code.
Implement Flutter manually with feature-first Clean Architecture.
NFC is primary; QR is fallback only.
All business outcomes must be backend-driven.
Use AppColors: #01599D, #E83B28, #FFFFFF.
```

---

## 17. Backend API Contract Requirement

Do not wire real APIs from memory.

Before integration, require one of:

1. `backend/docs/api/MOBILE_API_CONTRACT.md`
2. `backend/docs/api/openapi.json`
3. Live local Swagger: `http://localhost:8080/v3/api-docs`

If these are missing, stop and ask backend agent to generate them.
