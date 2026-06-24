import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/rewards/presentation/cubit/rewards_state.dart';
import 'package:metro_stamp_app/features/rewards/presentation/widgets/rewards_refresh_coordinator.dart';

void main() {
  const coordinator = RewardsRefreshCoordinator();

  group('RewardsRefreshCoordinator', () {
    test('initial mount without refresh token loads once', () {
      expect(
        coordinator.resolve(
          refreshToken: null,
          lastHandledToken: null,
          status: RewardsStatus.initial,
          isInitialMount: true,
        ),
        RewardsRefreshAction.load,
      );
    });

    test('new refresh token on loaded state refreshes', () {
      expect(
        coordinator.resolve(
          refreshToken: 'token-2',
          lastHandledToken: 'token-1',
          status: RewardsStatus.loaded,
          isInitialMount: false,
        ),
        RewardsRefreshAction.refresh,
      );
    });

    test('same refresh token does nothing', () {
      expect(
        coordinator.resolve(
          refreshToken: 'token-1',
          lastHandledToken: 'token-1',
          status: RewardsStatus.loaded,
          isInitialMount: false,
        ),
        RewardsRefreshAction.none,
      );
    });

    test('subsequent route sync without refresh token does nothing', () {
      expect(
        coordinator.resolve(
          refreshToken: null,
          lastHandledToken: null,
          status: RewardsStatus.loaded,
          isInitialMount: false,
        ),
        RewardsRefreshAction.none,
      );
    });
  });
}
