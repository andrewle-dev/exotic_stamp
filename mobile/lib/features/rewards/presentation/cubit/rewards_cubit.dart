import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/entities/rewards_overview.dart';
import '../../domain/usecases/get_rewards_overview_usecase.dart';
import 'rewards_state.dart';

class RewardsCubit extends Cubit<RewardsState> {
  RewardsCubit({required GetRewardsOverviewUseCase getRewardsOverviewUseCase})
      : _getRewardsOverviewUseCase = getRewardsOverviewUseCase,
        super(const RewardsState());

  final GetRewardsOverviewUseCase _getRewardsOverviewUseCase;

  Future<void> load() async {
    emit(
      state.copyWith(
        status: RewardsStatus.loading,
        clearFailure: true,
        isRefreshing: false,
      ),
    );
    await _fetchOverview();
  }

  Future<void> refresh() async {
    emit(state.copyWith(isRefreshing: true, clearFailure: true));
    await _fetchOverview(isRefresh: true);
  }

  Future<void> _fetchOverview({bool isRefresh = false}) async {
    try {
      final overview = await _getRewardsOverviewUseCase();
      emit(
        _loadedState(
          overview: overview,
          isRefreshing: false,
        ),
      );
    } on Failure catch (failure) {
      emit(
        state.copyWith(
          status: RewardsStatus.failure,
          failure: failure,
          isRefreshing: false,
          clearOverview: !isRefresh,
        ),
      );
    } catch (_) {
      emit(
        state.copyWith(
          status: RewardsStatus.failure,
          failure: const Failure(
            code: FailureCode.unknown,
            message: 'Không thể tải phần thưởng.',
          ),
          isRefreshing: false,
          clearOverview: !isRefresh,
        ),
      );
    }
  }

  RewardsState _loadedState({
    required RewardsOverview overview,
    required bool isRefreshing,
  }) {
    final RewardsStatus status;
    if (overview.hasPendingRewards) {
      status = RewardsStatus.rewardPending;
    } else if (overview.hasUserRewards || overview.availableVouchers.isNotEmpty) {
      status = RewardsStatus.loaded;
    } else {
      status = RewardsStatus.noRewardsYet;
    }

    return RewardsState(
      status: status,
      overview: overview,
      isRefreshing: isRefreshing,
    );
  }
}
