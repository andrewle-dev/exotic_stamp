import 'package:bloc_test/bloc_test.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/errors/failure.dart';
import 'package:metro_stamp_app/features/home/domain/entities/home_summary.dart';
import 'package:metro_stamp_app/features/home/presentation/cubit/home_cubit.dart';
import 'package:metro_stamp_app/features/home/presentation/cubit/home_state.dart';
import 'package:metro_stamp_app/features/home/presentation/screens/home_screen.dart';
import 'package:metro_stamp_app/features/home/presentation/widgets/recent_stamp_card.dart';
import 'package:metro_stamp_app/shared/widgets/app_empty_state.dart';
import 'package:metro_stamp_app/shared/widgets/app_error_view.dart';
import 'package:metro_stamp_app/shared/widgets/app_loading_view.dart';
import 'package:mocktail/mocktail.dart';

class MockHomeCubit extends MockCubit<HomeState> implements HomeCubit {}

void main() {
  late MockHomeCubit cubit;

  setUp(() {
    cubit = MockHomeCubit();
  });

  Widget buildSubject() {
    return MaterialApp(
      home: HomeScreen(cubit: cubit),
    );
  }

  testWidgets('shows loading view', (tester) async {
    when(() => cubit.state)
        .thenReturn(const HomeState(status: HomeStatus.loading));
    when(() => cubit.load()).thenAnswer((_) async {});

    await tester.pumpWidget(buildSubject());
    await tester.pump();

    expect(find.byType(AppLoadingView), findsOneWidget);
  });

  testWidgets('shows error view', (tester) async {
    when(() => cubit.state).thenReturn(
      const HomeState(
        status: HomeStatus.failure,
        failure: Failure(
          code: FailureCode.networkError,
          message: 'Network down',
        ),
      ),
    );
    when(() => cubit.load()).thenAnswer((_) async {});

    await tester.pumpWidget(buildSubject());
    await tester.pump();

    expect(find.byType(AppErrorView), findsOneWidget);
  });

  testWidgets('shows loaded content and empty recent stamps', (tester) async {
    when(() => cubit.state).thenReturn(
      const HomeState(
        status: HomeStatus.loaded,
        summary: HomeSummary(
          displayName: 'An Nguyen',
          progress: CollectionProgress(
            lineId: 'line-1',
            collected: 0,
            total: 5,
            percentage: 0,
          ),
        ),
      ),
    );
    when(() => cubit.load()).thenAnswer((_) async {});

    await tester.pumpWidget(buildSubject());
    await tester.pump();

    expect(find.text('Xin chào, An Nguyen'), findsOneWidget);
    expect(find.byType(AppEmptyState), findsOneWidget);
    expect(find.text('Chưa có stamp'), findsOneWidget);
  });

  testWidgets('shows recent stamp cards when data exists', (tester) async {
    when(() => cubit.state).thenReturn(
      HomeState(
        status: HomeStatus.loaded,
        summary: HomeSummary(
          displayName: 'An Nguyen',
          progress: const CollectionProgress(
            lineId: 'line-1',
            collected: 1,
            total: 5,
            percentage: 20,
          ),
          recentStamps: [
            RecentStamp(
              stationId: 's1',
              stationName: 'Ben Thanh Station Name',
              collectedAt: DateTime(2026, 6, 20),
            ),
          ],
        ),
      ),
    );
    when(() => cubit.load()).thenAnswer((_) async {});

    await tester.pumpWidget(buildSubject());
    await tester.pump();

    expect(find.byType(RecentStampCard), findsOneWidget);
    expect(find.text('Ben Thanh Station Name'), findsOneWidget);
  });

  testWidgets('shows partial error banner without replacing loaded sections',
      (tester) async {
    when(() => cubit.state).thenReturn(
      const HomeState(
        status: HomeStatus.loaded,
        summary: HomeSummary(
          displayName: 'An Nguyen',
          progress: CollectionProgress(
            lineId: 'line-1',
            collected: 2,
            total: 5,
            percentage: 40,
          ),
          partialErrors: ['Recent stamps unavailable'],
        ),
      ),
    );
    when(() => cubit.load()).thenAnswer((_) async {});

    await tester.pumpWidget(buildSubject());
    await tester.pump();

    expect(find.text('Recent stamps unavailable'), findsOneWidget);
    expect(find.text('Xin chào, An Nguyen'), findsOneWidget);
    expect(find.text('2/5'), findsOneWidget);
  });
}
