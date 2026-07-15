import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/nfc/nfc_payload_parser.dart';
import 'package:ndef_record/ndef_record.dart';

void main() {
  const parser = NfcPayloadParser();

  test('bytesToHex formats tag identifier bytes', () {
    expect(parser.bytesToHexForTest([0x04, 0xA1, 0xB2, 0xC3]), '04a1b2c3');
  });

  test('decodeNdefRecord decodes URI well-known record', () {
    final record = NdefRecord(
      typeNameFormat: TypeNameFormat.wellKnown,
      type: Uint8List.fromList(utf8.encode('U')),
      identifier: Uint8List(0),
      payload: Uint8List.fromList([
        0x00,
        ...utf8.encode('metrostamp://scan?k=nfc_test_home_001'),
      ]),
    );
    expect(
      parser.decodeNdefRecord(record),
      'metrostamp://scan?k=nfc_test_home_001',
    );
  });

  test('decodeNdefRecord decodes text well-known record', () {
    final language = utf8.encode('en');
    final record = NdefRecord(
      typeNameFormat: TypeNameFormat.wellKnown,
      type: Uint8List.fromList(utf8.encode('T')),
      identifier: Uint8List(0),
      payload: Uint8List.fromList([
        language.length,
        ...language,
        ...utf8.encode('nfc_test_home_001'),
      ]),
    );
    expect(parser.decodeNdefRecord(record), 'nfc_test_home_001');
  });
}
