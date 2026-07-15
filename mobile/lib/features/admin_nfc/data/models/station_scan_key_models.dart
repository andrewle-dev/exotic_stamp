class StationScanKeyCreatedModel {
  StationScanKeyCreatedModel({
    required this.id,
    required this.stationId,
    required this.scanType,
    required this.payloadToWrite,
    required this.keyPrefix,
    required this.status,
    this.label,
    this.placementNote,
  });

  factory StationScanKeyCreatedModel.fromJson(Map<String, dynamic> json) {
    return StationScanKeyCreatedModel(
      id: json['id'] as String? ?? '',
      stationId: json['stationId'] as String? ?? '',
      scanType: json['scanType'] as String? ?? 'NFC',
      payloadToWrite: json['payloadToWrite'] as String? ?? '',
      keyPrefix: json['keyPrefix'] as String? ?? '',
      status: json['status'] as String? ?? 'DRAFT',
      label: json['label'] as String?,
      placementNote: json['placementNote'] as String?,
    );
  }

  final String id;
  final String stationId;
  final String scanType;
  final String payloadToWrite;
  final String keyPrefix;
  final String status;
  final String? label;
  final String? placementNote;
}

class StationScanKeyVerifyModel {
  StationScanKeyVerifyModel({
    required this.verified,
    required this.id,
    required this.stationId,
    this.lastInstallVerifiedAt,
  });

  factory StationScanKeyVerifyModel.fromJson(Map<String, dynamic> json) {
    return StationScanKeyVerifyModel(
      verified: json['verified'] as bool? ?? false,
      id: json['id'] as String? ?? '',
      stationId: json['stationId'] as String? ?? '',
      lastInstallVerifiedAt: json['lastInstallVerifiedAt'] as String?,
    );
  }

  final bool verified;
  final String id;
  final String stationId;
  final String? lastInstallVerifiedAt;
}
