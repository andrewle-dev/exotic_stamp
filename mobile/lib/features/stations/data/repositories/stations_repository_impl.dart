import '../../../../core/errors/failure.dart';
import '../../domain/entities/line.dart';
import '../../domain/entities/station.dart';
import '../../domain/entities/station_collected_status.dart';
import '../../domain/entities/station_detail.dart';
import '../../domain/repositories/stations_repository.dart';
import '../datasources/stations_remote_datasource.dart';
import '../models/station_model.dart';

class StationsRepositoryImpl implements StationsRepository {
  StationsRepositoryImpl({required StationsRemoteDataSource remoteDataSource})
      : _remoteDataSource = remoteDataSource;

  final StationsRemoteDataSource _remoteDataSource;

  @override
  Future<List<Line>> getLines() async {
    final lines = await _remoteDataSource.getLines();
    return lines.map((line) => line.toEntity()).toList();
  }

  @override
  Future<List<Station>> getStations({
    String? lineId,
    String? searchQuery,
  }) async {
    final stations = await _remoteDataSource.getStations(lineId: lineId);
    final merged = await _mergeCollectedStatus(
      stations: stations,
      lineId: lineId ?? stations.firstOrNull?.lineId,
    );
    final filtered = _applySearch(merged, searchQuery);
    return filtered.map((station) => station.toEntity()).toList();
  }

  @override
  Future<StationDetail> getStationDetail(String stationId) async {
    final detail = await _remoteDataSource.getStationDetail(stationId);
    final collectedMap = await _loadCollectedMap(lineId: detail.lineId);
    final collectedStatus = _resolveCollectedStatus(
      stationId: stationId,
      collectedMap: collectedMap,
    );
    return detail.copyWithCollectedStatus(collectedStatus).toEntity();
  }

  Future<List<StationModel>> _mergeCollectedStatus({
    required List<StationModel> stations,
    String? lineId,
  }) async {
    final collectedMap = await _loadCollectedMap(lineId: lineId);
    return stations
        .map(
          (station) => station.copyWithCollectedStatus(
            _resolveCollectedStatus(
              stationId: station.id,
              collectedMap: collectedMap,
            ),
          ),
        )
        .toList();
  }

  StationCollectedStatus _resolveCollectedStatus({
    required String stationId,
    required Map<String, bool>? collectedMap,
  }) {
    if (collectedMap == null) {
      return StationCollectedStatus.unknown;
    }
    return collectedMap[stationId] == true
        ? StationCollectedStatus.collected
        : StationCollectedStatus.uncollected;
  }

  Future<Map<String, bool>?> _loadCollectedMap({String? lineId}) async {
    try {
      return await _remoteDataSource.getCollectedStationIds(lineId: lineId);
    } on Failure {
      return null;
    }
  }

  List<StationModel> _applySearch(
    List<StationModel> stations,
    String? searchQuery,
  ) {
    final query = searchQuery?.trim().toLowerCase();
    if (query == null || query.isEmpty) {
      return stations;
    }

    return stations.where((station) {
      final haystack = [
        station.name,
        station.displayName,
        station.code,
      ].whereType<String>().join(' ').toLowerCase();
      return haystack.contains(query);
    }).toList();
  }
}

extension _FirstOrNull<E> on List<E> {
  E? get firstOrNull => isEmpty ? null : first;
}
