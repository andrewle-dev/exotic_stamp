import '../../domain/entities/resolved_station.dart';
import '../../domain/entities/scan_type.dart';

class ScanResolveResponseModel {
  ScanResolveResponseModel({
    required this.station,
    required this.resolved,
    required this.scanType,
  });

  factory ScanResolveResponseModel.fromJson(Map<String, dynamic> json) {
    final stationJson = json['station'] as Map<String, dynamic>? ?? {};
    final scanJson = json['scan'] as Map<String, dynamic>? ?? {};
    final scanTypeRaw = scanJson['scanType'] as String? ?? 'NFC';

    return ScanResolveResponseModel(
      station: ResolvedStationModel.fromJson(stationJson),
      resolved: scanJson['resolved'] as bool? ?? true,
      scanType: _parseScanType(scanTypeRaw),
    );
  }

  static ScanType _parseScanType(String raw) {
    final normalized = raw.toUpperCase();
    if (normalized == 'NFC') {
      return ScanType.nfc;
    }
    return ScanType.qr;
  }

  final ResolvedStationModel station;
  final bool resolved;
  final ScanType scanType;

  ResolvedStation toEntity() => station.toEntity();
}

class ResolvedStationModel {
  ResolvedStationModel({
    required this.id,
    required this.name,
    this.lineName,
    this.latitude,
    this.longitude,
    this.zoneRadiusMeters,
    this.imageUrl,
    this.stampPreviewUrl,
  });

  factory ResolvedStationModel.fromJson(Map<String, dynamic> json) {
    return ResolvedStationModel(
      id: json['id'] as String? ?? '',
      name: json['name'] as String? ?? '',
      lineName: json['lineName'] as String?,
      latitude: (json['latitude'] as num?)?.toDouble(),
      longitude: (json['longitude'] as num?)?.toDouble(),
      zoneRadiusMeters: (json['zoneRadiusMeters'] as num?)?.toInt(),
      imageUrl: json['imageUrl'] as String?,
      stampPreviewUrl: json['stampPreviewUrl'] as String?,
    );
  }

  final String id;
  final String name;
  final String? lineName;
  final double? latitude;
  final double? longitude;
  final int? zoneRadiusMeters;
  final String? imageUrl;
  final String? stampPreviewUrl;

  ResolvedStation toEntity() {
    return ResolvedStation(
      id: id,
      name: name,
      lineName: lineName,
      latitude: latitude,
      longitude: longitude,
      zoneRadiusMeters: zoneRadiusMeters,
      imageUrl: imageUrl,
      stampPreviewUrl: stampPreviewUrl,
    );
  }
}
