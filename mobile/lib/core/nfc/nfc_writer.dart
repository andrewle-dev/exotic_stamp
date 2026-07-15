import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:flutter/foundation.dart';

import 'package:ndef_record/ndef_record.dart';
import 'package:nfc_manager/nfc_manager.dart';
import 'package:nfc_manager/nfc_manager_android.dart';
import 'package:nfc_manager/nfc_manager_ios.dart';

import 'nfc_availability.dart';
import 'nfc_payload_parser.dart';

class NfcWriteResult {
  const NfcWriteResult({
    required this.writtenPayload,
    required this.readBackPayload,
    required this.matches,
  });

  final String writtenPayload;
  final String readBackPayload;
  final bool matches;
}

/// Writes NDEF URI (or text) payloads and verifies read-back.
class NfcWriter {
  NfcWriter({
    NfcPayloadParser? payloadParser,
    bool Function()? nfcSupported,
  })  : _payloadParser = payloadParser ?? const NfcPayloadParser(),
        _nfcSupported = nfcSupported ?? _defaultNfcSupported;

  final NfcPayloadParser _payloadParser;
  final bool Function() _nfcSupported;

  static bool _defaultNfcSupported() =>
      !kIsWeb && (Platform.isAndroid || Platform.isIOS);

  Future<NfcAvailabilityStatus> checkAvailability() async {
    if (!_nfcSupported()) {
      return NfcAvailabilityStatus.unavailable;
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

  /// Writes [payload] as NDEF URI when it contains `://`, otherwise as text.
  /// Reads the tag back and compares payloads.
  Future<NfcWriteResult> writeAndVerify({
    required String payload,
    String iosAlertMessage = 'Hold phone near the NFC tag to write.',
  }) async {
    final trimmed = payload.trim();
    if (trimmed.isEmpty) {
      throw StateError('payload must not be blank');
    }
    if (!_nfcSupported()) {
      throw StateError('NFC is not supported on this device');
    }

    final message = NdefMessage(records: [_buildRecord(trimmed)]);
    final completer = Completer<NfcWriteResult>();

    await NfcManager.instance.startSession(
      pollingOptions: {
        NfcPollingOption.iso14443,
        NfcPollingOption.iso15693,
        NfcPollingOption.iso18092,
      },
      alertMessageIos: iosAlertMessage,
      onDiscovered: (tag) async {
        if (completer.isCompleted) {
          return;
        }
        try {
          await _writeMessage(tag, message);
          final readBack = await _readBack(tag) ?? '';
          final matches = _payloadsMatch(trimmed, readBack);
          await NfcManager.instance.stopSession(
            alertMessageIos: matches
                ? 'NFC tag written and verified.'
                : 'NFC tag written but read-back mismatch.',
          );
          completer.complete(
            NfcWriteResult(
              writtenPayload: trimmed,
              readBackPayload: readBack,
              matches: matches,
            ),
          );
        } catch (error, stackTrace) {
          try {
            await NfcManager.instance.stopSession(
              errorMessageIos: 'Failed to write NFC tag.',
            );
          } catch (_) {}
          completer.completeError(error, stackTrace);
        }
      },
    );

    return completer.future;
  }

  Future<void> _writeMessage(NfcTag tag, NdefMessage message) async {
    final android = NdefAndroid.from(tag);
    if (android != null) {
      if (!android.isWritable) {
        throw StateError('NFC tag is not writable');
      }
      await android.writeNdefMessage(message);
      return;
    }

    final ios = NdefIos.from(tag);
    if (ios != null) {
      if (ios.status != NdefStatusIos.readWrite) {
        throw StateError('NFC tag is not writable on iOS');
      }
      await ios.writeNdef(message);
      return;
    }

    throw StateError('Tag does not support NDEF');
  }

  Future<String?> _readBack(NfcTag tag) async {
    final android = NdefAndroid.from(tag);
    if (android != null) {
      final message = await android.getNdefMessage();
      if (message != null) {
        return _payloadParser.parseMessage(message);
      }
    }

    final ios = NdefIos.from(tag);
    if (ios != null) {
      final message = await ios.readNdef();
      if (message != null) {
        return _payloadParser.parseMessage(message);
      }
    }

    return _payloadParser.parse(tag);
  }

  NdefRecord _buildRecord(String payload) {
    if (payload.contains('://')) {
      return NdefRecord(
        typeNameFormat: TypeNameFormat.wellKnown,
        type: Uint8List.fromList(utf8.encode('U')),
        identifier: Uint8List(0),
        // 0x00 = no URI prefix; full URI follows.
        payload: Uint8List.fromList([0x00, ...utf8.encode(payload)]),
      );
    }

    final language = utf8.encode('en');
    return NdefRecord(
      typeNameFormat: TypeNameFormat.wellKnown,
      type: Uint8List.fromList(utf8.encode('T')),
      identifier: Uint8List(0),
      payload: Uint8List.fromList([
        language.length,
        ...language,
        ...utf8.encode(payload),
      ]),
    );
  }

  bool _payloadsMatch(String expected, String actual) {
    final a = expected.trim();
    final b = actual.trim();
    if (a == b) {
      return true;
    }
    final aKey = _extractKey(a);
    final bKey = _extractKey(b);
    return aKey != null && bKey != null && aKey == bKey;
  }

  String? _extractKey(String value) {
    final uriIndex = value.indexOf('k=');
    if (uriIndex >= 0 && uriIndex + 2 < value.length) {
      return value.substring(uriIndex + 2).trim();
    }
    return value.trim();
  }
}
