import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:uuid/uuid.dart';

/// Access token stays in memory; refresh + device id use platform secure storage.
class SecureTokenStorage {
  SecureTokenStorage({FlutterSecureStorage? storage})
      : _storage = storage ??
            const FlutterSecureStorage(
              aOptions: AndroidOptions(encryptedSharedPreferences: true),
              iOptions: IOSOptions(
                accessibility: KeychainAccessibility.first_unlock_this_device,
              ),
            );

  final FlutterSecureStorage _storage;

  static const _refreshTokenKey = 'refresh_token';
  static const _deviceIdKey = 'device_id';

  String? _accessToken;

  String? readAccessToken() => _accessToken;

  Future<bool> hasAccessToken() async =>
      _accessToken != null && _accessToken!.isNotEmpty;

  Future<void> writeAccessToken(String token) async {
    _accessToken = token;
  }

  Future<String?> readRefreshToken() => _storage.read(key: _refreshTokenKey);

  Future<void> writeRefreshToken(String token) async {
    await _storage.write(key: _refreshTokenKey, value: token);
  }

  Future<String> getOrCreateDeviceId() async {
    final existing = await _storage.read(key: _deviceIdKey);
    if (existing != null && existing.isNotEmpty) {
      return existing;
    }
    final created = const Uuid().v4();
    await _storage.write(key: _deviceIdKey, value: created);
    return created;
  }

  Future<void> clearSessionTokens() async {
    _accessToken = null;
    await _storage.delete(key: _refreshTokenKey);
  }

  /// Clears access + refresh. Device id is retained across logout.
  Future<void> clear() => clearSessionTokens();
}
