import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:go_router/go_router.dart';
import 'package:metro_stamp_app/app/router/route_names.dart';
import 'package:metro_stamp_app/features/scan/domain/entities/collect_stamp_result.dart';
import 'package:metro_stamp_app/features/scan/presentation/cubit/scan_flow_cubit.dart';
import 'package:metro_stamp_app/features/scan/presentation/cubit/scan_flow_state.dart';
import 'package:metro_stamp_app/features/scan/presentation/screens/stamp_collected_success_screen.dart';
import 'package:mocktail/mocktail.dart';

class MockScanFlowCubit extends Mock implements ScanFlowCubit {}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late MockScanFlowCubit scanFlowCubit;
  late GoRouter router;

  setUp(() {
    scanFlowCubit = MockScanFlowCubit();
    when(() => scanFlowCubit.state).thenReturn(
      ScanFlowState(
        phase: ScanFlowPhase.success,
        collectResult: CollectStampResult(
          stamp: CollectedStamp(
            stampId: 'stamp-1',
            stationId: 'station-1',
            stationName: 'Ben Thanh',
            collectedAt: DateTime(2026, 6, 24),
          ),
          isNew: true,
        ),
      ),
    );
    when(() => scanFlowCubit.resetFlow()).thenReturn(null);
    when(() => scanFlowCubit.stream)
        .thenAnswer((_) => const Stream<ScanFlowState>.empty());
    when(() => scanFlowCubit.close()).thenAnswer((_) async {});

    router = GoRouter(
      initialLocation: RouteNames.scanSuccess,
      routes: [
        GoRoute(
          path: RouteNames.scanSuccess,
          builder: (context, state) => StampCollectedSuccessScreen(
            cubit: scanFlowCubit,
          ),
        ),
        GoRoute(
          path: RouteNames.scanTapToCollect,
          builder: (context, state) =>
              const Scaffold(body: Text('Tap To Collect')),
        ),
      ],
    );
  });

  testWidgets('scan next CTA navigates to tap-to-collect and resets flow',
      (tester) async {
    await tester.pumpWidget(MaterialApp.router(routerConfig: router));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Quét tiếp'));
    await tester.pumpAndSettle();

    verify(() => scanFlowCubit.resetFlow()).called(1);
    expect(
      router.routerDelegate.currentConfiguration.uri.path,
      RouteNames.scanTapToCollect,
    );
    expect(find.text('Tap To Collect'), findsOneWidget);
  });
}
