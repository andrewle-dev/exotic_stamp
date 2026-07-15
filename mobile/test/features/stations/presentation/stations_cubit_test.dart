import 'package:bloc_test/bloc_test.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/errors/failure.dart';
import 'package:metro_stamp_app/features/stations/domain/entities/line.dart';
import 'package:metro_stamp_app/features/stations/domain/entities/station.dart';
import 'package:metro_stamp_app/features/stations/domain/entities/station_collected_status.dart';
import 'package:metro_stamp_app/features/stations/domain/repositories/stations_repository.dart';
import 'package:metro_stamp_app/features/stations/domain/usecases/get_lines_usecase.dart';
import 'package:metro_stamp_app/features/stations/domain/usecases/get_stations_usecase.dart';
import 'package:metro_stamp_app/features/stations/presentation/cubit/stations_cubit.dart';
import 'package:metro_stamp_app/features/stations/presentation/cubit/stations_state.dart';
import 'package:metro_stamp_app/features/stations/presentation/utils/stations_line_filter.dart';
import 'package:mocktail/mocktail.dart';

class MockStationsRepository extends Mock implements StationsRepository {}

void main() {
  late MockStationsRepository repository;
  late StationsCubit cubit;

  const lines = [
    Line(id: 'line-1', name: 'Line 1', status: 'ACTIVE'),
  ];

  const stations = [
    Station(
      id: 's1',
      lineId: 'line-1',
      code: 'S01',
      name: 'Ben Thanh',
      collectedStatus: StationCollectedStatus.collected,
    ),
  ];

  setUp(() {
    repository = MockStationsRepository();
    cubit = StationsCubit(
      getLinesUseCase: GetLinesUseCase(repository),
      getStationsUseCase: GetStationsUseCase(repository),
    );
  });

  tearDown(() => cubit.close());

  blocTest<StationsCubit, StationsState>(
    'emits loading then loaded on success',
    build: () {
      when(() => repository.getLines()).thenAnswer((_) async => lines);
      when(
        () => repository.getStations(lineId: null, searchQuery: ''),
      ).thenAnswer((_) async => stations);
      return cubit;
    },
    act: (cubit) => cubit.load(),
    expect: () => [
      isA<StationsState>()
          .having((s) => s.status, 'status', StationsStatus.loading),
      isA<StationsState>()
          .having((s) => s.status, 'status', StationsStatus.loaded)
          .having((s) => s.stations, 'stations', stations)
          .having(
            (s) => s.selectedLineId,
            'selectedLineId',
            StationsLineFilter.allLines,
          ),
    ],
  );

  blocTest<StationsCubit, StationsState>(
    'emits emptySearch when search returns no stations',
    build: () {
      when(() => repository.getLines()).thenAnswer((_) async => lines);
      when(
        () => repository.getStations(lineId: null, searchQuery: 'missing'),
      ).thenAnswer((_) async => const <Station>[]);
      return cubit;
    },
    act: (cubit) async {
      await cubit.load();
      await cubit.updateSearch('missing');
    },
    verify: (_) {
      expect(cubit.state.status, StationsStatus.emptySearch);
    },
  );

  blocTest<StationsCubit, StationsState>(
    'emits failure on error',
    build: () {
      when(() => repository.getLines()).thenThrow(
        const Failure(
          code: FailureCode.networkError,
          message: 'Network down',
        ),
      );
      return cubit;
    },
    act: (cubit) => cubit.load(),
    expect: () => [
      isA<StationsState>()
          .having((s) => s.status, 'status', StationsStatus.loading),
      isA<StationsState>()
          .having((s) => s.status, 'status', StationsStatus.failure),
    ],
  );
}
