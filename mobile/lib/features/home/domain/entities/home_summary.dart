import 'package:equatable/equatable.dart';

class HomeSummary extends Equatable {
  const HomeSummary({
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
  final CollectionProgress? progress;
  final List<RecentStamp> recentStamps;
  final NextReward? nextReward;
  final ActiveBanner? activeBanner;
  final List<PartnerBanner> promotionalBanners;
  final List<HomeMilestonePreview> milestones;
  final String? rankTitle;
  final String? rankSubtitle;
  final HomeSocialProof? socialProof;
  final List<String> partialErrors;

  @override
  List<Object?> get props => [
        displayName,
        lineId,
        lineName,
        progress,
        recentStamps,
        nextReward,
        activeBanner,
        promotionalBanners,
        milestones,
        rankTitle,
        rankSubtitle,
        socialProof,
        partialErrors,
      ];
}

class PartnerBanner extends Equatable {
  const PartnerBanner({
    required this.partnerId,
    required this.partnerName,
    this.logoUrl,
    required this.bannerImageUrl,
    this.contractStart,
    this.contractEnd,
  });

  final String partnerId;
  final String partnerName;
  final String? logoUrl;
  final String bannerImageUrl;
  final String? contractStart;
  final String? contractEnd;

  @override
  List<Object?> get props => [
        partnerId,
        partnerName,
        logoUrl,
        bannerImageUrl,
        contractStart,
        contractEnd,
      ];
}

class HomeMilestonePreview extends Equatable {
  const HomeMilestonePreview({
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

  @override
  List<Object?> get props => [
        id,
        requiredStampCount,
        label,
        rewardTitle,
        achieved,
      ];
}

class HomeSocialProof extends Equatable {
  const HomeSocialProof({
    required this.message,
    this.highlightCount,
  });

  final String message;
  final int? highlightCount;

  @override
  List<Object?> get props => [message, highlightCount];
}

class CollectionProgress extends Equatable {
  const CollectionProgress({
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

class RecentStamp extends Equatable {
  const RecentStamp({
    required this.stationId,
    required this.stationName,
    this.stampDesignUrl,
    required this.collectedAt,
    this.collectMethod,
  });

  final String stationId;
  final String stationName;
  final String? stampDesignUrl;
  final DateTime collectedAt;
  final String? collectMethod;

  @override
  List<Object?> get props => [
        stationId,
        stationName,
        stampDesignUrl,
        collectedAt,
        collectMethod,
      ];
}

class NextReward extends Equatable {
  const NextReward({
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

class ActiveBanner extends Equatable {
  const ActiveBanner({
    required this.campaignId,
    this.imageUrl,
    this.campaignName,
    this.promoLabel,
    this.subtitle,
  });

  final String campaignId;
  final String? imageUrl;
  final String? campaignName;
  final String? promoLabel;
  final String? subtitle;

  @override
  List<Object?> get props => [
        campaignId,
        imageUrl,
        campaignName,
        promoLabel,
        subtitle,
      ];
}
