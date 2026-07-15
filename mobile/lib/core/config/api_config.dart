import 'dart:io';

import '../storage/local_preferences.dart';

/// API and environment configuration.
class ApiConfig {
  const ApiConfig._();

  static const appName = 'Metro Stamp';

  // Installed app version for UI chrome comes from package_info_plus.
  // Update/maintenance policy is fetched from GET /mobile/app-config.

  static const _host = String.fromEnvironment('API_HOST', defaultValue: '');
  static const _port = String.fromEnvironment('API_PORT', defaultValue: '8080');

  /// Optional runtime override (debug / physical device LAN IP).
  static String? _runtimeHostOverride;
  static String? _runtimePortOverride;

  /// Apply in-app override (persisted via [LocalPreferences]).
  static void applyRuntimeOverride({String? host, String? port}) {
    _runtimeHostOverride =
        host == null || host.trim().isEmpty ? null : host.trim();
    _runtimePortOverride =
        port == null || port.trim().isEmpty ? null : port.trim();
  }

  static Future<void> loadFromPreferences(LocalPreferences preferences) async {
    applyRuntimeOverride(
      host: preferences.apiHostOverride,
      port: preferences.apiPortOverride,
    );
  }

  /// API base including `/api/v1` prefix.
  ///
  /// Priority:
  /// 1. In-app override (debug screen)
  /// 2. `--dart-define=API_HOST=<LAN-IP>` (+ optional `API_PORT`)
  /// 3. Android emulator: `http://10.0.2.2:8080/api/v1`
  /// 4. iOS simulator / desktop: `http://localhost:8080/api/v1`
  static String get baseUrl {
    final host = _runtimeHostOverride ??
        (_host.isNotEmpty ? _host : null);
    final port = _runtimePortOverride ?? _port;
    if (host != null && host.isNotEmpty) {
      return 'http://$host:$port/api/v1';
    }
    if (Platform.isAndroid) {
      return 'http://10.0.2.2:8080/api/v1';
    }
    return 'http://localhost:8080/api/v1';
  }

  /// Origin for static media (`/uploads/public/...`).
  static String get mediaOrigin {
    final base = baseUrl;
    return base.replaceFirst(RegExp(r'/api/v1$'), '');
  }

  static const refreshPath = '/auth/refresh';
  static const connectTimeout = Duration(seconds: 15);
  static const receiveTimeout = Duration(seconds: 15);
  static const sendTimeout = Duration(seconds: 15);
}
