import 'package:equatable/equatable.dart';

/// Route-safe stamp/station context for photo share.
///
/// Only populated from navigation extras or backend-confirmed stamp detail.
class PhotoShareContext extends Equatable {
  const PhotoShareContext({
    required this.stationId,
    required this.stationName,
    required this.shareType,
    this.stampId,
    this.stampDesignUrl,
    this.collectedAt,
    this.lineName,
  });

  final String stationId;
  final String stationName;
  final String shareType;
  final String? stampId;
  final String? stampDesignUrl;
  final DateTime? collectedAt;
  final String? lineName;

  /// Backend `targetId` when available; otherwise station id.
  String? get targetId => stampId ?? stationId;

  static const shareTypeStampCollected = 'stamp_collected';
  static const shareTypeRewardUnlocked = 'reward_unlocked';

  @override
  List<Object?> get props => [
        stationId,
        stationName,
        shareType,
        stampId,
        stampDesignUrl,
        collectedAt,
        lineName,
      ];
}
