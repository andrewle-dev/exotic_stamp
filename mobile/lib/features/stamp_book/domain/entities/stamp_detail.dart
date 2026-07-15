import 'package:equatable/equatable.dart';

class StampCollectionProgress extends Equatable {
  const StampCollectionProgress({
    required this.collectionName,
    required this.collected,
    required this.total,
    this.nextRewardHint,
  });

  final String collectionName;
  final int collected;
  final int total;
  final String? nextRewardHint;

  @override
  List<Object?> get props => [collectionName, collected, total, nextRewardHint];
}

enum StampDetailAvailability {
  full,
  limited,
  notFound,
}

class StampDetail extends Equatable {
  const StampDetail({
    required this.stationId,
    required this.stationName,
    required this.collected,
    this.lineId,
    this.lineName,
    this.campaignName,
    this.stampDesignUrl,
    this.stationImageUrl,
    this.stampDesignName,
    this.stampDesignDescription,
    this.rarity,
    this.collectedAt,
    this.stampId,
    this.collectMethod,
    this.serialNumber,
    this.stationStory,
    this.collectionProgress,
    this.availability = StampDetailAvailability.full,
  });

  final String stationId;
  final String stationName;
  final bool collected;
  final String? lineId;
  final String? lineName;
  final String? campaignName;
  final String? stampDesignUrl;
  final String? stationImageUrl;
  final String? stampDesignName;
  final String? stampDesignDescription;
  final String? rarity;
  final DateTime? collectedAt;
  final String? stampId;
  final String? collectMethod;
  final String? serialNumber;
  final String? stationStory;
  final StampCollectionProgress? collectionProgress;
  final StampDetailAvailability availability;

  bool get nfcVerified =>
      collectMethod != null && collectMethod!.toUpperCase().contains('NFC');

  @override
  List<Object?> get props => [
        stationId,
        stationName,
        collected,
        lineId,
        lineName,
        campaignName,
        stampDesignUrl,
        stationImageUrl,
        stampDesignName,
        stampDesignDescription,
        rarity,
        collectedAt,
        stampId,
        collectMethod,
        serialNumber,
        stationStory,
        collectionProgress,
        availability,
      ];
}
