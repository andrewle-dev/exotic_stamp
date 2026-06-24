import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/stamp_book/presentation/cubit/stamp_book_state.dart';
import 'package:metro_stamp_app/features/stamp_book/presentation/widgets/stamp_book_refresh_coordinator.dart';

void main() {
  const coordinator = StampBookRefreshCoordinator();

  group('StampBookRefreshCoordinator', () {
    test('initial mount without refresh token loads once', () {
      expect(
        coordinator.resolve(
          refreshToken: null,
          lastHandledToken: null,
          status: StampBookStatus.initial,
          isInitialMount: true,
        ),
        StampBookRefreshAction.load,
      );
    });

    test('new refresh token on loaded state refreshes', () {
      expect(
        coordinator.resolve(
          refreshToken: 'token-2',
          lastHandledToken: 'token-1',
          status: StampBookStatus.loaded,
          isInitialMount: false,
        ),
        StampBookRefreshAction.refresh,
      );
    });

    test('new refresh token on initial state loads', () {
      expect(
        coordinator.resolve(
          refreshToken: 'token-1',
          lastHandledToken: null,
          status: StampBookStatus.initial,
          isInitialMount: true,
        ),
        StampBookRefreshAction.load,
      );
    });

    test('same refresh token does nothing', () {
      expect(
        coordinator.resolve(
          refreshToken: 'token-1',
          lastHandledToken: 'token-1',
          status: StampBookStatus.loaded,
          isInitialMount: false,
        ),
        StampBookRefreshAction.none,
      );
    });

    test('subsequent mount without refresh token does nothing', () {
      expect(
        coordinator.resolve(
          refreshToken: null,
          lastHandledToken: null,
          status: StampBookStatus.loaded,
          isInitialMount: false,
        ),
        StampBookRefreshAction.none,
      );
    });
  });
}
