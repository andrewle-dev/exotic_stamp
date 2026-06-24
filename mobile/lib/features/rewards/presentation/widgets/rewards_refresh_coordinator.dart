import '../cubit/rewards_state.dart';

enum RewardsRefreshAction {
  none,
  load,
  refresh,
}

/// Decides whether rewards should load or refresh from route signals.
class RewardsRefreshCoordinator {
  const RewardsRefreshCoordinator();

  RewardsRefreshAction resolve({
    required String? refreshToken,
    required String? lastHandledToken,
    required RewardsStatus status,
    required bool isInitialMount,
  }) {
    if (refreshToken != null &&
        refreshToken.isNotEmpty &&
        refreshToken != lastHandledToken) {
      return status == RewardsStatus.initial
          ? RewardsRefreshAction.load
          : RewardsRefreshAction.refresh;
    }

    if (isInitialMount && status == RewardsStatus.initial) {
      return RewardsRefreshAction.load;
    }

    return RewardsRefreshAction.none;
  }
}
