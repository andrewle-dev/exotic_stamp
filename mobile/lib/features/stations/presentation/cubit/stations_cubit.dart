import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/entities/line.dart';
import '../../domain/entities/station.dart';
import '../../domain/usecases/get_lines_usecase.dart';
import '../../domain/usecases/get_stations_usecase.dart';
import '../utils/stations_line_filter.dart';
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

  void updateUserLocation({required double latitude, required double longitude}) {
    emit(
      state.copyWith(
        userLatitude: latitude,
        userLongitude: longitude,
        gpsStatus: StationsGpsStatus.enabled,
        status: _resolveLoadedStatus(
          stations: state.stations,
          searchQuery: state.searchQuery,
        ),
      ),
    );
  }

  void markGpsDisabled() {
    emit(
      state.copyWith(
        gpsStatus: StationsGpsStatus.disabled,
        clearUserLocation: true,
        status: state.stations.isEmpty && state.searchQuery.isNotEmpty
            ? StationsStatus.emptySearch
            : StationsStatus.gpsDisabled,
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
  }) {
    if (stations.isEmpty && searchQuery.trim().isNotEmpty) {
      return StationsStatus.emptySearch;
    }
    if (state.gpsStatus == StationsGpsStatus.disabled) {
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
