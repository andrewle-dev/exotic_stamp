import '../../../core/config/mock_config.dart';
import '../../../core/errors/failure.dart';
import '../../../features/rewards/domain/entities/milestone.dart';
import '../../../features/rewards/domain/entities/rewards_overview.dart';
import '../../../features/rewards/domain/entities/user_reward.dart';
import '../../../features/rewards/domain/entities/voucher_detail.dart';
import '../../../features/rewards/domain/repositories/rewards_repository.dart';
import '../mock_data_store.dart';
import '../mock_fixtures.dart';

/// Mock [RewardsRepository] — UI development only.
class MockRewardsRepository implements RewardsRepository {
  MockRewardsRepository({MockDataStore? store})
      : _store = store ?? MockDataStore.instance;

  final MockDataStore _store;

  @override
  Future<RewardsOverview> getRewardsOverview() async {
    await Future<void>.delayed(const Duration(milliseconds: 200));
    final overview = MockFixtures.rewardsOverview(_store.collectedStationIds);
    final rewards = overview.rewards
        .map(
          (reward) => UserReward(
            id: reward.id,
            campaignId: reward.campaignId,
            milestoneId: reward.milestoneId,
            milestoneCode: reward.milestoneCode,
            milestoneName: reward.milestoneName,
            rewardType: reward.rewardType,
            rewardTitle: reward.rewardTitle,
            rewardDescription: reward.rewardDescription,
            rewardImageUrl: reward.rewardImageUrl,
            partnerName: reward.partnerName,
            offerTitle: reward.offerTitle,
            isFavorite: reward.isFavorite,
            issuedAt: reward.issuedAt,
            expiresAt: reward.expiresAt,
            redeemedAt: reward.redeemedAt,
            status: _store.rewardStatus(reward.id),
            voucher: reward.voucher,
          ),
        )
        .toList();
    return RewardsOverview(
      campaignId: overview.campaignId,
      campaignName: overview.campaignName,
      rankTitle: overview.rankTitle,
      progress: overview.progress,
      milestones: overview.milestones,
      rewards: rewards,
      nextMilestone: overview.nextMilestone,
    );
  }

  @override
  Future<List<UserReward>> getMyRewards({
    String? status,
    int page = 0,
    int size = 50,
  }) async {
    final overview = await getRewardsOverview();
    return overview.rewards;
  }

  @override
  Future<List<Milestone>> getMilestones({
    required String campaignId,
    int page = 0,
    int size = 50,
  }) async {
    return MockFixtures.milestones();
  }

  @override
  Future<VoucherDetail> getVoucherDetail({required String id}) async {
    await Future<void>.delayed(const Duration(milliseconds: 150));
    final detail = MockFixtures.voucherDetail(id);
    final status = _store.rewardStatus(id);
    return VoucherDetail(
      id: detail.id,
      rewardTitle: detail.rewardTitle,
      milestoneName: detail.milestoneName,
      rewardDescription: detail.rewardDescription,
      rewardImageUrl: detail.rewardImageUrl,
      issuedAt: detail.issuedAt,
      expiresAt: detail.expiresAt,
      redeemedAt: status == UserRewardStatus.used ? DateTime.now() : null,
      status: status,
      voucherCode:
          status == UserRewardStatus.available ? detail.voucherCode : null,
      partnerName: detail.partnerName,
      offerTitle: detail.offerTitle,
      unlockCondition: detail.unlockCondition,
      terms: detail.terms,
      relatedVouchers: detail.relatedVouchers,
    );
  }

  /// Mock-only redeem confirmed by repository response.
  @override
  Future<VoucherDetail> redeemVoucher({required String id}) async {
    if (!MockConfig.allowMockWrites) {
      throw const Failure(
        code: FailureCode.redeemNotSupported,
        message: 'Redeem simulation is disabled outside mock mode.',
      );
    }
    await Future<void>.delayed(const Duration(milliseconds: 300));
    final status = _store.markRewardRedeemed(id);
    if (status == null) {
      throw const Failure(
        code: FailureCode.unknown,
        message: 'Không thể đổi quà voucher.',
      );
    }
    final detail = await getVoucherDetail(id: id);
    return detail.copyWithStatus(status);
  }

  /// Mock-only redeem simulation. No-op outside mock write mode.
  @Deprecated('Use redeemVoucher')
  Future<UserRewardStatus?> simulateRedeem(String rewardId) async {
    if (!MockConfig.allowMockWrites) {
      throw const Failure(
        code: FailureCode.redeemNotSupported,
        message: 'Redeem simulation is disabled outside mock mode.',
      );
    }
    await Future<void>.delayed(const Duration(milliseconds: 300));
    return _store.markRewardRedeemed(rewardId);
  }
}
