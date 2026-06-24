import '../../domain/entities/share_event.dart';

class ShareEventModel {
  ShareEventModel({
    required this.id,
    required this.platform,
    required this.shareType,
    required this.sharedAt,
    this.targetId,
  });

  factory ShareEventModel.fromJson(Map<String, dynamic> json) {
    return ShareEventModel(
      id: json['id'] as String? ?? '',
      platform: json['platform'] as String? ?? '',
      shareType: json['shareType'] as String? ?? '',
      targetId: json['targetId'] as String?,
      sharedAt: DateTime.tryParse(json['sharedAt'] as String? ?? '') ??
          DateTime.now().toUtc(),
    );
  }

  final String id;
  final String platform;
  final String shareType;
  final String? targetId;
  final DateTime sharedAt;

  ShareEvent toEntity() {
    return ShareEvent(
      id: id,
      platform: platform,
      shareType: shareType,
      targetId: targetId,
      sharedAt: sharedAt,
    );
  }
}
