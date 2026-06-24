import 'package:equatable/equatable.dart';

import 'stamp_item.dart';

class StampBookProgress extends Equatable {
  const StampBookProgress({
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

class StampBook extends Equatable {
  const StampBook({
    required this.lineId,
    required this.lineName,
    this.campaignId,
    this.campaignName,
    this.progress,
    required this.stations,
  });

  final String lineId;
  final String lineName;
  final String? campaignId;
  final String? campaignName;
  final StampBookProgress? progress;
  final List<StampItem> stations;

  bool get hasCollectedStamps =>
      stations.any((station) => station.collected) ||
      (progress?.collected ?? 0) > 0;

  @override
  List<Object?> get props => [
        lineId,
        lineName,
        campaignId,
        campaignName,
        progress,
        stations,
      ];
}
