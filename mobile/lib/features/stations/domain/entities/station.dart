import 'package:equatable/equatable.dart';

import 'station_collected_status.dart';

class Station extends Equatable {
  const Station({
    required this.id,
    required this.lineId,
    required this.code,
    required this.name,
    this.displayName,
    this.lineName,
    this.latitude,
    this.longitude,
    this.imageUrl,
    this.stampPreviewUrl,
    this.status,
    this.collectedStatus = StationCollectedStatus.unknown,
  });

  final String id;
  final String lineId;
  final String code;
  final String name;
  final String? displayName;
  final String? lineName;
  final double? latitude;
  final double? longitude;
  final String? imageUrl;
  final String? stampPreviewUrl;
  final String? status;
  final StationCollectedStatus collectedStatus;

  String get label => displayName ?? name;

  bool get isActive => status == null || status == 'ACTIVE';

  bool get isCollected => collectedStatus == StationCollectedStatus.collected;

  @override
  List<Object?> get props => [
        id,
        lineId,
        code,
        name,
        displayName,
        lineName,
        latitude,
        longitude,
        imageUrl,
        stampPreviewUrl,
        status,
        collectedStatus,
      ];
}
