import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/mock/mock_data_store.dart';
import 'package:metro_stamp_app/core/mock/repositories/mock_scan_repository.dart';
import 'package:metro_stamp_app/features/scan/domain/entities/scan_payload.dart';
import 'package:metro_stamp_app/features/scan/domain/entities/scan_type.dart';

void main() {
  setUp(MockDataStore.instance.reset);

  test('mock collect updates store when allowed in debug', () async {
    final repo = MockScanRepository(allowWritesOverride: true);
    final before = MockDataStore.instance.collectedCount;

    final result = await repo.collectStamp(
      payload: const ScanPayload(
        scanType: ScanType.nfc,
        payload: 'station-thao-dien',
      ),
      latitude: 10.78,
      longitude: 106.70,
      accuracyMeters: 10,
      idempotencyKey: 'test-key',
    );

    expect(result.isNew, isTrue);
    expect(MockDataStore.instance.collectedCount, before + 1);
    expect(result.stamp.stationId, 'station-thao-dien');
  });
}
