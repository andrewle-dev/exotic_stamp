import 'package:equatable/equatable.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/entities/line.dart';
import '../../domain/entities/station.dart';
import '../utils/stations_line_filter.dart';
import '../utils/stations_list_presenter.dart';

enum StationsStatus {
  initial,
  loading,
  loaded,
  emptySearch,
  emptyFilter,
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
    this.sortMode = StationsSortMode.distance,
    this.collectionFilter = StationsCollectionFilter.all,
    this.availabilityFilter = StationsAvailabilityFilter.all,
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
  final StationsSortMode sortMode;
  final StationsCollectionFilter collectionFilter;
  final StationsAvailabilityFilter availabilityFilter;
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

  bool get hasCollectionFilterData =>
      StationsListPresenter.hasCollectionStatusData(stations);

  bool get hasAvailabilityFilterData =>
      StationsListPresenter.hasAvailabilityStatusData(stations);

  bool get hasActiveClientFilters =>
      collectionFilter != StationsCollectionFilter.all ||
      availabilityFilter != StationsAvailabilityFilter.all;

  List<Station> get visibleStations {
    return StationsListPresenter.applyClientFilters(
      stations: stations,
      collectionFilter: collectionFilter,
      availabilityFilter: availabilityFilter,
    );
  }

  List<Station> get sortedVisibleStations {
    return StationsListPresenter.sortStations(
      stations: visibleStations,
      sortMode: sortMode,
      lines: lines,
      userLatitude: userLatitude,
      userLongitude: userLongitude,
      hasGps: hasGpsCoordinates,
    );
  }

  StationsState copyWith({
    StationsStatus? status,
    List<Line>? lines,
    List<Station>? stations,
    String? selectedLineId,
    String? searchQuery,
    StationsSortMode? sortMode,
    StationsCollectionFilter? collectionFilter,
    StationsAvailabilityFilter? availabilityFilter,
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
      sortMode: sortMode ?? this.sortMode,
      collectionFilter: collectionFilter ?? this.collectionFilter,
      availabilityFilter: availabilityFilter ?? this.availabilityFilter,
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
        sortMode,
        collectionFilter,
        availabilityFilter,
        gpsStatus,
        userLatitude,
        userLongitude,
        failure,
      ];
}
