import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/nfc/nfc_payload_parser.dart';

void main() {
  const parser = NfcPayloadParser();

  test('bytesToHex formats tag identifier bytes', () {
    expect(parser.bytesToHexForTest([0x04, 0xA1, 0xB2, 0xC3]), '04a1b2c3');
  });
}
