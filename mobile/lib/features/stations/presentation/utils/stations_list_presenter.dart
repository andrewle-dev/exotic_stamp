import 'package:geolocator/geolocator.dart';

import '../../domain/entities/line.dart';
import '../../domain/entities/station.dart';
import '../../domain/entities/station_collected_status.dart';
import 'stations_line_filter.dart';

/// Pure helpers for stations list presentation — no widget dependencies.
abstract final class StationsListPresenter {
  static bool hasCollectionStatusData(List<Station> stations) {
    return stations.any(
      (station) => station.collectedStatus != StationCollectedStatus.unknown,
    );
  }

  static bool hasAvailabilityStatusData(List<Station> stations) {
    return stations.any((station) => station.status != null);
  }

  /// Client-side filters after backend [lineId] + search have been applied.
  static List<Station> applyClientFilters({
    required List<Station> stations,
    required StationsCollectionFilter collectionFilter,
    required StationsAvailabilityFilter availabilityFilter,
  }) {
    var result = List<Station>.from(stations);

    if (collectionFilter != StationsCollectionFilter.all &&
        hasCollectionStatusData(result)) {
      result = result.where((station) {
        switch (collectionFilter) {
          case StationsCollectionFilter.collected:
            return station.collectedStatus ==
                StationCollectedStatus.collected;
          case StationsCollectionFilter.notCollected:
            return station.collectedStatus ==
                StationCollectedStatus.uncollected;
          case StationsCollectionFilter.all:
            return true;
        }
      }).toList();
    }

    if (availabilityFilter == StationsAvailabilityFilter.activeOnly &&
        hasAvailabilityStatusData(stations)) {
      result = result.where((station) => station.isActive).toList();
    }

    return result;
  }

  static List<Station> sortStations({
    required List<Station> stations,
    required StationsSortMode sortMode,
    required List<Line> lines,
    required double? userLatitude,
    required double? userLongitude,
    required bool hasGps,
  }) {
    final effectiveMode = sortMode == StationsSortMode.distance && !hasGps
        ? StationsSortMode.lineOrder
        : sortMode;

    final sorted = List<Station>.from(stations);
    final lineOrderById = <String, int>{
      for (var i = 0; i < lines.length; i++) lines[i].id: lines[i].sortOrder ?? i,
    };

    sorted.sort((a, b) {
      switch (effectiveMode) {
        case StationsSortMode.distance:
          return _compareDistance(a, b, userLatitude, userLongitude);
        case StationsSortMode.lineOrder:
          return _compareLineOrder(a, b, lineOrderById);
        case StationsSortMode.collectedStatus:
          final byCollected = _collectedRank(a).compareTo(_collectedRank(b));
          if (byCollected != 0) return byCollected;
          return _compareLineOrder(a, b, lineOrderById);
        case StationsSortMode.name:
          return a.label.toLowerCase().compareTo(b.label.toLowerCase());
      }
    });
    return sorted;
  }

  static Station? nearestStation({
    required List<Station> stations,
    required double? userLatitude,
    required double? userLongitude,
  }) {
    if (userLatitude == null ||
        userLongitude == null ||
        stations.isEmpty) {
      return null;
    }
    final byDistance = sortStations(
      stations: stations,
      sortMode: StationsSortMode.distance,
      lines: const [],
      userLatitude: userLatitude,
      userLongitude: userLongitude,
      hasGps: true,
    );
    return byDistance.first;
  }

  static List<Station> directoryStations({
    required List<Station> sortedStations,
    required Station? nearest,
  }) {
    if (nearest == null) {
      return sortedStations;
    }
    return sortedStations.where((station) => station.id != nearest.id).toList();
  }

  static String? distanceLabel({
    required Station station,
    required double? userLatitude,
    required double? userLongitude,
  }) {
    if (userLatitude == null || userLongitude == null) {
      return null;
    }
    final meters = _distanceMeters(station, userLatitude, userLongitude);
    if (meters == null) {
      return null;
    }
    if (meters < 1000) {
      return '${meters.round()} m';
    }
    return '${(meters / 1000).toStringAsFixed(1)} km';
  }

  static String? distanceAwayLabel({
    required Station station,
    required double? userLatitude,
    required double? userLongitude,
  }) {
    final label = distanceLabel(
      station: station,
      userLatitude: userLatitude,
      userLongitude: userLongitude,
    );
    if (label == null) {
      return null;
    }
    return '$label away';
  }

  /// Legacy alias used by older call sites / tests.
  static List<Station> sortedByDistance({
    required List<Station> stations,
    required double? userLatitude,
    required double? userLongitude,
  }) {
    return sortStations(
      stations: stations,
      sortMode: StationsSortMode.distance,
      lines: const [],
      userLatitude: userLatitude,
      userLongitude: userLongitude,
      hasGps: userLatitude != null && userLongitude != null,
    );
  }

  static int _compareDistance(
    Station a,
    Station b,
    double? userLatitude,
    double? userLongitude,
  ) {
    if (userLatitude == null || userLongitude == null) {
      return a.label.toLowerCase().compareTo(b.label.toLowerCase());
    }
    final da =
        _distanceMeters(a, userLatitude, userLongitude) ?? double.infinity;
    final db =
        _distanceMeters(b, userLatitude, userLongitude) ?? double.infinity;
    final byDistance = da.compareTo(db);
    if (byDistance != 0) return byDistance;
    return a.label.toLowerCase().compareTo(b.label.toLowerCase());
  }

  static int _compareLineOrder(
    Station a,
    Station b,
    Map<String, int> lineOrderById,
  ) {
    final lineA = lineOrderById[a.lineId] ?? 1 << 20;
    final lineB = lineOrderById[b.lineId] ?? 1 << 20;
    final byLine = lineA.compareTo(lineB);
    if (byLine != 0) return byLine;

    final orderA = a.sortOrder ?? 1 << 20;
    final orderB = b.sortOrder ?? 1 << 20;
    final byStation = orderA.compareTo(orderB);
    if (byStation != 0) return byStation;

    return a.label.toLowerCase().compareTo(b.label.toLowerCase());
  }

  /// Not collected first, then collected, unknown last.
  static int _collectedRank(Station station) {
    switch (station.collectedStatus) {
      case StationCollectedStatus.uncollected:
        return 0;
      case StationCollectedStatus.collected:
        return 1;
      case StationCollectedStatus.unknown:
        return 2;
    }
  }

  static double? _distanceMeters(
    Station station,
    double userLatitude,
    double userLongitude,
  ) {
    final lat = station.latitude;
    final lng = station.longitude;
    if (lat == null || lng == null) {
      return null;
    }
    return Geolocator.distanceBetween(userLatitude, userLongitude, lat, lng);
  }
}
