import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:go_router/go_router.dart';
import 'package:metro_stamp_app/app/router/route_names.dart';
import 'package:metro_stamp_app/app/router/stamp_book_route_refresh.dart';
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

  final collectResult = CollectStampResult(
    stamp: CollectedStamp(
      stampId: 'stamp-1',
      stationId: 'station-1',
      stationName: 'Ben Thanh',
      collectedAt: DateTime(2026, 6, 24),
    ),
    isNew: true,
  );

  setUp(() {
    scanFlowCubit = MockScanFlowCubit();
    when(() => scanFlowCubit.state).thenReturn(
      ScanFlowState(
        phase: ScanFlowPhase.success,
        collectResult: collectResult,
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
          path: RouteNames.stampBook,
          builder: (context, state) => Scaffold(
            body: Text(
              'refresh=${state.uri.queryParameters[StampBookRouteRefresh.queryKey]}',
            ),
          ),
        ),
        GoRoute(
          path: RouteNames.home,
          builder: (context, state) =>
              const Scaffold(body: Text('Home Screen')),
        ),
      ],
    );
  });

  testWidgets('scan success CTA navigates to stamp book with refresh signal',
      (tester) async {
    await tester.pumpWidget(MaterialApp.router(routerConfig: router));
    await tester.pumpAndSettle();

    expect(find.text('Xem Stamp Book'), findsOneWidget);
    await tester.tap(find.text('Xem Stamp Book'));
    await tester.pumpAndSettle();

    verify(() => scanFlowCubit.resetFlow()).called(1);

    final refreshToken = router.routerDelegate.currentConfiguration.uri
        .queryParameters[StampBookRouteRefresh.queryKey];
    expect(refreshToken, isNotNull);
    expect(refreshToken, isNotEmpty);
    expect(find.textContaining('refresh=$refreshToken'), findsOneWidget);
  });

  testWidgets('scan success does not pass collect result into stamp book route',
      (tester) async {
    await tester.pumpWidget(MaterialApp.router(routerConfig: router));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Xem Stamp Book'));
    await tester.pumpAndSettle();

    final uri = router.routerDelegate.currentConfiguration.uri;
    expect(uri.path, RouteNames.stampBook);
    expect(uri.queryParameters.containsKey(StampBookRouteRefresh.queryKey),
        isTrue);
    expect(uri.queryParameters.containsKey('stampId'), isFalse);
    expect(uri.queryParameters.containsKey('stationId'), isFalse);
    expect(uri.fragment, isEmpty);
  });
}
