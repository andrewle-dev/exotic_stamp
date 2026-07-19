import '../../domain/entities/station.dart';
import '../../domain/entities/station_collected_status.dart';

class StationModel {
  StationModel({
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
    this.sortOrder,
    this.collectedStatus = StationCollectedStatus.unknown,
  });

  factory StationModel.fromJson(Map<String, dynamic> json) {
    return StationModel(
      id: json['id'] as String? ?? '',
      lineId: json['lineId'] as String? ?? '',
      code: json['code'] as String? ?? '',
      name: json['name'] as String? ?? '',
      displayName: json['displayName'] as String?,
      lineName: json['lineName'] as String?,
      latitude: (json['latitude'] as num?)?.toDouble(),
      longitude: (json['longitude'] as num?)?.toDouble(),
      imageUrl: json['imageUrl'] as String?,
      stampPreviewUrl: json['stampPreviewUrl'] as String?,
      status: _statusToString(json['status']),
      sortOrder: (json['sortOrder'] as num?)?.toInt(),
    );
  }

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
  final int? sortOrder;
  final StationCollectedStatus collectedStatus;

  StationModel copyWithCollectedStatus(StationCollectedStatus collectedStatus) {
    return StationModel(
      id: id,
      lineId: lineId,
      code: code,
      name: name,
      displayName: displayName,
      lineName: lineName,
      latitude: latitude,
      longitude: longitude,
      imageUrl: imageUrl,
      stampPreviewUrl: stampPreviewUrl,
      status: status,
      sortOrder: sortOrder,
      collectedStatus: collectedStatus,
    );
  }

  Station toEntity() {
    return Station(
      id: id,
      lineId: lineId,
      code: code,
      name: name,
      displayName: displayName,
      lineName: lineName,
      latitude: latitude,
      longitude: longitude,
      imageUrl: imageUrl,
      stampPreviewUrl: stampPreviewUrl,
      status: status,
      sortOrder: sortOrder,
      collectedStatus: collectedStatus,
    );
  }

  static String? _statusToString(Object? raw) {
    if (raw == null) return null;
    if (raw is String) return raw;
    return raw.toString();
  }
}

class StampBookStationCellModel {
  StampBookStationCellModel({
    required this.stationId,
    required this.collected,
  });

  factory StampBookStationCellModel.fromJson(Map<String, dynamic> json) {
    return StampBookStationCellModel(
      stationId: json['stationId'] as String? ?? '',
      collected: json['collected'] as bool? ?? false,
    );
  }

  final String stationId;
  final bool collected;
}
