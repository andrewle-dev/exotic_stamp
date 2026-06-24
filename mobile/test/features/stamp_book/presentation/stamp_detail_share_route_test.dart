import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:go_router/go_router.dart';
import 'package:metro_stamp_app/app/router/route_names.dart';
import 'package:metro_stamp_app/features/memories/domain/entities/photo_share_context.dart';
import 'package:metro_stamp_app/features/stamp_book/domain/entities/stamp_detail.dart';
import 'package:metro_stamp_app/features/stamp_book/presentation/cubit/stamp_detail_cubit.dart';
import 'package:metro_stamp_app/features/stamp_book/presentation/cubit/stamp_detail_state.dart';
import 'package:metro_stamp_app/features/stamp_book/presentation/screens/stamp_detail_screen.dart';
import 'package:mocktail/mocktail.dart';

class MockStampDetailCubit extends Mock implements StampDetailCubit {}

void main() {
  late MockStampDetailCubit stampCubit;
  late GoRouter router;
  PhotoShareContext? capturedContext;

  const collectedDetail = StampDetail(
    stationId: 'station-1',
    stationName: 'Ben Thanh',
    collected: true,
    stampId: 'stamp-uuid',
    stampDesignUrl: '/uploads/stamp.png',
    collectedAt: null,
    lineName: 'Line 1',
  );

  setUp(() {
    stampCubit = MockStampDetailCubit();
    when(() => stampCubit.state).thenReturn(
      const StampDetailState(
        status: StampDetailStatus.loaded,
        detail: collectedDetail,
      ),
    );
    when(() => stampCubit.stream).thenAnswer((_) => const Stream.empty());
    when(() => stampCubit.close()).thenAnswer((_) async {});
    when(() => stampCubit.load()).thenAnswer((_) async {});

    router = GoRouter(
      initialLocation: RouteNames.stampDetail('station-1'),
      routes: [
        GoRoute(
          path: '/stamps/:stationId',
          builder: (context, state) => StampDetailScreen(
            stationId: state.pathParameters['stationId']!,
            cubit: stampCubit,
          ),
        ),
        GoRoute(
          path: RouteNames.memoriesCreate,
          builder: (context, state) {
            capturedContext = state.extra as PhotoShareContext?;
            return const Scaffold(body: Text('Photo Share Route'));
          },
        ),
      ],
    );
  });

  testWidgets('collected stamp share CTA routes to memories create',
      (tester) async {
    capturedContext = null;

    await tester.pumpWidget(MaterialApp.router(routerConfig: router));
    await tester.pump();

    expect(find.text('Chia sẻ Stamp'), findsOneWidget);

    await tester.scrollUntilVisible(
      find.text('Chia sẻ Stamp'),
      120,
    );
    await tester.tap(find.text('Chia sẻ Stamp'));
    await tester.pumpAndSettle();

    expect(find.text('Photo Share Route'), findsOneWidget);
    expect(capturedContext?.stationId, 'station-1');
    expect(capturedContext?.stationName, 'Ben Thanh');
    expect(
        capturedContext?.shareType, PhotoShareContext.shareTypeStampCollected);
    expect(capturedContext?.stampId, 'stamp-uuid');
  });
}
