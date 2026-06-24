import 'package:equatable/equatable.dart';

import 'station_collected_status.dart';

class StationDetail extends Equatable {
  const StationDetail({
    required this.id,
    required this.lineId,
    required this.name,
    this.lineName,
    this.displayName,
    this.description,
    this.address,
    this.imageUrl,
    this.stampPreviewUrl,
    this.latitude,
    this.longitude,
    this.zoneRadiusMeters,
    this.status,
    this.collectedStatus = StationCollectedStatus.unknown,
  });

  final String id;
  final String lineId;
  final String? lineName;
  final String name;
  final String? displayName;
  final String? description;
  final String? address;
  final String? imageUrl;
  final String? stampPreviewUrl;
  final double? latitude;
  final double? longitude;
  final int? zoneRadiusMeters;
  final String? status;
  final StationCollectedStatus collectedStatus;

  String get label => displayName ?? name;

  bool get isActive => status == null || status == 'ACTIVE';

  bool get isCollected => collectedStatus == StationCollectedStatus.collected;

  @override
  List<Object?> get props => [
        id,
        lineId,
        lineName,
        name,
        displayName,
        description,
        address,
        imageUrl,
        stampPreviewUrl,
        latitude,
        longitude,
        zoneRadiusMeters,
        status,
        collectedStatus,
      ];
}
