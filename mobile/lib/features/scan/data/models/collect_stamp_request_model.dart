import '../../domain/entities/scan_type.dart';

class CollectStampRequestModel {
  CollectStampRequestModel({
    required this.scanType,
    required this.payload,
    required this.latitude,
    required this.longitude,
    required this.accuracyMeters,
    required this.devicePlatform,
    required this.appVersion,
    required this.idempotencyKey,
  });

  final ScanType scanType;
  final String payload;
  final double latitude;
  final double longitude;
  final double accuracyMeters;
  final String devicePlatform;
  final String appVersion;
  final String idempotencyKey;

  Map<String, dynamic> toJson() {
    return {
      'scanType': scanType.apiValue,
      'payload': payload,
      'latitude': latitude,
      'longitude': longitude,
      'accuracyMeters': accuracyMeters,
      'devicePlatform': devicePlatform,
      'appVersion': appVersion,
      'idempotencyKey': idempotencyKey,
    };
  }
}
