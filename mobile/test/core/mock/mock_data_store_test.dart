import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/mock/mock_data_store.dart';
import 'package:metro_stamp_app/core/mock/mock_fixtures.dart';
import 'package:metro_stamp_app/features/rewards/domain/entities/user_reward.dart';

void main() {
  setUp(MockDataStore.instance.reset);

  test('starts with fixture collected stations', () {
    expect(
      MockDataStore.instance.collectedStationIds,
      MockFixtures.initialCollectedStationIds,
    );
  });

  test('markStationCollected updates collected state', () {
    final store = MockDataStore.instance;
    expect(store.isStationCollected('station-thao-dien'), isFalse);

    final isNew = store.markStationCollected('station-thao-dien');
    expect(isNew, isTrue);
    expect(store.isStationCollected('station-thao-dien'), isTrue);
    expect(store.collectedCount, 6);

    final duplicate = store.markStationCollected('station-thao-dien');
    expect(duplicate, isFalse);
  });

  test('markRewardRedeemed only when available', () {
    final store = MockDataStore.instance;
    expect(store.rewardStatus('reward-2'), UserRewardStatus.available);

    final result = store.markRewardRedeemed('reward-2');
    expect(result, UserRewardStatus.used);
    expect(store.rewardStatus('reward-2'), UserRewardStatus.used);

    expect(store.markRewardRedeemed('reward-2'), isNull);
  });

  test('reset restores initial fixture state', () {
    MockDataStore.instance.markStationCollected('station-thao-dien');
    MockDataStore.instance.reset();
    expect(
      MockDataStore.instance.collectedStationIds.length,
      MockFixtures.initialCollectedStationIds.length,
    );
  });
}
