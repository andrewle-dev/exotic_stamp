import '../entities/share_event.dart';

class RecordShareEventParams {
  const RecordShareEventParams({
    required this.platform,
    required this.shareType,
    this.targetId,
    this.metadata,
  });

  final String platform;
  final String shareType;
  final String? targetId;
  final Map<String, String>? metadata;
}

abstract class MemoriesRepository {
  Future<ShareEvent> recordShareEvent(RecordShareEventParams params);
}
