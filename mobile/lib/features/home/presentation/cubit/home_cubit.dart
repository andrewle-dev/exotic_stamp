import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/usecases/get_home_summary_usecase.dart';
import 'home_state.dart';

class HomeCubit extends Cubit<HomeState> {
  HomeCubit({required GetHomeSummaryUseCase getHomeSummaryUseCase})
      : _getHomeSummaryUseCase = getHomeSummaryUseCase,
        super(const HomeState());

  final GetHomeSummaryUseCase _getHomeSummaryUseCase;
  int _requestSeq = 0;

  /// Full-screen loading reload (initial open / pull-to-refresh / retry).
  Future<void> load() => _fetch(silent: false);

  /// Keeps the current summary visible while refetching (tab return).
  Future<void> refresh() => _fetch(silent: true);

  Future<void> _fetch({required bool silent}) async {
    final requestId = ++_requestSeq;
    if (!silent || state.summary == null) {
      emit(state.copyWith(status: HomeStatus.loading, clearFailure: true));
    } else {
      emit(state.copyWith(isRefreshing: true, clearFailure: true));
    }

    try {
      final summary = await _getHomeSummaryUseCase();
      if (requestId != _requestSeq || isClosed) {
        return;
      }
      emit(
        state.copyWith(
          status: HomeStatus.loaded,
          summary: summary,
          isRefreshing: false,
          clearFailure: true,
        ),
      );
    } on Failure catch (failure) {
      if (requestId != _requestSeq || isClosed) {
        return;
      }
      if (silent && state.summary != null) {
        emit(
          state.copyWith(
            status: HomeStatus.loaded,
            isRefreshing: false,
            failure: failure,
          ),
        );
        return;
      }
      emit(
        state.copyWith(
          status: HomeStatus.failure,
          failure: failure,
          isRefreshing: false,
          clearSummary: true,
        ),
      );
    } catch (_) {
      if (requestId != _requestSeq || isClosed) {
        return;
      }
      const failure = Failure(
        code: FailureCode.unknown,
        message: 'Không thể tải trang chủ.',
      );
      if (silent && state.summary != null) {
        emit(
          state.copyWith(
            status: HomeStatus.loaded,
            isRefreshing: false,
            failure: failure,
          ),
        );
        return;
      }
      emit(
        state.copyWith(
          status: HomeStatus.failure,
          failure: failure,
          isRefreshing: false,
          clearSummary: true,
        ),
      );
    }
  }
}
