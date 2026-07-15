import '../../../core/config/mock_config.dart';
import '../../../core/errors/failure.dart';
import '../../../features/scan/domain/entities/collect_stamp_result.dart';
import '../../../features/scan/domain/entities/collect_status_outcome.dart';
import '../../../features/scan/domain/entities/collect_status_result.dart';
import '../../../features/scan/domain/entities/resolved_station.dart';
import '../../../features/scan/domain/entities/scan_payload.dart';
import '../../../features/scan/domain/repositories/scan_repository.dart';
import '../mock_data_store.dart';
import '../mock_fixtures.dart';

/// Mock [ScanRepository] — UI development only.
///
/// Collect success is simulated only when [MockConfig.allowMockWrites] is true.
/// Special payloads:
/// - `MOCK:ERROR:DUPLICATE` — duplicate stamp
/// - `MOCK:ERROR:GPS` — GPS outside range
class MockScanRepository implements ScanRepository {
  MockScanRepository({
    MockDataStore? store,
    this.allowWritesOverride,
  }) : _store = store ?? MockDataStore.instance;

  final MockDataStore _store;

  /// Test-only override for write simulation without compile-time flag.
  final bool? allowWritesOverride;

  bool get _allowWrites => allowWritesOverride ?? MockConfig.allowMockWrites;

  static const _duplicatePayload = 'MOCK:ERROR:DUPLICATE';
  static const _gpsPayload = 'MOCK:ERROR:GPS';

  @override
  Future<ResolvedStation> resolveScan(ScanPayload payload) async {
    await Future<void>.delayed(const Duration(milliseconds: 250));

    if (payload.payload == _gpsPayload) {
      throw const Failure(
        code: FailureCode.gpsOutsideRange,
        message: 'Ngoài phạm vi ga (mock).',
      );
    }

    final stationId = _resolveStationId(payload.payload);
    final entry = MockFixtures.stationCatalog().firstWhere(
      (s) => s.id == stationId,
      orElse: () => MockFixtures.stationCatalog().first,
    );

    return ResolvedStation(
      id: entry.id,
      name: entry.name,
      lineName: MockFixtures.lineName,
      latitude: 10.77 + entry.sequence * 0.01,
      longitude: 106.69 + entry.sequence * 0.005,
      zoneRadiusMeters: 120,
    );
  }

  @override
  Future<CollectStampResult> collectStamp({
    required ScanPayload payload,
    required double latitude,
    required double longitude,
    required double accuracyMeters,
    required String idempotencyKey,
  }) async {
    if (!_allowWrites) {
      throw const Failure(
        code: FailureCode.unknown,
        message: 'Mock collect is disabled outside mock mode.',
      );
    }

    await Future<void>.delayed(const Duration(milliseconds: 400));

    if (payload.payload == _duplicatePayload) {
      throw const Failure(
        code: FailureCode.stampDuplicate,
        message: 'Stamp đã được thu (mock).',
      );
    }

    if (payload.payload == _gpsPayload) {
      throw const Failure(
        code: FailureCode.gpsOutsideRange,
        message: 'Ngoài phạm vi ga (mock).',
      );
    }

    final stationId = _resolveStationId(payload.payload);
    final entry = MockFixtures.stationCatalog().firstWhere(
      (s) => s.id == stationId,
      orElse: () => MockFixtures.stationCatalog().first,
    );

    final isNew = _store.markStationCollected(stationId);
    final book = MockFixtures.stampBook(_store.collectedStationIds);
    final progress = book.progress!;

    return CollectStampResult(
      isNew: isNew,
      stamp: CollectedStamp(
        stampId: 'stamp-$stationId',
        stationId: stationId,
        stationName: entry.name,
        lineName: MockFixtures.lineName,
        lineId: MockFixtures.lineId,
        campaignId: MockFixtures.campaignId,
        collectedAt: DateTime.now(),
      ),
      progress: CollectStampProgress(
        lineId: progress.lineId,
        collected: progress.collected,
        total: progress.total,
        percentage: progress.percentage,
      ),
      nextRewardHint: progress.collected >= progress.total - 1
          ? 'Bạn sắp mở khóa phần thưởng!'
          : 'Còn ${progress.total - progress.collected} ga nữa để nhận thưởng.',
      sponsorAd: payload.payload == 'MOCK:AD'
          ? const CollectSponsorAd(
              title: 'Metro Coffee',
              subtitle: 'Giảm 10% khi quét stamp hôm nay',
            )
          : null,
    );
  }

  @override
  Future<CollectStatusResult> getCollectStatus({
    required String idempotencyKey,
  }) async {
    await Future<void>.delayed(const Duration(milliseconds: 200));
    return const CollectStatusResult(outcome: CollectStatusOutcome.notFound);
  }

  String _resolveStationId(String payload) {
    final trimmed = payload.trim();
    if (trimmed.startsWith('station-')) {
      return trimmed;
    }
    if (trimmed.startsWith('MOCK:STATION:')) {
      return trimmed.substring('MOCK:STATION:'.length);
    }
    return MockFixtures.stationCatalog().first.id;
  }
}

/// Presentation-friendly scan error for mock/UI tests.
class MockScanError {
  const MockScanError({
    required this.code,
    required this.title,
    required this.message,
  });

  final String code;
  final String title;
  final String message;

  static MockScanError duplicate() => const MockScanError(
        code: 'STAMP_DUPLICATE',
        title: 'Stamp đã được thu',
        message: 'Bạn đã thu stamp tại ga này rồi.',
      );

  static MockScanError gpsOutsideRange() => const MockScanError(
        code: 'GPS_OUTSIDE_RANGE',
        title: 'Ngoài phạm vi ga',
        message: 'Hãy đến gần ga hơn và thử lại.',
      );
}
