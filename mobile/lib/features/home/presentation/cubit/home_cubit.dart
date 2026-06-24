import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/usecases/get_home_summary_usecase.dart';
import 'home_state.dart';

class HomeCubit extends Cubit<HomeState> {
  HomeCubit({required GetHomeSummaryUseCase getHomeSummaryUseCase})
      : _getHomeSummaryUseCase = getHomeSummaryUseCase,
        super(const HomeState());

  final GetHomeSummaryUseCase _getHomeSummaryUseCase;

  Future<void> load() async {
    emit(state.copyWith(status: HomeStatus.loading, clearFailure: true));
    try {
      final summary = await _getHomeSummaryUseCase();
      emit(
        state.copyWith(
          status: HomeStatus.loaded,
          summary: summary,
          clearFailure: true,
        ),
      );
    } on Failure catch (failure) {
      emit(
        state.copyWith(
          status: HomeStatus.failure,
          failure: failure,
          clearSummary: true,
        ),
      );
    } catch (_) {
      emit(
        state.copyWith(
          status: HomeStatus.failure,
          failure: const Failure(
            code: FailureCode.unknown,
            message: 'Không thể tải trang chủ.',
          ),
          clearSummary: true,
        ),
      );
    }
  }
}
