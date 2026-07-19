import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/entities/line.dart';
import '../../domain/entities/station.dart';
import '../../domain/usecases/get_lines_usecase.dart';
import '../../domain/usecases/get_stations_usecase.dart';
import '../utils/stations_line_filter.dart';
import '../utils/stations_list_presenter.dart';
import 'stations_state.dart';

class StationsCubit extends Cubit<StationsState> {
  StationsCubit({
    required GetLinesUseCase getLinesUseCase,
    required GetStationsUseCase getStationsUseCase,
  })  : _getLinesUseCase = getLinesUseCase,
        _getStationsUseCase = getStationsUseCase,
        super(const StationsState());

  final GetLinesUseCase _getLinesUseCase;
  final GetStationsUseCase _getStationsUseCase;

  Future<void> load() async {
    emit(state.copyWith(status: StationsStatus.loading, clearFailure: true));
    try {
      final lines = await _getLinesUseCase();
      final selectedLineId = _resolveSelectedLineId(lines);
      final stations = await _fetchStations(selectedLineId);
      emit(
        _loadedState(
          lines: lines,
          selectedLineId: selectedLineId,
          stations: stations,
        ),
      );
    } on Failure catch (failure) {
      emit(
        state.copyWith(
          status: StationsStatus.failure,
          failure: failure,
        ),
      );
    } catch (_) {
      emit(
        state.copyWith(
          status: StationsStatus.failure,
          failure: const Failure(
            code: FailureCode.unknown,
            message: 'Không thể tải danh sách ga.',
          ),
        ),
      );
    }
  }

  Future<void> selectLine(String lineId) async {
    emit(
      state.copyWith(
        status: StationsStatus.loading,
        selectedLineId: lineId,
        clearFailure: true,
      ),
    );
    await _reloadStations();
  }

  Future<void> updateSearch(String query) async {
    emit(state.copyWith(searchQuery: query));
    await _reloadStations();
  }

  void updateSortMode(StationsSortMode sortMode) {
    final next = state.copyWith(sortMode: sortMode);
    emit(
      next.copyWith(
        status: _resolveLoadedStatus(
          stations: next.stations,
          searchQuery: next.searchQuery,
          collectionFilter: next.collectionFilter,
          availabilityFilter: next.availabilityFilter,
        ),
      ),
    );
  }

  void updateCollectionFilter(StationsCollectionFilter filter) {
    final next = state.copyWith(collectionFilter: filter);
    emit(
      next.copyWith(
        status: _resolveLoadedStatus(
          stations: next.stations,
          searchQuery: next.searchQuery,
          collectionFilter: next.collectionFilter,
          availabilityFilter: next.availabilityFilter,
        ),
      ),
    );
  }

  void updateAvailabilityFilter(StationsAvailabilityFilter filter) {
    final next = state.copyWith(availabilityFilter: filter);
    emit(
      next.copyWith(
        status: _resolveLoadedStatus(
          stations: next.stations,
          searchQuery: next.searchQuery,
          collectionFilter: next.collectionFilter,
          availabilityFilter: next.availabilityFilter,
        ),
      ),
    );
  }

  Future<void> applyFilterSheet({
    required StationsSortMode sortMode,
    required String selectedLineId,
    required StationsCollectionFilter collectionFilter,
    required StationsAvailabilityFilter availabilityFilter,
  }) async {
    final lineChanged = selectedLineId != state.selectedLineId;
    emit(
      state.copyWith(
        sortMode: sortMode,
        collectionFilter: collectionFilter,
        availabilityFilter: availabilityFilter,
        selectedLineId: selectedLineId,
        status: lineChanged ? StationsStatus.loading : state.status,
        clearFailure: true,
      ),
    );
    if (lineChanged) {
      await _reloadStations();
      return;
    }
    emit(
      state.copyWith(
        status: _resolveLoadedStatus(
          stations: state.stations,
          searchQuery: state.searchQuery,
          collectionFilter: collectionFilter,
          availabilityFilter: availabilityFilter,
        ),
      ),
    );
  }

