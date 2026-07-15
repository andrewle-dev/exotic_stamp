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
    this.promotionalBanners = const [],
    this.milestones = const [],
    this.rankTitle,
    this.rankSubtitle,
    this.socialProof,
    this.partialErrors = const [],
  });

  final String displayName;
  final String? lineId;
  final String? lineName;
  final CollectionProgressModel? progress;
  final List<RecentStampModel> recentStamps;
  final NextRewardModel? nextReward;
  final ActiveBannerModel? activeBanner;
  final List<PartnerBannerModel> promotionalBanners;
  final List<HomeMilestonePreviewModel> milestones;
  final String? rankTitle;
  final String? rankSubtitle;
  final HomeSocialProofModel? socialProof;
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
      promotionalBanners:
          promotionalBanners.map((banner) => banner.toEntity()).toList(),
      milestones: milestones.map((m) => m.toEntity()).toList(),
      rankTitle: rankTitle,
      rankSubtitle: rankSubtitle,
      socialProof: socialProof?.toEntity(),
      partialErrors: partialErrors,
    );
  }
}

class PartnerBannerModel {
  PartnerBannerModel({
    required this.partnerId,
    required this.partnerName,
    this.logoUrl,
    required this.bannerImageUrl,
    this.contractStart,
    this.contractEnd,
  });

  factory PartnerBannerModel.fromJson(Map<String, dynamic> json) {
    return PartnerBannerModel(
      partnerId: json['partnerId'] as String? ?? '',
      partnerName: json['partnerName'] as String? ?? '',
      logoUrl: json['logoUrl'] as String?,
      bannerImageUrl: json['bannerImageUrl'] as String? ?? '',
      contractStart: json['contractStart'] as String?,
      contractEnd: json['contractEnd'] as String?,
    );
  }

  final String partnerId;
  final String partnerName;
  final String? logoUrl;
  final String bannerImageUrl;
  final String? contractStart;
  final String? contractEnd;

  PartnerBanner toEntity() {
    return PartnerBanner(
      partnerId: partnerId,
      partnerName: partnerName,
      logoUrl: logoUrl,
      bannerImageUrl: bannerImageUrl,
      contractStart: contractStart,
      contractEnd: contractEnd,
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

  /// Parses progress from `GET /collection/progress`.
  ///
  /// Required fields: `collected`, `total`. Missing/null values throw
  /// [FormatException] so Home does not silently render misleading `0/0`.
  factory CollectionProgressModel.fromJson(Map<String, dynamic> json) {
    final collected = _requireNonNegativeInt(json, 'collected');
    final total = _requireNonNegativeInt(json, 'total');
    final percentageRaw = json['percentage'];
    final percentage = percentageRaw == null
        ? (total <= 0 ? 0 : ((collected * 100) / total).floor().clamp(0, 100))
        : _requireNonNegativeInt(json, 'percentage').clamp(0, 100);
    final lineId = json['lineId'];
    return CollectionProgressModel(
      lineId: lineId == null ? '' : lineId.toString(),
      collected: collected,
      total: total,
      percentage: percentage,
    );
  }

  static int _requireNonNegativeInt(Map<String, dynamic> json, String key) {
    final value = json[key];
    if (value is! num) {
      throw FormatException(
        'Home progress field "$key" is required and must be a number '
        '(got ${value.runtimeType}).',
      );
    }
    final asInt = value.toInt();
    if (asInt < 0) {
      throw FormatException(
        'Home progress field "$key" must be >= 0 (got $asInt).',
      );
    }
    return asInt;
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
    this.promoLabel,
    this.subtitle,
  });

  factory ActiveBannerModel.fromCampaignJson(Map<String, dynamic> json) {
    return ActiveBannerModel(
      campaignId: json['id'] as String? ?? '',
      imageUrl: json['bannerImageUrl'] as String?,
      campaignName: json['name'] as String?,
      promoLabel: json['promoLabel'] as String?,
      subtitle: json['subtitle'] as String?,
    );
  }

  final String campaignId;
  final String? imageUrl;
  final String? campaignName;
  final String? promoLabel;
  final String? subtitle;

  ActiveBanner toEntity() {
    return ActiveBanner(
      campaignId: campaignId,
      imageUrl: imageUrl,
      campaignName: campaignName,
      promoLabel: promoLabel,
      subtitle: subtitle,
    );
  }
}

class HomeMilestonePreviewModel {
  HomeMilestonePreviewModel({
    required this.id,
    required this.requiredStampCount,
    required this.label,
    this.rewardTitle,
    required this.achieved,
  });

  final String id;
  final int requiredStampCount;
  final String label;
  final String? rewardTitle;
  final bool achieved;

  HomeMilestonePreview toEntity() {
    return HomeMilestonePreview(
      id: id,
      requiredStampCount: requiredStampCount,
      label: label,
      rewardTitle: rewardTitle,
      achieved: achieved,
    );
  }
}

class HomeSocialProofModel {
  HomeSocialProofModel({
    required this.message,
    this.highlightCount,
  });

  final String message;
  final int? highlightCount;

  HomeSocialProof toEntity() {
    return HomeSocialProof(
      message: message,
      highlightCount: highlightCount,
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
