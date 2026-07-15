import '../../../features/stations/domain/entities/line.dart';

import '../../../features/stations/domain/entities/station.dart';

import '../../../features/stations/domain/entities/station_detail.dart';

import '../../../features/stations/domain/repositories/stations_repository.dart';

import '../../../features/stations/presentation/utils/stations_line_filter.dart';

import '../mock_data_store.dart';

import '../mock_fixtures.dart';



/// Mock [StationsRepository] — UI development only.

class MockStationsRepository implements StationsRepository {

  MockStationsRepository({MockDataStore? store})

      : _store = store ?? MockDataStore.instance;



  final MockDataStore _store;



  @override

  Future<List<Line>> getLines() async {

    await Future<void>.delayed(const Duration(milliseconds: 150));

    return MockFixtures.lines();

  }



  @override

  Future<List<Station>> getStations({

    String? lineId,

    String? searchQuery,

  }) async {

    await Future<void>.delayed(const Duration(milliseconds: 200));

    if (lineId != null &&

        lineId != MockFixtures.lineId &&

        lineId != StationsLineFilter.allLines) {

      return const [];

    }



    var catalog = MockFixtures.stationCatalog();

    if (searchQuery != null && searchQuery.trim().isNotEmpty) {

      final query = searchQuery.trim().toLowerCase();

      catalog = catalog

          .where(

            (s) =>

                s.name.toLowerCase().contains(query) ||

                s.code.toLowerCase().contains(query),

          )

          .toList();

    }

    return catalog

        .map(

          (entry) => MockFixtures.stationFromCatalog(

            entry,

            _store.collectedStationIds,

          ),

        )

        .toList();

  }



  @override

  Future<StationDetail> getStationDetail(String stationId) async {

    await Future<void>.delayed(const Duration(milliseconds: 200));

    final catalog = MockFixtures.stationCatalog();

    final entry = catalog.firstWhere(

      (s) => s.id == stationId,

      orElse: () => catalog.first,

    );

    return MockFixtures.stationDetailFromCatalog(

      entry,

      _store.collectedStationIds,

    );

  }

}

