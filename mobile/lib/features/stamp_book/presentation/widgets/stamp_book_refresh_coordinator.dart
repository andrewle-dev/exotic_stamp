import '../cubit/stamp_book_state.dart';

enum StampBookRefreshAction {
  none,
  load,
  refresh,
}

/// Decides whether stamp book should load or refresh from route signals.
class StampBookRefreshCoordinator {
  const StampBookRefreshCoordinator();

  StampBookRefreshAction resolve({
    required String? refreshToken,
    required String? lastHandledToken,
    required StampBookStatus status,
    required bool isInitialMount,
  }) {
    if (refreshToken != null &&
        refreshToken.isNotEmpty &&
        refreshToken != lastHandledToken) {
      return status == StampBookStatus.initial
          ? StampBookRefreshAction.load
          : StampBookRefreshAction.refresh;
    }

    if (isInitialMount && status == StampBookStatus.initial) {
      return StampBookRefreshAction.load;
    }

    return StampBookRefreshAction.none;
  }
}
