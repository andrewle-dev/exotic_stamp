import 'dart:io';

/// API and environment configuration.
class ApiConfig {
  const ApiConfig._();

  static const appName = 'Metro Stamp';

  static const _host = String.fromEnvironment('API_HOST', defaultValue: '');
  static const _port = String.fromEnvironment('API_PORT', defaultValue: '8080');

  /// API base including `/api/v1` prefix.
  ///
  /// - Android emulator default: `http://10.0.2.2:8080/api/v1`
  /// - iOS simulator / desktop: `http://localhost:8080/api/v1`
  /// - Physical device: `--dart-define=API_HOST=<LAN-IP>` (same port via `API_PORT`)
  static String get baseUrl {
    if (_host.isNotEmpty) {
      return 'http://$_host:$_port/api/v1';
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
