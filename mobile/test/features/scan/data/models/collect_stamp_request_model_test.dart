import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/scan/data/models/collect_stamp_request_model.dart';
import 'package:metro_stamp_app/features/scan/domain/entities/scan_type.dart';

void main() {
  test('toJson maps all openapi collect fields including idempotencyKey', () {
    final model = CollectStampRequestModel(
      scanType: ScanType.nfc,
      payload: 'tag-abc',
      latitude: 10.7721,
      longitude: 106.6983,
      accuracyMeters: 12.5,
      devicePlatform: 'android',
      appVersion: '0.1.0',
      idempotencyKey: '550e8400-e29b-41d4-a716-446655440099',
    );

    expect(
      model.toJson(),
      {
        'scanType': 'NFC',
        'payload': 'tag-abc',
        'latitude': 10.7721,
        'longitude': 106.6983,
        'accuracyMeters': 12.5,
        'devicePlatform': 'android',
        'appVersion': '0.1.0',
        'idempotencyKey': '550e8400-e29b-41d4-a716-446655440099',
      },
    );
  });

  test('qr scan type serializes as QR_STATIC per openapi', () {
    final model = CollectStampRequestModel(
      scanType: ScanType.qr,
      payload: 'qr-token',
      latitude: 10.77,
      longitude: 106.69,
      accuracyMeters: 10,
      devicePlatform: 'ios',
      appVersion: '0.1.0',
      idempotencyKey: 'key-1',
    );

    expect(model.toJson()['scanType'], 'QR_STATIC');
  });
}