  Future<void> resetFilters() async {
    final shouldReload =
        state.selectedLineId != StationsLineFilter.allLines;
    final defaultSort = state.hasGpsCoordinates
        ? StationsSortMode.distance
        : StationsSortMode.lineOrder;
    emit(
      state.copyWith(
        sortMode: defaultSort,
        collectionFilter: StationsCollectionFilter.all,
        availabilityFilter: StationsAvailabilityFilter.all,
        selectedLineId: StationsLineFilter.allLines,
        status: shouldReload ? StationsStatus.loading : state.status,
        clearFailure: true,
      ),
    );
    if (shouldReload) {
      await _reloadStations();
      return;
    }
    emit(
      state.copyWith(
        status: _resolveLoadedStatus(
          stations: state.stations,
          searchQuery: state.searchQuery,
          collectionFilter: StationsCollectionFilter.all,
          availabilityFilter: StationsAvailabilityFilter.all,
        ),
      ),
    );
  }

  void updateUserLocation({required double latitude, required double longitude}) {
    emit(
      state.copyWith(
        userLatitude: latitude,
        userLongitude: longitude,
        gpsStatus: StationsGpsStatus.enabled,
        status: _resolveLoadedStatus(
          stations: state.stations,
          searchQuery: state.searchQuery,
          collectionFilter: state.collectionFilter,
          availabilityFilter: state.availabilityFilter,
        ),
      ),
    );
  }

  void markGpsDisabled() {
    final nextSort = state.sortMode == StationsSortMode.distance
        ? StationsSortMode.lineOrder
        : state.sortMode;
    emit(
      state.copyWith(
        gpsStatus: StationsGpsStatus.disabled,
        sortMode: nextSort,
        clearUserLocation: true,
        status: _resolveLoadedStatus(
          stations: state.stations,
          searchQuery: state.searchQuery,
          collectionFilter: state.collectionFilter,
          availabilityFilter: state.availabilityFilter,
          forceGpsDisabled: true,
        ),
      ),
    );
  }

  Future<void> _reloadStations() async {
    try {
      final stations = await _fetchStations(state.selectedLineId);
      emit(
        _loadedState(
          lines: state.lines,
          selectedLineId: state.selectedLineId,
          stations: stations,
        ),
      );
    } on Failure catch (failure) {
      emit(
        state.copyWith(
          status: StationsStatus.failure,
          failure: failure,
        ),
      );
    }
  }

  Future<List<Station>> _fetchStations(String? selectedLineId) {
    return _getStationsUseCase(
      lineId: _apiLineId(selectedLineId),
      searchQuery: state.searchQuery,
    );
  }

  StationsState _loadedState({
    required List<Line> lines,
    required String? selectedLineId,
    required List<Station> stations,
  }) {
    final status = _resolveLoadedStatus(
      stations: stations,
      searchQuery: state.searchQuery,
      collectionFilter: state.collectionFilter,
      availabilityFilter: state.availabilityFilter,
    );
    return state.copyWith(
      status: status,
      lines: lines,
      selectedLineId: selectedLineId,
      stations: stations,
      clearFailure: true,
    );
  }

  StationsStatus _resolveLoadedStatus({
    required List<Station> stations,
    required String searchQuery,
    required StationsCollectionFilter collectionFilter,
    required StationsAvailabilityFilter availabilityFilter,
    bool forceGpsDisabled = false,
  }) {
    if (stations.isEmpty && searchQuery.trim().isNotEmpty) {
      return StationsStatus.emptySearch;
    }

    final visible = StationsListPresenter.applyClientFilters(
      stations: stations,
      collectionFilter: collectionFilter,
      availabilityFilter: availabilityFilter,
    );
    if (stations.isNotEmpty && visible.isEmpty) {
      return StationsStatus.emptyFilter;
    }

    if (forceGpsDisabled || state.gpsStatus == StationsGpsStatus.disabled) {
      return StationsStatus.gpsDisabled;
    }
    return StationsStatus.loaded;
  }

  String? _resolveSelectedLineId(List<Line> lines) {
    final current = state.selectedLineId;
    if (current == StationsLineFilter.allLines) {
      return StationsLineFilter.allLines;
    }
    if (current != null && lines.any((line) => line.id == current)) {
      return current;
    }
    return StationsLineFilter.allLines;
  }

  String? _apiLineId(String? selectedLineId) {
    if (selectedLineId == null ||
        selectedLineId == StationsLineFilter.allLines) {
      return null;
    }
    return selectedLineId;
  }
}
