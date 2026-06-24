import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/rewards/data/models/user_reward_model.dart';
import 'package:metro_stamp_app/features/rewards/domain/entities/user_reward.dart';

void main() {
  test('maps ISSUED to available status', () {
    final model = UserRewardModel.fromJson({
      'id': 'reward-1',
      'campaignId': 'campaign-1',
      'milestoneId': 'milestone-1',
      'rewardTitle': 'Coffee',
      'status': 'ISSUED',
    });

    expect(model.status, UserRewardStatus.available);
  });

  test('maps REDEEMED to used status', () {
    expect(
      mapUserRewardStatus('REDEEMED'),
      UserRewardStatus.used,
    );
  });

  test('maps PENDING_STOCK to pending status', () {
    expect(
      mapUserRewardStatus('PENDING_STOCK'),
      UserRewardStatus.pending,
    );
  });
}
