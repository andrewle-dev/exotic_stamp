import '../entities/milestone.dart';
import '../entities/rewards_overview.dart';
import '../entities/user_reward.dart';
import '../entities/voucher_detail.dart';

abstract class RewardsRepository {
  Future<RewardsOverview> getRewardsOverview();

  Future<List<UserReward>> getMyRewards({
    String? status,
    int page = 0,
    int size = 50,
  });

  Future<List<Milestone>> getMilestones({
    required String campaignId,
    int page = 0,
    int size = 50,
  });

  Future<VoucherDetail> getVoucherDetail({required String id});
}
