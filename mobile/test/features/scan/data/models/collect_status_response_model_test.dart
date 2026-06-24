import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/scan/data/models/collect_status_response_model.dart';
import 'package:metro_stamp_app/features/scan/domain/entities/collect_status_outcome.dart';

void main() {
  test('fromJson maps SUCCESS with stamp and progress', () {
    final model = CollectStatusResponseModel.fromJson({
      'status': 'SUCCESS',
      'stamp': {
        'stampId': 'stamp-1',
        'stationId': 'station-1',
        'stationName': 'Ben Thanh',
        'collectedAt': '2026-06-24T10:05:00',
      },
      'progress': {
        'lineId': 'line-1',
        'collected': 3,
        'total': 10,
        'percentage': 30,
      },
    });

    final entity = model.toEntity();

    expect(model.status, CollectStatusOutcome.success);
    expect(entity.outcome, CollectStatusOutcome.success);
    expect(entity.collectResult?.isNew, isTrue);
    expect(entity.collectResult?.stamp.stationName, 'Ben Thanh');
  });

  test('fromJson maps DUPLICATE with isNew false', () {
    final model = CollectStatusResponseModel.fromJson({
      'status': 'DUPLICATE',
      'stamp': {
        'stampId': 'stamp-1',
        'stationId': 'station-1',
        'stationName': 'Ben Thanh',
        'collectedAt': '2026-06-24T10:05:00',
      },
    });

    final entity = model.toEntity();

    expect(entity.outcome, CollectStatusOutcome.duplicate);
    expect(entity.collectResult?.isNew, isFalse);
  });

  test('fromJson maps NOT_FOUND without collect result', () {
    final model = CollectStatusResponseModel.fromJson({
      'status': 'NOT_FOUND',
    });

    final entity = model.toEntity();

    expect(entity.outcome, CollectStatusOutcome.notFound);
    expect(entity.collectResult, isNull);
    expect(entity.isStillUncertain, isTrue);
  });
}
