import '../../domain/entities/home_summary.dart';

class HomeSummaryModel {
  HomeSummaryModel({
    required this.displayName,
    this.lineId,
    this.lineName,
    this.progress,
    this.recentStamps = const [],
    this.nextReward,
    this.activeBanner,
    this.partialErrors = const [],
  });

  final String displayName;
  final String? lineId;
  final String? lineName;
  final CollectionProgressModel? progress;
  final List<RecentStampModel> recentStamps;
  final NextRewardModel? nextReward;
  final ActiveBannerModel? activeBanner;
  final List<String> partialErrors;

  HomeSummary toEntity() {
    return HomeSummary(
      displayName: displayName,
      lineId: lineId,
      lineName: lineName,
      progress: progress?.toEntity(),
      recentStamps: recentStamps.map((stamp) => stamp.toEntity()).toList(),
      nextReward: nextReward?.toEntity(),
      activeBanner: activeBanner?.toEntity(),
      partialErrors: partialErrors,
    );
  }
}

class CollectionProgressModel {
  CollectionProgressModel({
    required this.lineId,
    required this.collected,
    required this.total,
    required this.percentage,
  });

  factory CollectionProgressModel.fromJson(Map<String, dynamic> json) {
    return CollectionProgressModel(
      lineId: json['lineId'] as String? ?? '',
      collected: (json['collected'] as num?)?.toInt() ?? 0,
      total: (json['total'] as num?)?.toInt() ?? 0,
      percentage: (json['percentage'] as num?)?.toInt() ?? 0,
    );
  }

  final String lineId;
  final int collected;
  final int total;
  final int percentage;

  CollectionProgress toEntity() {
    return CollectionProgress(
      lineId: lineId,
      collected: collected,
      total: total,
      percentage: percentage,
    );
  }
}

class RecentStampModel {
  RecentStampModel({
    required this.stationId,
    required this.stationName,
    this.stampDesignUrl,
    required this.collectedAt,
    this.collectMethod,
  });

  factory RecentStampModel.fromJson(Map<String, dynamic> json) {
    return RecentStampModel(
      stationId: json['stationId'] as String? ?? '',
      stationName: json['stationName'] as String? ?? 'Station',
      stampDesignUrl: json['stampDesignUrl'] as String?,
      collectedAt: DateTime.tryParse(json['collectedAt'] as String? ?? '') ??
          DateTime.fromMillisecondsSinceEpoch(0),
      collectMethod: json['collectMethod'] as String?,
    );
  }

  final String stationId;
  final String stationName;
  final String? stampDesignUrl;
  final DateTime collectedAt;
  final String? collectMethod;

  RecentStamp toEntity() {
    return RecentStamp(
      stationId: stationId,
      stationName: stationName,
      stampDesignUrl: stampDesignUrl,
      collectedAt: collectedAt,
      collectMethod: collectMethod,
    );
  }
}

class NextRewardModel {
  NextRewardModel({
    required this.milestoneId,
    required this.requiredStampCount,
    required this.rewardTitle,
    required this.stampsRemaining,
  });

  final String milestoneId;
  final int requiredStampCount;
  final String rewardTitle;
  final int stampsRemaining;

  NextReward toEntity() {
    return NextReward(
      milestoneId: milestoneId,
      requiredStampCount: requiredStampCount,
      rewardTitle: rewardTitle,
      stampsRemaining: stampsRemaining,
    );
  }
}

class ActiveBannerModel {
  ActiveBannerModel({
    required this.campaignId,
    this.imageUrl,
    this.campaignName,
  });

  factory ActiveBannerModel.fromCampaignJson(Map<String, dynamic> json) {
    return ActiveBannerModel(
      campaignId: json['id'] as String? ?? '',
      imageUrl: json['bannerImageUrl'] as String?,
      campaignName: json['name'] as String?,
    );
  }

  final String campaignId;
  final String? imageUrl;
  final String? campaignName;

  ActiveBanner toEntity() {
    return ActiveBanner(
      campaignId: campaignId,
      imageUrl: imageUrl,
      campaignName: campaignName,
    );
  }
}

class MilestoneModel {
  MilestoneModel({
    required this.id,
    required this.requiredStampCount,
    required this.rewardTitle,
  });

  factory MilestoneModel.fromJson(Map<String, dynamic> json) {
    return MilestoneModel(
      id: json['id'] as String? ?? '',
      requiredStampCount: (json['requiredStampCount'] as num?)?.toInt() ?? 0,
      rewardTitle: json['rewardTitle'] as String? ??
          json['milestoneName'] as String? ??
          'Reward',
    );
  }

  final String id;
  final int requiredStampCount;
  final String rewardTitle;
}

class MetroLineModel {
  MetroLineModel({
    required this.id,
    required this.name,
    this.displayName,
    this.status,
  });

  factory MetroLineModel.fromJson(Map<String, dynamic> json) {
    return MetroLineModel(
      id: json['id'] as String? ?? '',
      name: json['name'] as String? ?? '',
      displayName: json['displayName'] as String?,
      status: json['status'] as String?,
    );
  }

  final String id;
  final String name;
  final String? displayName;
  final String? status;
}

class UserProfileModel {
  UserProfileModel({
    required this.firstname,
    required this.lastname,
  });

  factory UserProfileModel.fromJson(Map<String, dynamic> json) {
    return UserProfileModel(
      firstname: json['firstname'] as String? ?? '',
      lastname: json['lastname'] as String? ?? '',
    );
  }

  String get displayName {
    final full = '${firstname.trim()} ${lastname.trim()}'.trim();
    return full.isEmpty ? 'Collector' : full;
  }

  final String firstname;
  final String lastname;
}
