# Mobile Android/iOS Compatibility Report

Date: 2026-08-01
Project: `mobile/`
Flutter SDK: 3.41.0
Dart SDK: 3.11.0

## 1. Root cause

The project was still pinned to `font_awesome_flutter: ^10.8.0`, which resolved to `10.12.0`.

Under Flutter 3.41.0 / Dart 3.11.0, `IconData` is now a final class. `font_awesome_flutter` 11.x models Font Awesome icons as `FaIconData`, so the auth social-button widgets failed because they:

- accepted `IconData`
- received `FontAwesomeIcons.*` values
- rendered them with `FaIcon`

That created `IconData` / `FaIconData` assignment errors.

## 2. Confirmed successful checks

- `flutter --version`
- `dart --version`
- `flutter doctor -v`
- `flutter pub deps`
- `flutter pub outdated`
- `flutter pub get`
- `flutter analyze`
- `flutter test`
- `flutter build apk --debug`
- `flutter build appbundle --debug`
- `Info.plist` XML parsing
- `Runner.entitlements` XML parsing

## 3. Files changed

- `mobile/pubspec.yaml`
- `mobile/pubspec.lock`
- `mobile/lib/features/auth/presentation/screens/login_screen.dart`
- `mobile/lib/features/auth/presentation/screens/register_screen.dart`
- `mobile/android/gradle.properties`
- `mobile/ios/Podfile`

## 4. Exact changes made

### Dependency

- Updated `font_awesome_flutter` from `^10.8.0` to `^11.0.0`.
- Ran `flutter pub get`, which updated `pubspec.lock` to resolved `11.0.0`.

### Dart source

- Changed `_SocialButton.icon` in `login_screen.dart` from `IconData` to `FaIconData`.
- Changed `_RegisterSocialButton.icon` in `register_screen.dart` from `IconData` to `FaIconData`.
- Kept rendering with `FaIcon`.
- Left Material icon fields as `IconData`; no global icon-type rewrite was performed.

### Android build configuration

- Added `kotlin.incremental=false` to `android/gradle.properties`.
- Reason: on this Windows machine the repo is on `D:` while pub cache is on `C:`. Kotlin incremental cache handling failed across drive roots during plugin compilation. Disabling incremental Kotlin compilation fixed the Android debug build without changing runtime behavior.

### iOS configuration

- Extended the existing `post_install` block in `ios/Podfile`.
- Added a guarded normalization that raises any pod deployment target below `13.0` up to `13.0`.
- This matches the existing project iOS baseline already declared in:
  - `ios/Podfile`
  - `ios/Runner.xcodeproj/project.pbxproj`
- Existing Flutter pod setup and entitlements were preserved.

## 5. Dependency compatibility table

| Package | Old version | New/resolved version | Reason for change | Android impact | iOS impact |
|---|---:|---:|---|---|---|
| `font_awesome_flutter` | `10.12.0` resolved from `^10.8.0` | `11.0.0` resolved from `^11.0.0` | Required for compatibility with Flutter 3.41.0 and `FaIconData` API | No runtime behavior change; compile fix only | No runtime behavior change; compile fix only |

No other package versions were changed.

## 6. Dependency audit summary

### SDK constraints

- `pubspec.yaml` Dart constraint: `>=3.3.0 <4.0.0`
- Resolved lockfile SDKs:
  - Dart: `>=3.11.0 <4.0.0`
  - Flutter: `>=3.38.4`

These are compatible with the installed toolchain:

- Flutter 3.41.0
- Dart 3.11.0

### Dependency overrides

- No `dependency_overrides` section found.

### Direct dependencies with native Android/iOS plugin surface

- `connectivity_plus`
- `device_info_plus`
- `flutter_secure_storage`
- `geolocator`
- `image_picker`
- `mobile_scanner`
- `nfc_manager`
- `package_info_plus`
- `path_provider`
- `share_plus`
- `shared_preferences`
- `url_launcher`

Resolved dependency graph includes corresponding Apple/Android platform packages, so shared code remains platform-capable.

### Packages with newer versions available but not upgraded

Several packages show newer versions in `flutter pub outdated`, including `go_router`, `geolocator`, `share_plus`, `device_info_plus`, `flutter_secure_storage`, and others. These were not upgraded because:

