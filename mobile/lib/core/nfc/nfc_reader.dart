import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:nfc_manager/nfc_manager.dart';

import 'nfc_availability.dart';
import 'nfc_payload_parser.dart';

typedef NfcPayloadHandler = Future<void> Function(String payload);

/// Thin wrapper around [NfcManager] with safe session lifecycle.
class NfcReader {
  NfcReader({
    NfcPayloadParser? payloadParser,
    bool Function()? iosTestBuildDisabled,
    bool Function()? nfcSupported,
  })  : _payloadParser = payloadParser ?? const NfcPayloadParser(),
        _iosTestBuildDisabled = iosTestBuildDisabled ??
            (() => defaultTargetPlatform == TargetPlatform.iOS),
        _nfcSupported = nfcSupported ?? _defaultNfcSupported;

  final NfcPayloadParser _payloadParser;
  final bool Function() _iosTestBuildDisabled;
  final bool Function() _nfcSupported;

  static bool _defaultNfcSupported() =>
      !kIsWeb && (Platform.isAndroid || Platform.isIOS);

  bool _sessionRunning = false;

  bool get isSessionRunning => _sessionRunning;

  Future<NfcAvailabilityStatus> checkAvailability() async {
    if (!_nfcSupported()) {
      return NfcAvailabilityStatus.unavailable;
    }

    if (_iosTestBuildDisabled()) {
      return NfcAvailabilityStatus.iosTestBuildDisabled;
    }

    final availability = await NfcManager.instance.checkAvailability();
    switch (availability) {
      case NfcAvailability.enabled:
        return NfcAvailabilityStatus.enabled;
      case NfcAvailability.disabled:
        return NfcAvailabilityStatus.disabled;
      case NfcAvailability.unsupported:
        return NfcAvailabilityStatus.unavailable;
    }
  }

  Future<void> startSession({
    required NfcPayloadHandler onPayload,
    String iosAlertMessage = 'Đưa điện thoại lại gần thẻ NFC để quét stamp.',
  }) async {
    if (!_nfcSupported() || _sessionRunning) {
      return;
    }

    _sessionRunning = true;
    try {
      await NfcManager.instance.startSession(
        pollingOptions: {
          NfcPollingOption.iso14443,
          NfcPollingOption.iso15693,
          NfcPollingOption.iso18092,
        },
        alertMessageIos: iosAlertMessage,
        onDiscovered: (tag) async {
          final payload = _payloadParser.parse(tag);
          if (payload == null || payload.isEmpty) {
            return;
          }

          await onPayload(payload);
          await NfcManager.instance.stopSession(
            alertMessageIos: 'Đã đọc thẻ NFC thành công.',
          );
        },
      );
    } catch (_) {
      _sessionRunning = false;
      rethrow;
    }
  }

  Future<void> stopSession() async {
    if (!_sessionRunning || !_nfcSupported()) {
      return;
    }

    try {
      await NfcManager.instance.stopSession();
    } catch (_) {
      // Ignore shutdown errors when leaving the screen.
    } finally {
      _sessionRunning = false;
    }
  }
}
