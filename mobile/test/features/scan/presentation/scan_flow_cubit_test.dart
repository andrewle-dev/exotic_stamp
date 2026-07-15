import 'package:bloc_test/bloc_test.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/errors/failure.dart';
import 'package:metro_stamp_app/core/location/app_location_service.dart';
import 'package:metro_stamp_app/core/nfc/nfc_availability.dart';
import 'package:metro_stamp_app/core/nfc/nfc_reader.dart';
import 'package:metro_stamp_app/core/utils/idempotency_key_generator.dart';
import 'package:metro_stamp_app/features/scan/domain/entities/collect_stamp_result.dart';
import 'package:metro_stamp_app/features/scan/domain/entities/resolved_station.dart';
import 'package:metro_stamp_app/features/scan/domain/entities/scan_payload.dart';
import 'package:metro_stamp_app/features/scan/domain/entities/scan_type.dart';
import 'package:metro_stamp_app/features/scan/domain/repositories/scan_repository.dart';
import 'package:metro_stamp_app/features/scan/domain/entities/collect_status_outcome.dart';
import 'package:metro_stamp_app/features/scan/domain/entities/collect_status_result.dart';
import 'package:metro_stamp_app/features/scan/domain/usecases/check_collect_status_usecase.dart';
import 'package:metro_stamp_app/features/scan/domain/usecases/collect_stamp_usecase.dart';
import 'package:metro_stamp_app/features/scan/domain/usecases/resolve_scan_usecase.dart';
import 'package:metro_stamp_app/features/scan/presentation/cubit/scan_flow_cubit.dart';
import 'package:metro_stamp_app/features/scan/presentation/cubit/scan_flow_state.dart';
import 'package:mocktail/mocktail.dart';

class MockScanRepository extends Mock implements ScanRepository {}

class MockAppLocationService extends Mock implements AppLocationService {}

class MockNfcReader extends Mock implements NfcReader {}

class FakeIdempotencyKeyGenerator extends IdempotencyKeyGenerator {
  const FakeIdempotencyKeyGenerator(this.key);

  final String key;

  @override
  String generate() => key;
}

