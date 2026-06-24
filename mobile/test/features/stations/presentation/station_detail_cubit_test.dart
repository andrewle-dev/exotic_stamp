import 'package:bloc_test/bloc_test.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/errors/failure.dart';
import 'package:metro_stamp_app/features/stations/domain/entities/station_collected_status.dart';
import 'package:metro_stamp_app/features/stations/domain/entities/station_detail.dart';
import 'package:metro_stamp_app/features/stations/domain/repositories/stations_repository.dart';
import 'package:metro_stamp_app/features/stations/domain/usecases/get_station_detail_usecase.dart';
import 'package:metro_stamp_app/features/stations/presentation/cubit/station_detail_cubit.dart';
import 'package:metro_stamp_app/features/stations/presentation/cubit/station_detail_state.dart';
import 'package:mocktail/mocktail.dart';

class MockStationsRepository extends Mock implements StationsRepository {}

void main() {
  late MockStationsRepository repository;

  const detail = StationDetail(
    id: 's1',
    lineId: 'line-1',
    name: 'Ben Thanh',
    collectedStatus: StationCollectedStatus.collected,
  );

  setUp(() {
    repository = MockStationsRepository();
  });

  blocTest<StationDetailCubit, StationDetailState>(
    'emits loaded on success',
    build: () {
      when(() => repository.getStationDetail('s1'))
          .thenAnswer((_) async => detail);
      return StationDetailCubit(
        getStationDetailUseCase: GetStationDetailUseCase(repository),
        stationId: 's1',
      );
    },
    act: (cubit) => cubit.load(),
    expect: () => [
      isA<StationDetailState>().having(
        (s) => s.status,
        'status',
        StationDetailStatus.loading,
      ),
      isA<StationDetailState>()
          .having((s) => s.status, 'status', StationDetailStatus.loaded)
          .having((s) => s.detail?.name, 'name', 'Ben Thanh'),
    ],
  );

  blocTest<StationDetailCubit, StationDetailState>(
    'emits notFound on 404',
    build: () {
      when(() => repository.getStationDetail('missing')).thenThrow(
        const Failure(
          code: FailureCode.unknown,
          message: 'Not found',
          statusCode: 404,
        ),
      );
      return StationDetailCubit(
        getStationDetailUseCase: GetStationDetailUseCase(repository),
        stationId: 'missing',
      );
    },
    act: (cubit) => cubit.load(),
    expect: () => [
      isA<StationDetailState>().having(
        (s) => s.status,
        'status',
        StationDetailStatus.loading,
      ),
      isA<StationDetailState>().having(
        (s) => s.status,
        'status',
        StationDetailStatus.notFound,
      ),
    ],
  );
}
