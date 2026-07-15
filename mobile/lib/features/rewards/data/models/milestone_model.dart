import '../../domain/entities/milestone.dart';

class MilestoneModel {
  MilestoneModel({
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

  factory MilestoneModel.fromJson(Map<String, dynamic> json) {
    return MilestoneModel(
      id: json['id'] as String? ?? '',
      campaignId: json['campaignId'] as String? ?? '',
      code: json['code'] as String? ?? '',
      name: json['name'] as String? ??
          json['milestoneName'] as String? ??
          'Milestone',
      requiredStampCount: (json['requiredStampCount'] as num?)?.toInt() ?? 0,
      rewardTitle: json['rewardTitle'] as String? ?? 'Reward',
      description: json['description'] as String?,
      rewardType: json['rewardType'] as String?,
      rewardDescription: json['rewardDescription'] as String?,
      rewardImageUrl: json['rewardImageUrl'] as String?,
      claimStatus: _parseClaimStatus(json['claimStatus'] as String?),
    );
  }

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

  Milestone toEntity() {
    return Milestone(
      id: id,
      campaignId: campaignId,
      code: code,
      name: name,
      requiredStampCount: requiredStampCount,
      rewardTitle: rewardTitle,
      description: description,
      rewardType: rewardType,
      rewardDescription: rewardDescription,
      rewardImageUrl: rewardImageUrl,
      claimStatus: claimStatus,
    );
  }
}

MilestoneClaimStatus? _parseClaimStatus(String? raw) {
  switch (raw?.toUpperCase()) {
    case 'LOCKED':
      return MilestoneClaimStatus.locked;
    case 'IN_PROGRESS':
      return MilestoneClaimStatus.inProgress;
    case 'CLAIMABLE':
      return MilestoneClaimStatus.claimable;
    case 'CLAIMED':
      return MilestoneClaimStatus.claimed;
    default:
      return null;
  }
}
