import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/errors/failure.dart';
import 'package:metro_stamp_app/features/scan/data/datasources/scan_remote_datasource.dart';
import 'package:metro_stamp_app/features/scan/data/models/collect_stamp_response_model.dart';
import 'package:metro_stamp_app/features/scan/data/models/collect_status_response_model.dart';
import 'package:metro_stamp_app/features/scan/data/models/scan_resolve_response_model.dart';
import 'package:metro_stamp_app/features/scan/domain/entities/collect_status_outcome.dart';
import 'package:metro_stamp_app/features/scan/data/repositories/scan_repository_impl.dart';
import 'package:metro_stamp_app/features/scan/domain/entities/scan_payload.dart';
import 'package:metro_stamp_app/features/scan/domain/entities/scan_type.dart';
import 'package:mocktail/mocktail.dart';

class MockScanRemoteDataSource extends Mock implements ScanRemoteDataSource {}

void main() {
  late MockScanRemoteDataSource remoteDataSource;
  late ScanRepositoryImpl repository;

  setUpAll(() {
    registerFallbackValue(
      const ScanPayload(scanType: ScanType.nfc, payload: 'fallback'),
    );
  });

  setUp(() {
    remoteDataSource = MockScanRemoteDataSource();
    repository = ScanRepositoryImpl(remoteDataSource: remoteDataSource);
  });

  test('maps resolve scan response to entity', () async {
    when(() => remoteDataSource.resolveScan(any())).thenAnswer(
      (_) async => ScanResolveResponseModel(
        station: ResolvedStationModel(
          id: 'station-1',
          name: 'Ben Thanh',
          lineName: 'Line 1',
        ),
        resolved: true,
        scanType: ScanType.nfc,
      ),
    );

    final station = await repository.resolveScan(
      const ScanPayload(scanType: ScanType.nfc, payload: 'tag-1'),
    );

    expect(station.id, 'station-1');
    expect(station.name, 'Ben Thanh');
  });

  test('maps collect success response to entity', () async {
    when(
      () => remoteDataSource.collectStamp(
        payload: any(named: 'payload'),
        latitude: any(named: 'latitude'),
        longitude: any(named: 'longitude'),
        accuracyMeters: any(named: 'accuracyMeters'),
        idempotencyKey: any(named: 'idempotencyKey'),
      ),
    ).thenAnswer(
      (_) async => CollectStampResponseModel(
        stamp: CollectedStampModel(
          stampId: 'stamp-1',
          stationId: 'station-1',
          stationName: 'Ben Thanh',
          collectedAt: DateTime(2026, 6, 24),
        ),
        progress: CollectStampProgressModel(
          lineId: 'line-1',
          collected: 3,
          total: 10,
          percentage: 30,
        ),
        isNew: true,
      ),
    );

    final result = await repository.collectStamp(
      payload: const ScanPayload(scanType: ScanType.nfc, payload: 'tag-1'),
      latitude: 10.77,
      longitude: 106.69,
      accuracyMeters: 12,
      idempotencyKey: 'key-1',
    );

    expect(result.isNew, isTrue);
    expect(result.stamp.stationName, 'Ben Thanh');
    expect(result.progress?.collected, 3);
  });

  test('propagates mapped backend failure on resolve', () async {
    when(() => remoteDataSource.resolveScan(any())).thenThrow(
      const Failure(
        code: FailureCode.nfcInvalid,
        message: 'Invalid tag',
        backendCode: 'SCAN_KEY_NOT_FOUND',
      ),
    );

    expect(
      () => repository.resolveScan(
        const ScanPayload(scanType: ScanType.nfc, payload: 'bad'),
      ),
      throwsA(
        isA<Failure>().having((f) => f.code, 'code', FailureCode.nfcInvalid),
      ),
    );
  });

  test('propagates mapped backend failure on collect', () async {
    when(
      () => remoteDataSource.collectStamp(
        payload: any(named: 'payload'),
        latitude: any(named: 'latitude'),
        longitude: any(named: 'longitude'),
        accuracyMeters: any(named: 'accuracyMeters'),
        idempotencyKey: any(named: 'idempotencyKey'),
      ),
    ).thenThrow(
      const Failure(
        code: FailureCode.gpsOutsideRange,
        message: 'Outside range',
        backendCode: 'GPS_OUT_OF_RANGE',
      ),
    );

    expect(
      () => repository.collectStamp(
        payload: const ScanPayload(scanType: ScanType.nfc, payload: 'tag-1'),
        latitude: 10.77,
        longitude: 106.69,
        accuracyMeters: 12,
        idempotencyKey: 'key-1',
      ),
      throwsA(
        isA<Failure>().having(
          (f) => f.code,
          'code',
          FailureCode.gpsOutsideRange,
        ),
      ),
    );
  });

  test('maps getCollectStatus response to entity', () async {
    when(
      () => remoteDataSource.getCollectStatus(
        idempotencyKey: any(named: 'idempotencyKey'),
      ),
    ).thenAnswer(
      (_) async => CollectStatusResponseModel(
        status: CollectStatusOutcome.success,
        stamp: CollectedStampModel(
          stampId: 'stamp-1',
          stationId: 'station-1',
          stationName: 'Ben Thanh',
          collectedAt: DateTime(2026, 6, 24),
        ),
      ),
    );

    final result = await repository.getCollectStatus(idempotencyKey: 'key-1');

    expect(result.outcome, CollectStatusOutcome.success);
    expect(result.collectResult?.stamp.stationName, 'Ben Thanh');
  });
}
