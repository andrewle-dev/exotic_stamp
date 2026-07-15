import 'package:equatable/equatable.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/entities/line.dart';
import '../../domain/entities/station.dart';
import '../utils/stations_line_filter.dart';

enum StationsStatus {
  initial,
  loading,
  loaded,
  emptySearch,
  gpsDisabled,
  failure,
}

class StationsState extends Equatable {
  const StationsState({
    this.status = StationsStatus.initial,
    this.lines = const [],
    this.stations = const [],
    this.selectedLineId = StationsLineFilter.allLines,
    this.searchQuery = '',
    this.gpsStatus = StationsGpsStatus.unknown,
    this.userLatitude,
    this.userLongitude,
    this.failure,
  });

  final StationsStatus status;
  final List<Line> lines;
  final List<Station> stations;
  final String? selectedLineId;
  final String searchQuery;
  final StationsGpsStatus gpsStatus;
  final double? userLatitude;
  final double? userLongitude;
  final Failure? failure;

  Line? get selectedLine {
    if (selectedLineId == null ||
        selectedLineId == StationsLineFilter.allLines) {
      return null;
    }
    for (final line in lines) {
      if (line.id == selectedLineId) {
        return line;
      }
    }
    return null;
  }

  bool get hasGpsCoordinates =>
      userLatitude != null && userLongitude != null;

  StationsState copyWith({
    StationsStatus? status,
    List<Line>? lines,
    List<Station>? stations,
    String? selectedLineId,
    String? searchQuery,
    StationsGpsStatus? gpsStatus,
    double? userLatitude,
    double? userLongitude,
    Failure? failure,
    bool clearFailure = false,
    bool clearUserLocation = false,
  }) {
    return StationsState(
      status: status ?? this.status,
      lines: lines ?? this.lines,
      stations: stations ?? this.stations,
      selectedLineId: selectedLineId ?? this.selectedLineId,
      searchQuery: searchQuery ?? this.searchQuery,
      gpsStatus: gpsStatus ?? this.gpsStatus,
      userLatitude:
          clearUserLocation ? null : (userLatitude ?? this.userLatitude),
      userLongitude:
          clearUserLocation ? null : (userLongitude ?? this.userLongitude),
      failure: clearFailure ? null : (failure ?? this.failure),
    );
  }

  @override
  List<Object?> get props => [
        status,
        lines,
        stations,
        selectedLineId,
        searchQuery,
        gpsStatus,
        userLatitude,
        userLongitude,
        failure,
      ];
}
