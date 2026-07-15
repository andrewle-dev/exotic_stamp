import 'package:flutter/foundation.dart';

/// Controls mock repository wiring for UI development.
///
/// Enable with: `--dart-define=USE_MOCK_DATA=true`
///
/// Mock write simulations (collect / redeem) are blocked in release builds
/// even if the flag is set at compile time.
abstract final class MockConfig {
  static const useMockData = bool.fromEnvironment(
    'USE_MOCK_DATA',
    defaultValue: false,
  );

  /// True when mock repositories should be wired instead of remote APIs.
  static bool get isMockMode => useMockData && !kReleaseMode;

  /// True when mock collect/redeem simulations are allowed.
  static bool get allowMockWrites => isMockMode;
}
