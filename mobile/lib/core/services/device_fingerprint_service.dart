import 'package:uuid/uuid.dart';

import '../storage/secure_token_storage.dart';

/// App-generated device id for session metadata (not an auth factor).
class DeviceFingerprintService {
  DeviceFingerprintService({SecureTokenStorage? tokenStorage})
      : _tokenStorage = tokenStorage ?? SecureTokenStorage();

  final SecureTokenStorage _tokenStorage;
  String? _cached;

  Future<String> getFingerprint() async {
    if (_cached != null) {
      return _cached!;
    }
    _cached = await _tokenStorage.getOrCreateDeviceId();
    return _cached!;
  }

  /// Testing helper — generates a non-persisted id.
  static String generateEphemeral() => const Uuid().v4();
}
