import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:go_router/go_router.dart';
import 'package:metro_stamp_app/app/router/route_names.dart';
import 'package:metro_stamp_app/core/location/app_location_service.dart';
import 'package:metro_stamp_app/core/nfc/nfc_availability.dart';
import 'package:metro_stamp_app/core/nfc/nfc_reader.dart';
import 'package:metro_stamp_app/core/utils/idempotency_key_generator.dart';
import 'package:metro_stamp_app/features/scan/domain/repositories/scan_repository.dart';
import 'package:metro_stamp_app/features/scan/domain/usecases/check_collect_status_usecase.dart';
import 'package:metro_stamp_app/features/scan/domain/usecases/collect_stamp_usecase.dart';
import 'package:metro_stamp_app/features/scan/domain/usecases/resolve_scan_usecase.dart';
import 'package:metro_stamp_app/features/scan/presentation/cubit/scan_flow_cubit.dart';
import 'package:metro_stamp_app/features/scan/presentation/screens/scan_screen.dart';
import 'package:mocktail/mocktail.dart';

class MockScanRepository extends Mock implements ScanRepository {}

class MockAppLocationService extends Mock implements AppLocationService {}

class MockNfcReader extends Mock implements NfcReader {}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late MockNfcReader nfcReader;
  late ScanFlowCubit cubit;
  late GoRouter router;

  setUp(() {
    nfcReader = MockNfcReader();
    when(() => nfcReader.checkAvailability())
        .thenAnswer((_) async => NfcAvailabilityStatus.enabled);
    when(() => nfcReader.isSessionRunning).thenReturn(false);
    when(() => nfcReader.stopSession()).thenAnswer((_) async {});

    cubit = ScanFlowCubit(
      resolveScanUseCase: ResolveScanUseCase(MockScanRepository()),
      collectStampUseCase: CollectStampUseCase(MockScanRepository()),
      checkCollectStatusUseCase:
          CheckCollectStatusUseCase(MockScanRepository()),
      locationService: MockAppLocationService(),
      idempotencyKeyGenerator: const IdempotencyKeyGenerator(),
      nfcReader: nfcReader,
    );

    router = GoRouter(
      initialLocation: RouteNames.scan,
      routes: [
        GoRoute(
          path: RouteNames.scan,
          builder: (context, state) => BlocProvider.value(
            value: cubit,
            child: const ScanScreen(),
          ),
        ),
      ],
    );
  });

  tearDown(() async {
    await cubit.close();
  });

  testWidgets('does not start duplicate NFC sessions on rebuild',
      (tester) async {
    var startCount = 0;
    when(
      () => nfcReader.startSession(onPayload: any(named: 'onPayload')),
    ).thenAnswer((_) async {
      startCount++;
    });

    await tester.pumpWidget(MaterialApp.router(routerConfig: router));
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 50));
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 50));

    expect(startCount, 1);
  });
}
