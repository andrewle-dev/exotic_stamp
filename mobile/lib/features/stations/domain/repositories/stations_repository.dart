import '../entities/line.dart';
import '../entities/station.dart';
import '../entities/station_detail.dart';

abstract class StationsRepository {
  Future<List<Line>> getLines();

  Future<List<Station>> getStations({
    String? lineId,
    String? searchQuery,
  });

  Future<StationDetail> getStationDetail(String stationId);
}
