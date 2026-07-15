import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/home/data/models/home_summary_model.dart';

void main() {
  test('parses fourteen of fourteen stamps', () {
    final model = CollectionProgressModel.fromJson({
      'lineId': 'line-1',
      'collected': 14,
      'total': 14,
      'percentage': 100,
    });

    expect(model.collected, 14);
    expect(model.total, 14);
    expect(model.percentage, 100);
  });

  test('parses zero of fourteen stamps', () {
    final model = CollectionProgressModel.fromJson({
      'lineId': 'line-1',
      'collected': 0,
      'total': 14,
      'percentage': 0,
    });

    expect(model.collected, 0);
    expect(model.total, 14);
  });

  test('missing collected throws instead of defaulting to zero', () {
    expect(
      () => CollectionProgressModel.fromJson({
        'lineId': 'line-1',
        'total': 14,
        'percentage': 0,
      }),
      throwsA(isA<FormatException>()),
    );
  });

  test('wrong field names collectedCount/totalStations do not become 0/0', () {
    expect(
      () => CollectionProgressModel.fromJson({
        'lineId': 'line-1',
        'collectedCount': 14,
        'totalStations': 14,
        'percentage': 100,
      }),
      throwsA(isA<FormatException>()),
    );
  });

  test('null collected throws', () {
    expect(
      () => CollectionProgressModel.fromJson({
        'lineId': 'line-1',
        'collected': null,
        'total': 14,
      }),
      throwsA(isA<FormatException>()),
    );
  });
}
