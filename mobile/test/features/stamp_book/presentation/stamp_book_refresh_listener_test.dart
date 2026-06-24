import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:go_router/go_router.dart';
import 'package:metro_stamp_app/app/router/route_names.dart';
import 'package:metro_stamp_app/app/router/stamp_book_route_refresh.dart';
import 'package:metro_stamp_app/features/stamp_book/domain/entities/stamp_book.dart';
import 'package:metro_stamp_app/features/stamp_book/domain/entities/stamp_item.dart';
import 'package:metro_stamp_app/features/stamp_book/presentation/cubit/stamp_book_cubit.dart';
import 'package:metro_stamp_app/features/stamp_book/presentation/cubit/stamp_book_state.dart';
import 'package:metro_stamp_app/features/stamp_book/presentation/widgets/stamp_book_refresh_listener.dart';
import 'package:mocktail/mocktail.dart';

class MockStampBookCubit extends Mock implements StampBookCubit {}

void main() {
  late MockStampBookCubit cubit;
  late GoRouter router;

  const loadedState = StampBookState(
    status: StampBookStatus.loaded,
    selectedLineId: 'line-1',
    stampBook: StampBook(
      lineId: 'line-1',
      lineName: 'Line 1',
      progress: StampBookProgress(
        lineId: 'line-1',
        collected: 1,
        total: 2,
        percentage: 50,
      ),
      stations: [
        StampItem(
          stationId: 'station-1',
          stationName: 'Ben Thanh',
          sequence: 1,
          collected: true,
        ),
      ],
    ),
  );

  setUp(() {
    cubit = MockStampBookCubit();
    when(() => cubit.state).thenReturn(const StampBookState());
    when(() => cubit.stream).thenAnswer((_) => const Stream.empty());
    when(() => cubit.close()).thenAnswer((_) async {});
    when(() => cubit.load()).thenAnswer((_) async {});
    when(() => cubit.refresh()).thenAnswer((_) async {});
  });

  Widget buildApp(String location) {
    router = GoRouter(
      initialLocation: location,
      routes: [
        GoRoute(
          path: RouteNames.stampBook,
          builder: (context, state) => BlocProvider<StampBookCubit>.value(
            value: cubit,
            child: const StampBookRefreshListener(
              child: SizedBox(key: Key('stamp-book-body')),
            ),
          ),
        ),
      ],
    );
    return MaterialApp.router(routerConfig: router);
  }

  testWidgets('loads on first mount without refresh token', (tester) async {
    await tester.pumpWidget(buildApp(RouteNames.stampBook));
    await tester.pump();
    await tester.pump();

    verify(() => cubit.load()).called(1);
    verifyNever(() => cubit.refresh());
  });

  testWidgets('loads on first mount with refresh token when cubit is initial',
      (tester) async {
    await tester.pumpWidget(
      buildApp(StampBookRouteRefresh.locationWithRefresh('token-1')),
    );
    await tester.pump();
    await tester.pump();

    verify(() => cubit.load()).called(1);
    verifyNever(() => cubit.refresh());
  });

  testWidgets('refetches when refresh query changes', (tester) async {
    when(() => cubit.state).thenReturn(loadedState);

    await tester.pumpWidget(
      buildApp(StampBookRouteRefresh.locationWithRefresh('token-1')),
    );
    await tester.pump();
    await tester.pump();

    verify(() => cubit.refresh()).called(1);
    verifyNever(() => cubit.load());

    clearInteractions(cubit);

    router.go(StampBookRouteRefresh.locationWithRefresh('token-2'));
    await tester.pump();
    await tester.pump();

    verify(() => cubit.refresh()).called(1);
  });
}
