import 'package:equatable/equatable.dart';

enum MilestoneClaimStatus {
  locked,
  inProgress,
  claimable,
  claimed,
}

class Milestone extends Equatable {
  const Milestone({
    required this.id,
    required this.campaignId,
    required this.code,
    required this.name,
    required this.requiredStampCount,
    required this.rewardTitle,
    this.description,
    this.rewardType,
    this.rewardDescription,
    this.rewardImageUrl,
    this.claimStatus,
  });

  final String id;
  final String campaignId;
  final String code;
  final String name;
  final int requiredStampCount;
  final String rewardTitle;
  final String? description;
  final String? rewardType;
  final String? rewardDescription;
  final String? rewardImageUrl;
  final MilestoneClaimStatus? claimStatus;

  bool isAchieved(int collectedStampCount) {
    return collectedStampCount >= requiredStampCount;
  }

  @override
  List<Object?> get props => [
        id,
        campaignId,
        code,
        name,
        requiredStampCount,
        rewardTitle,
        description,
        rewardType,
        rewardDescription,
        rewardImageUrl,
        claimStatus,
      ];
}
