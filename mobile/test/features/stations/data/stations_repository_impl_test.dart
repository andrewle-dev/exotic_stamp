import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/errors/failure.dart';
import 'package:metro_stamp_app/features/stations/data/datasources/stations_remote_datasource.dart';
import 'package:metro_stamp_app/features/stations/data/models/station_model.dart';
import 'package:metro_stamp_app/features/stations/data/repositories/stations_repository_impl.dart';
import 'package:metro_stamp_app/features/stations/domain/entities/station_collected_status.dart';
import 'package:mocktail/mocktail.dart';

class MockStationsRemoteDataSource extends Mock
    implements StationsRemoteDataSource {}

void main() {
  late MockStationsRemoteDataSource remoteDataSource;
  late StationsRepositoryImpl repository;

  setUp(() {
    remoteDataSource = MockStationsRemoteDataSource();
    repository = StationsRepositoryImpl(remoteDataSource: remoteDataSource);
  });

  test('merges collected status from stamp-book', () async {
    when(() => remoteDataSource.getStations(lineId: 'line-1')).thenAnswer(
      (_) async => [
        StationModel(
          id: 's1',
          lineId: 'line-1',
          code: 'S01',
          name: 'Ben Thanh',
        ),
        StationModel(
          id: 's2',
          lineId: 'line-1',
          code: 'S02',
          name: 'Station Two',
        ),
      ],
    );
    when(
      () => remoteDataSource.getCollectedStationIds(lineId: 'line-1'),
    ).thenAnswer(
      (_) async => {
        's1': true,
        's2': false,
      },
    );

    final stations = await repository.getStations(lineId: 'line-1');

    expect(stations[0].collectedStatus, StationCollectedStatus.collected);
    expect(stations[1].collectedStatus, StationCollectedStatus.uncollected);
  });

  test('uses unknown collected status when stamp-book fails', () async {
    when(() => remoteDataSource.getStations(lineId: 'line-1')).thenAnswer(
      (_) async => [
        StationModel(
          id: 's1',
          lineId: 'line-1',
          code: 'S01',
          name: 'Ben Thanh',
        ),
      ],
    );
    when(
      () => remoteDataSource.getCollectedStationIds(lineId: 'line-1'),
    ).thenThrow(
      const Failure(
        code: FailureCode.unauthorized,
        message: 'Unauthorized',
      ),
    );

    final stations = await repository.getStations(lineId: 'line-1');

    expect(stations.single.collectedStatus, StationCollectedStatus.unknown);
    expect(stations.single.isCollected, isFalse);
  });
}
