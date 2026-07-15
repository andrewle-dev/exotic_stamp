/// Sentinel for the stations list "All Lines" filter chip.
abstract final class StationsLineFilter {
  static const allLines = '__all__';
}

/// GPS availability for distance sorting on the stations list.
enum StationsGpsStatus {
  unknown,
  enabled,
  disabled,
}

/// Sort order for the station directory list.
enum StationsSortMode {
  distance,
}
