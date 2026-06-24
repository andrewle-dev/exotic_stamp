class RecordShareEventRequestModel {
  const RecordShareEventRequestModel({
    required this.platform,
    required this.shareType,
    this.targetId,
    this.metadata,
  });

  final String platform;
  final String shareType;
  final String? targetId;
  final Map<String, String>? metadata;

  Map<String, dynamic> toJson() {
    return {
      'platform': platform,
      'shareType': shareType,
      if (targetId != null) 'targetId': targetId,
      if (metadata != null && metadata!.isNotEmpty) 'metadata': metadata,
    };
  }
}
