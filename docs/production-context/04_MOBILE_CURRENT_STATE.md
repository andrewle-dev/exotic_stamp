# Mobile Current State

## Stack

- Flutter: `3.41.0`
- Dart: `3.11.0`
- App version in `pubspec.yaml`: `0.1.0+1`
- Package IDs:
  - Android: `com.example.metro_stamp_app`
  - iOS: `com.hoanganh.exoticstamp`

## Build / Tooling Evidence

- `flutter doctor -v`: passed with no issues
- `flutter pub get`: passed
- `flutter analyze`: passed
- `flutter test`: passed (`All tests passed!`)

## API / Auth Model

- API base config priority:
  1. in-app debug override
  2. `--dart-define=API_HOST` / `API_PORT`
  3. Android emulator `10.0.2.2`
  4. iOS simulator / desktop `localhost`
- Mobile transport header: `X-Client-Transport: body`
- Refresh token is not cookie-based; it is stored via secure storage
- Access token is memory-oriented client state with refresh in storage

## Android Status

- `applicationId`: `com.example.metro_stamp_app` (placeholder, not publishable)
- `namespace`: `com.example.metro_stamp_app`
- Release build still signs with `debug` signing config
- `android:usesCleartextTraffic="true"` in main manifest
- Permissions:
  - Internet
  - Camera
  - NFC
  - coarse location
  - fine location
- NFC feature is optional (`required="false"`)

## iOS Status

- Bundle ID in Xcode project: `com.hoanganh.exoticstamp`
- Development team present in project file: `999V3977BA`
- NFC entitlement present for `NDEF`
- `Info.plist` includes usage descriptions for:
  - camera
  - photo library
  - location
  - NFC
- ATS allows local networking

## Mobile Product / Debug Signals

- QR flow is feature-gated by `ENABLE_QR_FLOW`
- Mock mode is feature-gated by `USE_MOCK_DATA`
- Profile route includes debug/API override screen in debug mode
- Admin/internal `NFC Tag Writer` screen exists in mobile code

## Release Blockers

### Android

- Placeholder package ID
- Debug signing used for release
- Cleartext traffic enabled in production manifest
- No evidence of Play-ready signing config files

### iOS

- No App Store/TestFlight readiness evidence in repo beyond base Xcode project
- No provisioning profiles or distribution signing assets were inspected
- Store metadata/assets/legal/account readiness not represented in source

## Platform Readiness Summary

- Android: **BLOCKED for store release**
- iOS: **PARTIAL / operator-dependent**
- Mobile overall: **not on the critical path for Web Admin first release**
