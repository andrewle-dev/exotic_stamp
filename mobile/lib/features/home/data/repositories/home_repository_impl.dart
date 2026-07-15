import '../../../../core/errors/failure.dart';
import '../../domain/entities/home_summary.dart';
import '../../domain/repositories/home_repository.dart';
import '../datasources/home_remote_datasource.dart';
import '../models/home_summary_model.dart';

class HomeRepositoryImpl implements HomeRepository {
  HomeRepositoryImpl({required HomeRemoteDataSource remoteDataSource})
      : _remoteDataSource = remoteDataSource;

  final HomeRemoteDataSource _remoteDataSource;

  @override
  Future<HomeSummary> getHomeSummary() async {
    final partialErrors = <String>[];

    final user = await _remoteDataSource.getMe();

    MetroLineModel? primaryLine;
    try {
      final lines = await _remoteDataSource.getLines();
      if (lines.isNotEmpty) {
        primaryLine = lines.firstWhere(
          (line) => line.status == 'ACTIVE',
          orElse: () => lines.first,
        );
      }
    } on Failure catch (failure) {
      partialErrors.add(failure.message);
    }

    ActiveBannerModel? banner;
    try {
      banner = await _remoteDataSource.getActiveCampaign();
    } on Failure catch (failure) {
      partialErrors.add(failure.message);
    }

    List<PartnerBannerModel> promotionalBanners = const [];
    try {
      promotionalBanners = await _remoteDataSource.getPromotionalBanners();
    } on Failure catch (failure) {
      partialErrors.add(failure.message);
    }

    CollectionProgressModel? progress;
    List<RecentStampModel> recentStamps = const [];
    NextRewardModel? nextReward;
    List<HomeMilestonePreviewModel> milestonePreviews = const [];
    String? rankTitle;
    String? rankSubtitle;

    final lineId = primaryLine?.id;
    if (lineId != null && lineId.isNotEmpty) {
      try {
        progress = await _remoteDataSource.getProgress(lineId: lineId);
      } on Failure catch (failure) {
        partialErrors.add(failure.message);
      }

      try {
        recentStamps = await _remoteDataSource.getRecentStamps(lineId: lineId);
      } on Failure catch (failure) {
        partialErrors.add(failure.message);
      }

      final campaignId = banner?.campaignId;
      if (campaignId != null && campaignId.isNotEmpty && progress != null) {
        try {
          final milestones = await _remoteDataSource.getMilestones(
            campaignId: campaignId,
          );
          nextReward = _resolveNextReward(
            milestones: milestones,
            collected: progress.collected,
          );
          milestonePreviews = _buildMilestonePreviews(
            milestones: milestones,
            collected: progress.collected,
          );
          rankTitle = _resolveRankTitle(
            milestones: milestones,
            collected: progress.collected,
          );
          rankSubtitle = _resolveRankSubtitle(nextReward: nextReward);
        } on Failure catch (failure) {
          partialErrors.add(failure.message);
        }
      }
    }

    return HomeSummaryModel(
      displayName: user.displayName,
      lineId: lineId,
      lineName: primaryLine?.displayName ?? primaryLine?.name,
      progress: progress,
      recentStamps: recentStamps,
      nextReward: nextReward,
      activeBanner: banner,
      promotionalBanners: promotionalBanners,
      milestones: milestonePreviews,
      rankTitle: rankTitle,
      rankSubtitle: rankSubtitle,
      partialErrors: partialErrors,
    ).toEntity();
  }

  List<HomeMilestonePreviewModel> _buildMilestonePreviews({
    required List<MilestoneModel> milestones,
    required int collected,
  }) {
    final sorted = [...milestones]
      ..sort((a, b) => a.requiredStampCount.compareTo(b.requiredStampCount));
    return sorted
        .map(
          (milestone) => HomeMilestonePreviewModel(
            id: milestone.id,
            requiredStampCount: milestone.requiredStampCount,
            label: '${milestone.requiredStampCount} Stamps',
            rewardTitle: milestone.rewardTitle,
            achieved: collected >= milestone.requiredStampCount,
          ),
        )
        .toList();
  }

  String? _resolveRankTitle({
    required List<MilestoneModel> milestones,
    required int collected,
  }) {
    final achieved = milestones
        .where((m) => m.requiredStampCount <= collected)
        .toList()
      ..sort((a, b) => b.requiredStampCount.compareTo(a.requiredStampCount));
    if (achieved.isEmpty) {
      return null;
    }
    return achieved.first.rewardTitle;
  }

  String? _resolveRankSubtitle({NextRewardModel? nextReward}) {
    if (nextReward == null) {
      return null;
    }
    return 'Còn ${nextReward.stampsRemaining} ga nữa để mở khóa ${nextReward.rewardTitle}';
  }

  NextRewardModel? _resolveNextReward({
    required List<MilestoneModel> milestones,
    required int collected,
  }) {
    for (final milestone in milestones) {
      if (milestone.requiredStampCount > collected) {
        return NextRewardModel(
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
