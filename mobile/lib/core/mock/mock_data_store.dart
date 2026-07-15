import '../../features/rewards/domain/entities/user_reward.dart';
import 'mock_fixtures.dart';

/// In-memory mock state isolated from production repositories.
///
/// Mutations (collect / redeem) only occur when [MockConfig.allowMockWrites]
/// is true via mock repository implementations.
class MockDataStore {
  MockDataStore._();

  static final MockDataStore instance = MockDataStore._();

  final Set<String> _collectedStationIds = {...MockFixtures.initialCollectedStationIds};
  final Map<String, UserRewardStatus> _rewardStatuses = {
    'reward-1': UserRewardStatus.used,
    'reward-2': UserRewardStatus.available,
  };

  Set<String> get collectedStationIds => Set.unmodifiable(_collectedStationIds);

  void reset() {
    _collectedStationIds
      ..clear()
      ..addAll(MockFixtures.initialCollectedStationIds);
    _rewardStatuses
      ..clear()
      ..addAll({
        'reward-1': UserRewardStatus.used,
        'reward-2': UserRewardStatus.available,
      });
  }

  bool isStationCollected(String stationId) =>
      _collectedStationIds.contains(stationId);

  UserRewardStatus rewardStatus(String rewardId) =>
      _rewardStatuses[rewardId] ?? UserRewardStatus.unavailable;

  /// Mock-only: marks a station collected after simulated NFC collect.
  bool markStationCollected(String stationId) {
    return _collectedStationIds.add(stationId);
  }

  /// Mock-only: simulates voucher redeem UI state.
  UserRewardStatus? markRewardRedeemed(String rewardId) {
    final current = _rewardStatuses[rewardId];
    if (current != UserRewardStatus.available) {
      return null;
    }
    _rewardStatuses[rewardId] = UserRewardStatus.used;
    return UserRewardStatus.used;
  }

  int get collectedCount => _collectedStationIds.length;
}
