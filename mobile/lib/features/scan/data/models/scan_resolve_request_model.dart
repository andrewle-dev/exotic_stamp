import '../../domain/entities/scan_type.dart';

class ScanResolveRequestModel {
  ScanResolveRequestModel({
    required this.scanType,
    required this.payload,
    required this.devicePlatform,
    required this.appVersion,
  });

  final ScanType scanType;
  final String payload;
  final String devicePlatform;
  final String appVersion;

  Map<String, dynamic> toJson() {
    return {
      'scanType': scanType.apiValue,
      'payload': payload,
      'devicePlatform': devicePlatform,
      'appVersion': appVersion,
    };
  }
}
