import 'package:equatable/equatable.dart';

class ShareEvent extends Equatable {
  const ShareEvent({
    required this.id,
    required this.platform,
    required this.shareType,
    required this.sharedAt,
    this.targetId,
  });

  final String id;
  final String platform;
  final String shareType;
  final String? targetId;
  final DateTime sharedAt;

  @override
  List<Object?> get props => [
        id,
        platform,
        shareType,
        targetId,
        sharedAt,
      ];
}
