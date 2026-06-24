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
    this.partialErrors = const [],
  });

  final String displayName;
  final String? lineId;
  final String? lineName;
  final CollectionProgress? progress;
  final List<RecentStamp> recentStamps;
  final NextReward? nextReward;
  final ActiveBanner? activeBanner;
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
        partialErrors,
      ];
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
  });

  final String campaignId;
  final String? imageUrl;
  final String? campaignName;

  @override
  List<Object?> get props => [campaignId, imageUrl, campaignName];
}
