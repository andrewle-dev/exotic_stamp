import 'dart:convert';

import 'package:flutter/foundation.dart';

import 'package:ndef_record/ndef_record.dart';
import 'package:nfc_manager/nfc_manager.dart';
import 'package:nfc_manager/nfc_manager_android.dart';
import 'package:nfc_manager/nfc_manager_ios.dart';

/// Extracts backend scan payload from an NFC tag.
class NfcPayloadParser {
  const NfcPayloadParser();

  String? parse(NfcTag tag) {
    final ndefPayload = _readNdefPayload(tag);
    if (ndefPayload != null && ndefPayload.isNotEmpty) {
      return ndefPayload;
    }

    final identifier = _readTagIdentifier(tag);
    if (identifier != null && identifier.isNotEmpty) {
      return identifier;
    }

    return null;
  }

  String? parseMessage(NdefMessage message) {
    final buffer = StringBuffer();
    for (final record in message.records) {
      final decoded = decodeNdefRecord(record);
      if (decoded != null && decoded.isNotEmpty) {
        if (buffer.isNotEmpty) {
          buffer.write('|');
        }
        buffer.write(decoded);
      }
    }
    final value = buffer.toString().trim();
    return value.isEmpty ? null : value;
  }

  String? _readNdefPayload(NfcTag tag) {
    final androidNdef = NdefAndroid.from(tag);
    final iosNdef = NdefIos.from(tag);
    final message =
        androidNdef?.cachedNdefMessage ?? iosNdef?.cachedNdefMessage;
    if (message == null) {
      return null;
    }
    return parseMessage(message);
  }

  @visibleForTesting
  String? decodeNdefRecord(NdefRecord record) {
    final payload = record.payload;
    if (payload.isEmpty) {
      return null;
    }

    if (record.typeNameFormat == TypeNameFormat.absoluteUri) {
      return utf8.decode(payload, allowMalformed: true).trim();
    }

    if (record.typeNameFormat == TypeNameFormat.wellKnown) {
      final type = utf8.decode(record.type, allowMalformed: true);
      if (type == 'U') {
        return _decodeUriRecord(payload);
      }
      if (type == 'T') {
        return _decodeTextRecord(payload);
      }
    }

    return utf8.decode(payload, allowMalformed: true).trim();
  }

  String? _decodeUriRecord(Uint8List payload) {
    if (payload.isEmpty) {
      return null;
    }
    // First byte is URI identifier code; 0x00 = no prefix (full URI follows).
    final body = utf8.decode(payload.sublist(1), allowMalformed: true).trim();
    return body.isEmpty ? null : body;
  }

  String? _decodeTextRecord(Uint8List payload) {
    final languageCodeLength = payload.first & 0x3F;
    if (payload.length <= languageCodeLength + 1) {
      return null;
    }
    return utf8
        .decode(payload.sublist(languageCodeLength + 1), allowMalformed: true)
        .trim();
  }

  String? _readTagIdentifier(NfcTag tag) {
    final androidTag = NfcTagAndroid.from(tag);
    if (androidTag != null) {
      return _bytesToHex(androidTag.id);
    }

    final iosTag = MiFareIos.from(tag);
    if (iosTag != null) {
      return _bytesToHex(iosTag.identifier);
    }

    return null;
  }

  String _bytesToHex(List<int> bytes) {
    return bytes.map((byte) => byte.toRadixString(16).padLeft(2, '0')).join();
  }

  @visibleForTesting
  String bytesToHexForTest(List<int> bytes) => _bytesToHex(bytes);
}