void main() {
  late MockScanRepository repository;
  late MockAppLocationService locationService;
  late MockNfcReader nfcReader;
  late ScanFlowCubit cubit;

  const payload = ScanPayload(scanType: ScanType.nfc, payload: 'tag-abc');
  const station = ResolvedStation(id: 'station-1', name: 'Ben Thanh');
  const gps = GpsReading(
    latitude: 10.77,
    longitude: 106.69,
    accuracyMeters: 12,
  );
  final collectResult = CollectStampResult(
    stamp: CollectedStamp(
      stampId: 'stamp-1',
      stationId: 'station-1',
      stationName: 'Ben Thanh',
      collectedAt: DateTime(2026, 6, 24),
    ),
    progress: const CollectStampProgress(
      lineId: 'line-1',
      collected: 3,
      total: 10,
      percentage: 30,
    ),
    isNew: true,
  );

  setUpAll(() {
    registerFallbackValue(payload);
  });

  setUp(() {
    repository = MockScanRepository();
    locationService = MockAppLocationService();
    nfcReader = MockNfcReader();

    when(() => nfcReader.checkAvailability())
        .thenAnswer((_) async => NfcAvailabilityStatus.enabled);

    cubit = ScanFlowCubit(
      resolveScanUseCase: ResolveScanUseCase(repository),
      collectStampUseCase: CollectStampUseCase(repository),
      checkCollectStatusUseCase: CheckCollectStatusUseCase(repository),
      locationService: locationService,
      idempotencyKeyGenerator: const FakeIdempotencyKeyGenerator('idem-1'),
      nfcReader: nfcReader,
    );
  });

  tearDown(() => cubit.close());

  blocTest<ScanFlowCubit, ScanFlowState>(
    'initialize moves to waitingForNfc when NFC enabled',
    build: () => cubit,
    act: (cubit) => cubit.initialize(),
    expect: () => [
      isA<ScanFlowState>().having(
        (s) => s.phase,
        'phase',
        ScanFlowPhase.checkingNfcAvailability,
      ),
      isA<ScanFlowState>().having(
        (s) => s.phase,
        'phase',
        ScanFlowPhase.waitingForNfc,
      ),
    ],
  );

  blocTest<ScanFlowCubit, ScanFlowState>(
    'nfc unavailable keeps NFC-first waiting without QR fallback when QR UI disabled',
    build: () {
      when(() => nfcReader.checkAvailability())
          .thenAnswer((_) async => NfcAvailabilityStatus.unavailable);
      return cubit;
    },
    act: (cubit) => cubit.initialize(),
    expect: () => [
      isA<ScanFlowState>().having(
        (s) => s.phase,
        'phase',
        ScanFlowPhase.checkingNfcAvailability,
      ),
      isA<ScanFlowState>()
          .having((s) => s.phase, 'phase', ScanFlowPhase.waitingForNfc)
          // ScanCapabilities.enableQrFlow defaults to false.
          .having((s) => s.qrFallbackAvailable, 'qrFallback', false),
    ],
  );

  blocTest<ScanFlowCubit, ScanFlowState>(
    'happy path resolves station and awaits collect confirmation',
    build: () {
      when(() => locationService.getCurrentReading())
          .thenAnswer((_) async => const LocationReadResult.success(gps));
      when(() => repository.resolveScan(any()))
          .thenAnswer((_) async => station);
      return cubit;
    },
    act: (cubit) => cubit.onNfcPayloadRead('tag-abc'),
    expect: () => [
      isA<ScanFlowState>().having(
        (s) => s.phase,
        'phase',
        ScanFlowPhase.readingNfc,
      ),
      isA<ScanFlowState>().having(
        (s) => s.phase,
        'phase',
        ScanFlowPhase.checkingLocation,
      ),
      isA<ScanFlowState>().having(
        (s) => s.phase,
        'phase',
        ScanFlowPhase.resolvingStation,
      ),
      isA<ScanFlowState>()
          .having((s) => s.awaitingCollectConfirmation, 'awaiting', true)
          .having((s) => s.resolvedStation?.id, 'station', 'station-1')
          .having((s) => s.idempotencyKey, 'idempotencyKey', 'idem-1'),
    ],
  );

  blocTest<ScanFlowCubit, ScanFlowState>(
    'collect success only after backend returns isNew true',
    build: () {
      when(() => locationService.getCurrentReading())
          .thenAnswer((_) async => const LocationReadResult.success(gps));
      when(() => repository.resolveScan(any()))
          .thenAnswer((_) async => station);
      when(
        () => repository.collectStamp(
          payload: any(named: 'payload'),
          latitude: any(named: 'latitude'),
          longitude: any(named: 'longitude'),
          accuracyMeters: any(named: 'accuracyMeters'),
          idempotencyKey: any(named: 'idempotencyKey'),
        ),
      ).thenAnswer((_) async => collectResult);
      return cubit;
    },
    act: (cubit) async {
      await cubit.onNfcPayloadRead('tag-abc');
      await cubit.confirmCollect();
    },
    verify: (_) {
      expect(cubit.state.phase, ScanFlowPhase.success);
      expect(cubit.state.collectResult?.isNew, isTrue);
    },
  );

  blocTest<ScanFlowCubit, ScanFlowState>(
    'isNew false maps to duplicate without success phase',
    build: () {
      when(() => locationService.getCurrentReading())
          .thenAnswer((_) async => const LocationReadResult.success(gps));
      when(() => repository.resolveScan(any()))
          .thenAnswer((_) async => station);
      when(
        () => repository.collectStamp(
          payload: any(named: 'payload'),
          latitude: any(named: 'latitude'),
          longitude: any(named: 'longitude'),
          accuracyMeters: any(named: 'accuracyMeters'),
          idempotencyKey: any(named: 'idempotencyKey'),
        ),
      ).thenAnswer(
        (_) async => CollectStampResult(
          stamp: collectResult.stamp,
          progress: collectResult.progress,
          isNew: false,
        ),
      );
      return cubit;
    },
    act: (cubit) async {
      await cubit.onNfcPayloadRead('tag-abc');
      await cubit.confirmCollect();
    },
    verify: (_) {
      expect(cubit.state.phase, ScanFlowPhase.duplicate);
      expect(cubit.state.phase, isNot(ScanFlowPhase.success));
    },
  );

  blocTest<ScanFlowCubit, ScanFlowState>(
    'resolve failure does not call collect',
    build: () {
      when(() => locationService.getCurrentReading())
          .thenAnswer((_) async => const LocationReadResult.success(gps));
      when(() => repository.resolveScan(any())).thenThrow(
        const Failure(
          code: FailureCode.nfcInvalid,
          message: 'Invalid tag',
        ),
      );
      return cubit;
    },
    act: (cubit) => cubit.onNfcPayloadRead('tag-abc'),
    verify: (_) {
      verifyNever(
        () => repository.collectStamp(
          payload: any(named: 'payload'),
          latitude: any(named: 'latitude'),
          longitude: any(named: 'longitude'),
          accuracyMeters: any(named: 'accuracyMeters'),
          idempotencyKey: any(named: 'idempotencyKey'),
        ),
      );
      expect(cubit.state.phase, ScanFlowPhase.invalidTag);
      expect(cubit.state.awaitingCollectConfirmation, isFalse);
    },
  );

  blocTest<ScanFlowCubit, ScanFlowState>(
    'gps permission denied does not call collect or resolve',
    build: () {
      when(() => locationService.getCurrentReading()).thenAnswer(
        (_) async => const LocationReadResult.failure(
          issue: LocationIssue.permissionDenied,
          message: 'Permission denied',
        ),
      );
      return cubit;
    },
    act: (cubit) => cubit.onNfcPayloadRead('tag-abc'),
    verify: (_) {
      verifyNever(() => repository.resolveScan(any()));
      verifyNever(
        () => repository.collectStamp(
          payload: any(named: 'payload'),
          latitude: any(named: 'latitude'),
          longitude: any(named: 'longitude'),
          accuracyMeters: any(named: 'accuracyMeters'),
          idempotencyKey: any(named: 'idempotencyKey'),
        ),
      );
      expect(cubit.state.phase, ScanFlowPhase.locationPermissionDenied);
    },
  );

  blocTest<ScanFlowCubit, ScanFlowState>(
    'location service disabled maps to locationServiceDisabled',
    build: () {
      when(() => locationService.getCurrentReading()).thenAnswer(
        (_) async => const LocationReadResult.failure(
          issue: LocationIssue.serviceDisabled,
          message: 'Service disabled',
        ),
      );
      return cubit;
    },
    act: (cubit) => cubit.onNfcPayloadRead('tag-abc'),
    verify: (_) {
      verifyNever(() => repository.resolveScan(any()));
      expect(cubit.state.phase, ScanFlowPhase.locationServiceDisabled);
    },
  );

  blocTest<ScanFlowCubit, ScanFlowState>(
    'low GPS accuracy maps to locationLowAccuracy without collect',
    build: () {
      when(() => locationService.getCurrentReading()).thenAnswer(
        (_) async => const LocationReadResult.failure(
          issue: LocationIssue.lowAccuracy,
          message: 'Low accuracy',
        ),
      );
      return cubit;
    },
    act: (cubit) => cubit.onNfcPayloadRead('tag-abc'),
    verify: (_) {
      verifyNever(() => repository.resolveScan(any()));
      expect(cubit.state.phase, ScanFlowPhase.locationLowAccuracy);
    },
  );

  blocTest<ScanFlowCubit, ScanFlowState>(
    'backend stamp duplicate failure maps to duplicate phase',
    build: () {
      when(() => locationService.getCurrentReading())
          .thenAnswer((_) async => const LocationReadResult.success(gps));
      when(() => repository.resolveScan(any()))
          .thenAnswer((_) async => station);
      when(
        () => repository.collectStamp(
          payload: any(named: 'payload'),
          latitude: any(named: 'latitude'),
          longitude: any(named: 'longitude'),
          accuracyMeters: any(named: 'accuracyMeters'),
          idempotencyKey: any(named: 'idempotencyKey'),
        ),
      ).thenThrow(
        const Failure(
          code: FailureCode.stampDuplicate,
          message: 'Already collected',
        ),
      );
      return cubit;
    },
    act: (cubit) async {
      await cubit.onNfcPayloadRead('tag-abc');
      await cubit.confirmCollect();
    },
    verify: (_) {
      expect(cubit.state.phase, ScanFlowPhase.duplicate);
    },
  );

  blocTest<ScanFlowCubit, ScanFlowState>(
    'gps outside range maps to gpsOutsideRange',
    build: () {
      when(() => locationService.getCurrentReading())
          .thenAnswer((_) async => const LocationReadResult.success(gps));
      when(() => repository.resolveScan(any()))
          .thenAnswer((_) async => station);
      when(
        () => repository.collectStamp(
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
        ),
      );
      return cubit;
    },
    act: (cubit) async {
      await cubit.onNfcPayloadRead('tag-abc');
      await cubit.confirmCollect();
    },
    verify: (_) {
      expect(cubit.state.phase, ScanFlowPhase.gpsOutsideRange);
    },
  );

  blocTest<ScanFlowCubit, ScanFlowState>(
    'collect network error is uncertain and not success',
    build: () {
      when(() => locationService.getCurrentReading())
          .thenAnswer((_) async => const LocationReadResult.success(gps));
      when(() => repository.resolveScan(any()))
          .thenAnswer((_) async => station);
      when(
        () => repository.collectStamp(
          payload: any(named: 'payload'),
          latitude: any(named: 'latitude'),
          longitude: any(named: 'longitude'),
          accuracyMeters: any(named: 'accuracyMeters'),
          idempotencyKey: any(named: 'idempotencyKey'),
        ),
      ).thenThrow(
        const Failure(
          code: FailureCode.networkError,
          message: 'Timeout',
        ),
      );
      return cubit;
    },
    act: (cubit) async {
      await cubit.onNfcPayloadRead('tag-abc');
      await cubit.confirmCollect();
    },
    verify: (_) {
      expect(cubit.state.phase, ScanFlowPhase.networkError);
      expect(cubit.state.phase, isNot(ScanFlowPhase.success));
      expect(cubit.state.isUncertainOutcome, isTrue);
      expect(cubit.state.collectResult, isNull);
    },
  );

  blocTest<ScanFlowCubit, ScanFlowState>(
    'checkCollectStatus SUCCESS maps to success without local stamp insert',
    build: () {
      when(
        () => repository.getCollectStatus(
            idempotencyKey: any(named: 'idempotencyKey')),
      ).thenAnswer(
        (_) async => CollectStatusResult(
          outcome: CollectStatusOutcome.success,
          collectResult: collectResult,
        ),
      );
      return cubit;
    },
    seed: () => const ScanFlowState(
      phase: ScanFlowPhase.networkError,
      idempotencyKey: 'idem-1',
      isUncertainOutcome: true,
    ),
    act: (cubit) => cubit.checkCollectStatus(),
    verify: (_) {
      verify(
        () => repository.getCollectStatus(idempotencyKey: 'idem-1'),
      ).called(1);
      expect(cubit.state.phase, ScanFlowPhase.success);
      expect(cubit.state.collectResult?.stamp.stampId, 'stamp-1');
      expect(cubit.state.isUncertainOutcome, isFalse);
    },
  );

  blocTest<ScanFlowCubit, ScanFlowState>(
    'checkCollectStatus DUPLICATE maps to duplicate phase',
    build: () {
      when(
        () => repository.getCollectStatus(
            idempotencyKey: any(named: 'idempotencyKey')),
      ).thenAnswer(
        (_) async => CollectStatusResult(
          outcome: CollectStatusOutcome.duplicate,
          collectResult: CollectStampResult(
            stamp: collectResult.stamp,
            progress: collectResult.progress,
            isNew: false,
          ),
        ),
      );
      return cubit;
    },
    seed: () => const ScanFlowState(
      phase: ScanFlowPhase.networkError,
      idempotencyKey: 'idem-1',
      isUncertainOutcome: true,
    ),
    act: (cubit) => cubit.checkCollectStatus(),
    verify: (_) {
      expect(cubit.state.phase, ScanFlowPhase.duplicate);
      expect(cubit.state.isUncertainOutcome, isFalse);
    },
  );

  blocTest<ScanFlowCubit, ScanFlowState>(
    'checkCollectStatus NOT_FOUND stays uncertain',
    build: () {
      when(
        () => repository.getCollectStatus(
            idempotencyKey: any(named: 'idempotencyKey')),
      ).thenAnswer(
        (_) async => const CollectStatusResult(
          outcome: CollectStatusOutcome.notFound,
        ),
      );
      return cubit;
    },
    seed: () => const ScanFlowState(
      phase: ScanFlowPhase.networkError,
      idempotencyKey: 'idem-1',
      isUncertainOutcome: true,
    ),
    act: (cubit) => cubit.checkCollectStatus(),
    verify: (_) {
      expect(cubit.state.phase, ScanFlowPhase.networkError);
      expect(cubit.state.isUncertainOutcome, isTrue);
      expect(cubit.state.collectResult, isNull);
    },
  );
}
