import 'package:equatable/equatable.dart';

class CollectStampProgress extends Equatable {
  const CollectStampProgress({
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

class CollectedStamp extends Equatable {
  const CollectedStamp({
    required this.stampId,
    required this.stationId,
    required this.stationName,
    this.lineName,
    this.lineId,
    this.campaignId,
    this.stampDesignUrl,
    required this.collectedAt,
  });

  final String stampId;
  final String stationId;
  final String stationName;
  final String? lineName;
  final String? lineId;
  final String? campaignId;
  final String? stampDesignUrl;
  final DateTime collectedAt;

  @override
  List<Object?> get props => [
        stampId,
        stationId,
        stationName,
        lineName,
        lineId,
        campaignId,
        stampDesignUrl,
        collectedAt,
      ];
}

class CollectStampResult extends Equatable {
  const CollectStampResult({
    required this.stamp,
    this.progress,
    required this.isNew,
  });

  final CollectedStamp stamp;
  final CollectStampProgress? progress;
  final bool isNew;

  @override
  List<Object?> get props => [stamp, progress, isNew];
}
