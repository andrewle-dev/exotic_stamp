import 'package:bloc_test/bloc_test.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/errors/failure.dart';
import 'package:metro_stamp_app/features/home/domain/entities/home_summary.dart';
import 'package:metro_stamp_app/features/home/domain/repositories/home_repository.dart';
import 'package:metro_stamp_app/features/home/domain/usecases/get_home_summary_usecase.dart';
import 'package:metro_stamp_app/features/home/presentation/cubit/home_cubit.dart';
import 'package:metro_stamp_app/features/home/presentation/cubit/home_state.dart';
import 'package:mocktail/mocktail.dart';

class MockHomeRepository extends Mock implements HomeRepository {}

void main() {
  late MockHomeRepository repository;
  late HomeCubit cubit;

  final summary = HomeSummary(
    displayName: 'An Nguyen',
    lineId: 'line-1',
    lineName: 'Line 1',
    progress: const CollectionProgress(
      lineId: 'line-1',
      collected: 3,
      total: 10,
      percentage: 30,
    ),
    recentStamps: [
      RecentStamp(
        stationId: 's1',
        stationName: 'Ben Thanh',
        collectedAt: DateTime(2026, 6, 20),
      ),
    ],
  );

  setUp(() {
    repository = MockHomeRepository();
    cubit = HomeCubit(
      getHomeSummaryUseCase: GetHomeSummaryUseCase(repository),
    );
  });

  tearDown(() => cubit.close());

  blocTest<HomeCubit, HomeState>(
    'emits loading then loaded on success',
    build: () {
      when(() => repository.getHomeSummary()).thenAnswer((_) async => summary);
      return cubit;
    },
    act: (cubit) => cubit.load(),
    expect: () => [
      isA<HomeState>().having((s) => s.status, 'status', HomeStatus.loading),
      isA<HomeState>()
          .having((s) => s.status, 'status', HomeStatus.loaded)
          .having((s) => s.summary?.displayName, 'name', 'An Nguyen'),
    ],
  );

  blocTest<HomeCubit, HomeState>(
    'emits failure on repository error',
    build: () {
      when(() => repository.getHomeSummary()).thenThrow(
        const Failure(
          code: FailureCode.networkError,
          message: 'Network down',
        ),
      );
      return cubit;
    },
    act: (cubit) => cubit.load(),
    expect: () => [
      isA<HomeState>().having((s) => s.status, 'status', HomeStatus.loading),
      isA<HomeState>()
          .having((s) => s.status, 'status', HomeStatus.failure)
          .having((s) => s.failure?.code, 'code', FailureCode.networkError),
    ],
  );
}
