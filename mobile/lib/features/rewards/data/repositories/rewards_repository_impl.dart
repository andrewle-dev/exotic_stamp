import '../../../../core/errors/failure.dart';
import '../../domain/entities/milestone.dart';
import '../../domain/entities/rewards_overview.dart';
import '../../domain/entities/user_reward.dart';
import '../../domain/entities/voucher_detail.dart';
import '../../domain/repositories/rewards_repository.dart';
import '../datasources/rewards_remote_datasource.dart';
import '../models/milestone_model.dart';

class RewardsRepositoryImpl implements RewardsRepository {
  RewardsRepositoryImpl({required RewardsRemoteDataSource remoteDataSource})
      : _remoteDataSource = remoteDataSource;

  final RewardsRemoteDataSource _remoteDataSource;

  @override
  Future<RewardsOverview> getRewardsOverview() async {
    final partialErrors = <String>[];

    String? campaignId;
    String? campaignName;
    try {
      final campaign = await _remoteDataSource.getActiveCampaign();
      campaignId = campaign?['id'] as String?;
      campaignName = campaign?['name'] as String?;
    } on Failure catch (failure) {
      partialErrors.add(failure.message);
    }

    RewardsProgress? progress;
    String? lineId;
    try {
      final lines = await _remoteDataSource.getLines();
      if (lines.isNotEmpty) {
        final primaryLine = lines.firstWhere(
          (line) => line['status'] == 'ACTIVE',
          orElse: () => lines.first,
        );
        lineId = primaryLine['id'] as String?;
      }
    } on Failure catch (failure) {
      partialErrors.add(failure.message);
    }

    if (lineId != null && lineId.isNotEmpty) {
      try {
        final progressMap =
            await _remoteDataSource.getCollectionProgress(lineId: lineId);
        progress = RewardsProgress(
          lineId: progressMap['lineId'] as String? ?? lineId,
          collected: (progressMap['collected'] as num?)?.toInt() ?? 0,
          total: (progressMap['total'] as num?)?.toInt() ?? 0,
          percentage: (progressMap['percentage'] as num?)?.toInt() ?? 0,
        );
      } on Failure catch (failure) {
        partialErrors.add(failure.message);
      }
    }

    List<Milestone> milestones = const [];
    NextMilestoneHint? nextMilestone;
    if (campaignId != null && campaignId.isNotEmpty) {
      try {
        final milestoneModels = await _remoteDataSource.getMilestones(
          campaignId: campaignId,
        );
        milestones = milestoneModels.map((model) => model.toEntity()).toList();
        if (progress != null) {
          nextMilestone = _resolveNextMilestone(
            milestones: milestoneModels,
            collected: progress.collected,
          );
        }
      } on Failure catch (failure) {
        partialErrors.add(failure.message);
      }
    }

    final rewardModels = await _remoteDataSource.getMyRewards();
    final rewards = rewardModels.map((model) => model.toEntity()).toList();

    return RewardsOverview(
      campaignId: campaignId,
      campaignName: campaignName,
      progress: progress,
      milestones: milestones,
      rewards: rewards,
      nextMilestone: nextMilestone,
      partialErrors: partialErrors,
    );
  }

  @override
  Future<List<UserReward>> getMyRewards({
    String? status,
    int page = 0,
    int size = 50,
  }) async {
    final models = await _remoteDataSource.getMyRewards(
      status: status,
      page: page,
      size: size,
    );
    return models.map((model) => model.toEntity()).toList();
  }

  @override
  Future<List<Milestone>> getMilestones({
    required String campaignId,
    int page = 0,
    int size = 50,
  }) async {
    final models = await _remoteDataSource.getMilestones(
      campaignId: campaignId,
      page: page,
      size: size,
    );
    return models.map((model) => model.toEntity()).toList();
  }

  @override
  Future<VoucherDetail> getVoucherDetail({required String id}) async {
    final model = await _remoteDataSource.getVoucherDetail(id: id);
    return model.toEntity();
  }

  @override
  Future<VoucherDetail> redeemVoucher({required String id}) async {
    throw const Failure(
      code: FailureCode.redeemNotSupported,
      message: 'Đổi quà trực tuyến chưa được hỗ trợ. Vui lòng dùng mã đổi quà tại cửa hàng.',
    );
  }

  NextMilestoneHint? _resolveNextMilestone({
    required List<MilestoneModel> milestones,
    required int collected,
  }) {
    for (final milestone in milestones) {
      if (milestone.requiredStampCount > collected) {
        return NextMilestoneHint(
          milestoneId: milestone.id,
          requiredStampCount: milestone.requiredStampCount,
          rewardTitle: milestone.rewardTitle,
          stampsRemaining: milestone.requiredStampCount - collected,
        );
      }
    }
    return null;
  }
}
