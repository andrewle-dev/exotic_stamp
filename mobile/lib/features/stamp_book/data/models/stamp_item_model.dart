import '../../domain/entities/stamp_item.dart';

class StampItemModel {
  StampItemModel({
    required this.stationId,
    required this.stationName,
    required this.sequence,
    required this.collected,
    this.stampDesignUrl,
    this.stampDesignName,
    this.stampDesignDescription,
    this.rarity,
    this.collectedAt,
    this.stampId,
    this.collectMethod,
  });

  factory StampItemModel.fromJson(Map<String, dynamic> json) {
    return StampItemModel(
      stationId: json['stationId'] as String? ?? '',
      stationName: json['stationName'] as String? ?? '',
      sequence: (json['sequence'] as num?)?.toInt() ?? 0,
      collected: json['collected'] as bool? ?? false,
      stampDesignUrl: json['stampDesignUrl'] as String?,
      stampDesignName: json['stampDesignName'] as String?,
      stampDesignDescription: json['stampDesignDescription'] as String?,
      rarity: json['rarity'] as String?,
      collectedAt: DateTime.tryParse(json['collectedAt'] as String? ?? ''),
      stampId: json['stampId'] as String?,
      collectMethod: json['collectMethod'] as String?,
    );
  }

  factory StampItemModel.fromUserStampJson(Map<String, dynamic> json) {
    return StampItemModel(
      stationId: json['stationId'] as String? ?? '',
      stationName: json['stationName'] as String? ?? '',
      sequence: 0,
      collected: true,
      stampDesignUrl: json['stampDesignUrl'] as String?,
      stampDesignName: json['stampDesignName'] as String?,
      stampDesignDescription: json['stampDesignDescription'] as String?,
      rarity: json['rarity'] as String?,
      collectedAt: DateTime.tryParse(json['collectedAt'] as String? ?? ''),
      stampId: json['stampId'] as String?,
      collectMethod: json['collectMethod'] as String?,
    );
  }

  final String stationId;
  final String stationName;
  final int sequence;
  final bool collected;
  final String? stampDesignUrl;
  final String? stampDesignName;
  final String? stampDesignDescription;
  final String? rarity;
  final DateTime? collectedAt;
  final String? stampId;
  final String? collectMethod;

  StampItem toEntity() {
    return StampItem(
      stationId: stationId,
      stationName: stationName,
      sequence: sequence,
      collected: collected,
      stampDesignUrl: stampDesignUrl,
      stampDesignName: stampDesignName,
      stampDesignDescription: stampDesignDescription,
      rarity: rarity,
      collectedAt: collectedAt,
      stampId: stampId,
      collectMethod: collectMethod,
    );
  }

  StampItemModel mergeCollectedMetadata(StampItemModel other) {
    return StampItemModel(
      stationId: stationId,
      stationName: stationName.isNotEmpty ? stationName : other.stationName,
      sequence: sequence,
      collected: collected,
      stampDesignUrl: stampDesignUrl ?? other.stampDesignUrl,
      stampDesignName: stampDesignName ?? other.stampDesignName,
      stampDesignDescription:
          stampDesignDescription ?? other.stampDesignDescription,
      rarity: rarity ?? other.rarity,
      collectedAt: collectedAt ?? other.collectedAt,
      stampId: other.stampId ?? stampId,
      collectMethod: other.collectMethod ?? collectMethod,
    );
  }
}
