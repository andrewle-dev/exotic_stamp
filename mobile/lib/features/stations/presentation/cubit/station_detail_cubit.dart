import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/usecases/get_station_detail_usecase.dart';
import 'station_detail_state.dart';

class StationDetailCubit extends Cubit<StationDetailState> {
  StationDetailCubit({
    required GetStationDetailUseCase getStationDetailUseCase,
    required String stationId,
  })  : _getStationDetailUseCase = getStationDetailUseCase,
        _stationId = stationId,
        super(const StationDetailState());

  final GetStationDetailUseCase _getStationDetailUseCase;
  final String _stationId;

  Future<void> load() async {
    emit(state.copyWith(
        status: StationDetailStatus.loading, clearFailure: true));
    try {
      final detail = await _getStationDetailUseCase(_stationId);
      if (!detail.isActive) {
        emit(
          state.copyWith(
            status: StationDetailStatus.inactive,
            detail: detail,
            clearFailure: true,
          ),
        );
        return;
      }
      emit(
        state.copyWith(
          status: StationDetailStatus.loaded,
          detail: detail,
          clearFailure: true,
        ),
      );
    } on Failure catch (failure) {
      final status = failure.statusCode == 404
          ? StationDetailStatus.notFound
          : StationDetailStatus.failure;
      emit(
        state.copyWith(
          status: status,
          failure: failure,
          clearDetail: true,
        ),
      );
    } catch (_) {
      emit(
        state.copyWith(
          status: StationDetailStatus.failure,
          failure: const Failure(
            code: FailureCode.unknown,
            message: 'Không thể tải chi tiết ga.',
          ),
          clearDetail: true,
        ),
      );
    }
  }
}
