import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/entities/stamp_detail.dart';
import '../../domain/usecases/get_stamp_detail_usecase.dart';
import 'stamp_detail_state.dart';

class StampDetailCubit extends Cubit<StampDetailState> {
  StampDetailCubit({
    required GetStampDetailUseCase getStampDetailUseCase,
    required String stationId,
    String? lineId,
  })  : _getStampDetailUseCase = getStampDetailUseCase,
        _stationId = stationId,
        _lineId = lineId,
        super(const StampDetailState());

  final GetStampDetailUseCase _getStampDetailUseCase;
  final String _stationId;
  final String? _lineId;

  Future<void> load() async {
    emit(
      state.copyWith(
        status: StampDetailStatus.loading,
        clearFailure: true,
        clearDetail: true,
      ),
    );

    try {
      final detail = await _getStampDetailUseCase(
        stationId: _stationId,
        lineId: _lineId,
      );

      if (detail.availability == StampDetailAvailability.notFound) {
        emit(
          state.copyWith(
            status: StampDetailStatus.failure,
            detail: detail,
            failure: const Failure(
              code: FailureCode.unknown,
              message: 'Không tìm thấy stamp cho ga này.',
            ),
          ),
        );
        return;
      }

      emit(
        state.copyWith(
          status: StampDetailStatus.loaded,
          detail: detail,
          clearFailure: true,
        ),
      );
    } on Failure catch (failure) {
      emit(
        state.copyWith(
          status: StampDetailStatus.failure,
          failure: failure,
        ),
      );
    } catch (_) {
      emit(
        state.copyWith(
          status: StampDetailStatus.failure,
          failure: const Failure(
            code: FailureCode.unknown,
            message: 'Không thể tải chi tiết stamp.',
          ),
        ),
      );
    }
  }
}
