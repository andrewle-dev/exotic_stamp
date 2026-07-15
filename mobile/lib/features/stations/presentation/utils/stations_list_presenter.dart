import 'package:geolocator/geolocator.dart';

import '../../domain/entities/station.dart';

/// Pure helpers for stations list presentation — no widget dependencies.
abstract final class StationsListPresenter {
  static List<Station> sortedByDistance({
    required List<Station> stations,
    required double? userLatitude,
    required double? userLongitude,
  }) {
    if (userLatitude == null || userLongitude == null) {
      return List<Station>.from(stations);
    }

    final sorted = [...stations];
    sorted.sort((a, b) {
      final da = _distanceMeters(a, userLatitude, userLongitude) ?? double.infinity;
      final db = _distanceMeters(b, userLatitude, userLongitude) ?? double.infinity;
      return da.compareTo(db);
    });
    return sorted;
  }

  static Station? nearestStation({
    required List<Station> sortedStations,
    required double? userLatitude,
    required double? userLongitude,
  }) {
    if (userLatitude == null ||
        userLongitude == null ||
        sortedStations.isEmpty) {
      return null;
    }
    return sortedStations.first;
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
