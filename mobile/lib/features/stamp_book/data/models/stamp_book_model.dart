import '../../domain/entities/stamp_book.dart';
import 'stamp_item_model.dart';

class StampBookProgressModel {
  StampBookProgressModel({
    required this.lineId,
    required this.collected,
    required this.total,
    required this.percentage,
  });

  factory StampBookProgressModel.fromJson(Map<String, dynamic> json) {
    return StampBookProgressModel(
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

  StampBookProgress toEntity() {
    return StampBookProgress(
      lineId: lineId,
      collected: collected,
      total: total,
      percentage: percentage,
    );
  }
}

class StampBookModel {
  StampBookModel({
    required this.lineId,
    required this.lineName,
    this.campaignId,
    this.campaignName,
    this.progress,
    required this.stations,
  });

  factory StampBookModel.fromJson(Map<String, dynamic> json) {
    final stationsJson = json['stations'] as List<dynamic>? ?? [];
    final progressJson = json['progress'] as Map<String, dynamic>?;

    return StampBookModel(
      lineId: json['lineId'] as String? ?? '',
      lineName: json['lineName'] as String? ?? '',
      campaignId: json['campaignId'] as String?,
      campaignName: json['campaignName'] as String?,
      progress: progressJson == null
          ? null
          : StampBookProgressModel.fromJson(progressJson),
      stations: stationsJson
          .whereType<Map<String, dynamic>>()
          .map(StampItemModel.fromJson)
          .toList(),
    );
  }

  final String lineId;
  final String lineName;
  final String? campaignId;
  final String? campaignName;
  final StampBookProgressModel? progress;
  final List<StampItemModel> stations;

  StampBook toEntity() {
    return StampBook(
      lineId: lineId,
      lineName: lineName,
      campaignId: campaignId,
      campaignName: campaignName,
      progress: progress?.toEntity(),
      stations: stations.map((station) => station.toEntity()).toList(),
    );
  }
}
