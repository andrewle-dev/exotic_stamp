import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/nfc/nfc_availability.dart';
import 'package:metro_stamp_app/core/nfc/nfc_reader.dart';

void main() {
  test('isSessionRunning is false before any session starts', () {
    final reader = NfcReader(iosTestBuildDisabled: () => true);

    expect(reader.isSessionRunning, isFalse);
  });

  test('ios test build reports disabled without touching platform NFC',
      () async {
    final reader = NfcReader(iosTestBuildDisabled: () => true);

    final status = await reader.checkAvailability();

    expect(status, NfcAvailabilityStatus.iosTestBuildDisabled);
    expect(reader.isSessionRunning, isFalse);
  });
}
