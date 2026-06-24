import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/entities/line.dart';
import '../../domain/usecases/get_lines_usecase.dart';
import '../../domain/usecases/get_stations_usecase.dart';
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
      final selectedLineId = _resolveDefaultLineId(lines);
      final stations = await _getStationsUseCase(
        lineId: selectedLineId,
      );
      emit(
        state.copyWith(
          status: StationsStatus.loaded,
          lines: lines,
          selectedLineId: selectedLineId,
          stations: stations,
          clearFailure: true,
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

  Future<void> _reloadStations() async {
    try {
      final stations = await _getStationsUseCase(
        lineId: state.selectedLineId,
        searchQuery: state.searchQuery,
      );
      emit(
        state.copyWith(
          status: StationsStatus.loaded,
          stations: stations,
          clearFailure: true,
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

  String? _resolveDefaultLineId(List<Line> lines) {
    if (lines.isEmpty) {
      return null;
    }
    for (final line in lines) {
      if (line.status == 'ACTIVE') {
        return line.id;
      }
    }
    return lines.first.id;
  }
}