- they are unrelated to the current breakage
- the current resolved versions analyze and test cleanly
- Android debug APK and debug AAB now build successfully
- broader upgrades would increase regression risk without necessity

## 7. Platform configuration audit

### Android

- `namespace`: `com.example.metro_stamp_app`
- `applicationId`: `com.example.metro_stamp_app`
- `compileSdk`: inherited from Flutter Gradle plugin
- `targetSdk`: inherited from Flutter Gradle plugin
- `minSdk`: inherited from Flutter Gradle plugin
- Java target: 17
- Kotlin JVM target: 17

Notes:

- NFC permission remains present in `android/app/src/main/AndroidManifest.xml`.
- Camera and location permissions remain present.
- `android.hardware.nfc` feature remains declared.
- Android build completed successfully, so current min SDK is sufficient for the resolved plugin set in this environment.

### iOS

- `ios/Podfile` platform: `13.0`
- `ios/Runner.xcodeproj/project.pbxproj` deployment target: `13.0`
- `Runner.entitlements` preserves NFC session formats
- `Runner/Info.plist` includes `NFCReaderUsageDescription`
- No simulator-only workaround was added
- No shared Dart file received an iOS-only import

## 8. Validation results

### Passed

- `flutter analyze`: passed with no issues
- `flutter test`: passed
- `flutter build apk --debug`: passed
- `flutter build appbundle --debug`: passed

### Resolved during validation

- Initial Android build attempts failed on Windows due Kotlin incremental cache issues across `C:` pub cache and `D:` workspace paths.
- After setting `kotlin.incremental=false`, both debug Android builds succeeded.

## 9. Warnings

- `dart format --output=none --set-exit-if-changed lib test` reports formatting changes across many files in this repo. I did not include a broad formatting sweep because that would expand the change set far beyond the requested compatibility fix.
- `flutter test` logs expected test-time warnings from mocked startup/plugin scenarios such as `MissingPluginException` for `package_info_plus`, but the suite still passes.
- `ruby` was not available on this Windows machine, so Podfile syntax was validated by structure review rather than `ruby -c`.

## 10. Untested iOS checks

These were reviewed statically only on Windows and are not confirmed by a real Apple toolchain build:

- CocoaPods installation on macOS
- Xcode project indexing/build
- `flutter build ios --debug --no-codesign`
- Physical iPhone deployment

## 11. Actual blockers

- No code blocker remains for Android.
- No source-level iOS blocker was found during static inspection.
- macOS/Xcode validation is still required before claiming iOS build success.

## 12. Commands executed

From `mobile/`:

```powershell
flutter --version
dart --version
flutter doctor -v
Get-Content -Raw pubspec.yaml
Get-Content -Raw pubspec.lock
flutter pub deps
flutter pub outdated
flutter pub get
flutter analyze
flutter test
flutter clean
flutter pub get
flutter test
flutter build apk --debug
flutter build appbundle --debug
```

Static inspection commands:

```powershell
[xml](Get-Content -Raw 'ios/Runner/Info.plist') | Out-Null
[xml](Get-Content -Raw 'ios/Runner/Runner.entitlements') | Out-Null
git status --short
git diff --stat
git diff -- mobile/pubspec.yaml mobile/pubspec.lock mobile/lib/features/auth/presentation/screens/login_screen.dart mobile/lib/features/auth/presentation/screens/register_screen.dart mobile/android/gradle.properties mobile/ios/Podfile
```

## 13. macOS validation commands

Run these on macOS exactly, from the user-provided path:

```bash
cd /Users/dungle2906/Documents/ExoticStamp/exotic_stamp/mobile

flutter --version
flutter pub get
flutter analyze
flutter test
flutter build ios --debug --no-codesign

cd ios
pod install --repo-update
cd ..

open ios/Runner.xcworkspace
```

## 14. Git diff summary

- 6 tracked files changed
- 13 insertions
- 5 deletions

Tracked changes are limited to:

- one dependency declaration
- one lockfile resolution update
- two auth screen type fixes
- one Android Gradle property
- one iOS Podfile post-install normalization

No generated or cached directories were added to git.

## 15. Final verdict

- Android ready
- iOS source ready but macOS validation pending
