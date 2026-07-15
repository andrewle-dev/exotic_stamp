import 'package:equatable/equatable.dart';

import 'milestone.dart';
import 'user_reward.dart';

class RewardsProgress extends Equatable {
  const RewardsProgress({
    required this.lineId,
    required this.collected,
    required this.total,
    required this.percentage,
  });

  final String lineId;
  final int collected;
  final int total;
  final int percentage;

  @override
  List<Object?> get props => [lineId, collected, total, percentage];
}

class NextMilestoneHint extends Equatable {
  const NextMilestoneHint({
    required this.milestoneId,
    required this.requiredStampCount,
    required this.rewardTitle,
    required this.stampsRemaining,
  });

  final String milestoneId;
  final int requiredStampCount;
  final String rewardTitle;
  final int stampsRemaining;

  @override
  List<Object?> get props => [
        milestoneId,
        requiredStampCount,
        rewardTitle,
        stampsRemaining,
      ];
}

class RewardsOverview extends Equatable {
  const RewardsOverview({
    this.campaignId,
    this.campaignName,
    this.progress,
    this.milestones = const [],
    this.rewards = const [],
    this.nextMilestone,
    this.rankTitle,
    this.partialErrors = const [],
  });

  final String? campaignId;
  final String? campaignName;
  final RewardsProgress? progress;
  final List<Milestone> milestones;
  final List<UserReward> rewards;
  final NextMilestoneHint? nextMilestone;
  final String? rankTitle;
  final List<String> partialErrors;

  bool get hasUserRewards => rewards.isNotEmpty;

  bool get hasMilestones => milestones.isNotEmpty;

  bool get hasPendingRewards =>
      rewards.any((reward) => reward.status == UserRewardStatus.pending);

  List<UserReward> get availableVouchers =>
      rewards.where((reward) => reward.status == UserRewardStatus.available).toList();

  List<UserReward> get historyVouchers => rewards
      .where(
        (reward) =>
            reward.status == UserRewardStatus.used ||
            reward.status == UserRewardStatus.expired ||
            reward.status == UserRewardStatus.unavailable,
      )
      .toList();

  @override
  List<Object?> get props => [
        campaignId,
        campaignName,
        progress,
        milestones,
        rewards,
        nextMilestone,
        rankTitle,
        partialErrors,
      ];
}
