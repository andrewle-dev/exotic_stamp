import '../../domain/entities/station_collected_status.dart';
import '../../domain/entities/station_detail.dart';

class StationDetailModel {
  StationDetailModel({
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

  factory StationDetailModel.fromJson(Map<String, dynamic> json) {
    return StationDetailModel(
      id: json['id'] as String? ?? '',
      lineId: json['lineId'] as String? ?? '',
      lineName: json['lineName'] as String?,
      name: json['name'] as String? ?? '',
      displayName: json['displayName'] as String?,
      description: json['description'] as String?,
      address: json['address'] as String?,
      imageUrl: json['imageUrl'] as String?,
      stampPreviewUrl: json['stampPreviewUrl'] as String?,
      latitude: (json['latitude'] as num?)?.toDouble(),
      longitude: (json['longitude'] as num?)?.toDouble(),
      zoneRadiusMeters: (json['zoneRadiusMeters'] as num?)?.toInt(),
      status: json['status'] as String?,
    );
  }

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

  StationDetailModel copyWithCollectedStatus(
    StationCollectedStatus collectedStatus,
  ) {
    return StationDetailModel(
      id: id,
      lineId: lineId,
      lineName: lineName,
      name: name,
      displayName: displayName,
      description: description,
      address: address,
      imageUrl: imageUrl,
      stampPreviewUrl: stampPreviewUrl,
      latitude: latitude,
      longitude: longitude,
      zoneRadiusMeters: zoneRadiusMeters,
      status: status,
      collectedStatus: collectedStatus,
    );
  }

  StationDetail toEntity() {
    return StationDetail(
      id: id,
      lineId: lineId,
      lineName: lineName,
      name: name,
      displayName: displayName,
      description: description,
      address: address,
      imageUrl: imageUrl,
      stampPreviewUrl: stampPreviewUrl,
      latitude: latitude,
      longitude: longitude,
      zoneRadiusMeters: zoneRadiusMeters,
      status: status,
      collectedStatus: collectedStatus,
    );
  }
}
