import '../../domain/entities/collect_stamp_result.dart';

class CollectStampResponseModel {
  CollectStampResponseModel({
    required this.stamp,
    this.progress,
    required this.isNew,
  });

  factory CollectStampResponseModel.fromJson(Map<String, dynamic> json) {
    final stampJson = json['stamp'] as Map<String, dynamic>? ?? {};
    final progressJson = json['progress'] as Map<String, dynamic>?;

    return CollectStampResponseModel(
      stamp: CollectedStampModel.fromJson(stampJson),
      progress: progressJson == null
          ? null
          : CollectStampProgressModel.fromJson(progressJson),
      isNew: json['isNew'] as bool? ?? json['new'] as bool? ?? true,
    );
  }

  final CollectedStampModel stamp;
  final CollectStampProgressModel? progress;
  final bool isNew;

  CollectStampResult toEntity() {
    return CollectStampResult(
      stamp: stamp.toEntity(),
      progress: progress?.toEntity(),
      isNew: isNew,
    );
  }
}

class CollectedStampModel {
  CollectedStampModel({
    required this.stampId,
    required this.stationId,
    required this.stationName,
    this.lineName,
    this.lineId,
    this.campaignId,
    this.stampDesignUrl,
    required this.collectedAt,
  });

  factory CollectedStampModel.fromJson(Map<String, dynamic> json) {
    return CollectedStampModel(
      stampId: json['stampId'] as String? ?? '',
      stationId: json['stationId'] as String? ?? '',
      stationName: json['stationName'] as String? ?? '',
      lineName: json['lineName'] as String?,
      lineId: json['lineId'] as String?,
      campaignId: json['campaignId'] as String?,
      stampDesignUrl: json['stampDesignUrl'] as String?,
      collectedAt: DateTime.tryParse(json['collectedAt'] as String? ?? '') ??
          DateTime.fromMillisecondsSinceEpoch(0),
    );
  }

  final String stampId;
  final String stationId;
  final String stationName;
  final String? lineName;
  final String? lineId;
  final String? campaignId;
  final String? stampDesignUrl;
  final DateTime collectedAt;

  CollectedStamp toEntity() {
    return CollectedStamp(
      stampId: stampId,
      stationId: stationId,
      stationName: stationName,
      lineName: lineName,
      lineId: lineId,
      campaignId: campaignId,
      stampDesignUrl: stampDesignUrl,
      collectedAt: collectedAt,
    );
  }
}

class CollectStampProgressModel {
  CollectStampProgressModel({
    required this.lineId,
    required this.collected,
    required this.total,
    required this.percentage,
  });

  factory CollectStampProgressModel.fromJson(Map<String, dynamic> json) {
    return CollectStampProgressModel(
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

  CollectStampProgress toEntity() {
    return CollectStampProgress(
      lineId: lineId,
      collected: collected,
      total: total,
      percentage: percentage,
    );
  }
}
