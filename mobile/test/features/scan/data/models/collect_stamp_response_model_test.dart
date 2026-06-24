import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/scan/data/models/collect_stamp_response_model.dart';

void main() {
  test('fromJson reads isNew field', () {
    final model = CollectStampResponseModel.fromJson({
      'stamp': {
        'stampId': 's1',
        'stationId': 'st1',
        'stationName': 'Test',
        'collectedAt': '2026-06-24T10:00:00',
      },
      'isNew': false,
    });

    expect(model.isNew, isFalse);
  });

  test('fromJson falls back to new alias when isNew absent', () {
    final model = CollectStampResponseModel.fromJson({
      'stamp': {
        'stampId': 's1',
        'stationId': 'st1',
        'stationName': 'Test',
        'collectedAt': '2026-06-24T10:00:00',
      },
      'new': false,
    });

    expect(model.isNew, isFalse);
  });
}
