import 'dart:io';

import 'package:device_info_plus/device_info_plus.dart';

class DeviceFingerprintService {
  DeviceFingerprintService({DeviceInfoPlugin? deviceInfo})
      : _deviceInfo = deviceInfo ?? DeviceInfoPlugin();

  final DeviceInfoPlugin _deviceInfo;
  String? _cachedFingerprint;

  Future<String> getFingerprint() async {
    if (_cachedFingerprint != null) {
      return _cachedFingerprint!;
    }

    if (Platform.isAndroid) {
      final info = await _deviceInfo.androidInfo;
      _cachedFingerprint = info.id.isNotEmpty
          ? info.id
          : '${info.brand}-${info.model}-${info.device}';
    } else if (Platform.isIOS) {
      final info = await _deviceInfo.iosInfo;
      _cachedFingerprint = info.identifierForVendor ?? 'ios-${info.model}';
    } else {
      _cachedFingerprint = 'unknown-device';
    }

    return _cachedFingerprint!;
  }
}
