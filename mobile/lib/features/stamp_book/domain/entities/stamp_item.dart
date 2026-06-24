import 'package:equatable/equatable.dart';

class StampItem extends Equatable {
  const StampItem({
    required this.stationId,
    required this.stationName,
    required this.sequence,
    required this.collected,
    this.stampDesignUrl,
    this.collectedAt,
    this.stampId,
    this.collectMethod,
  });

  final String stationId;
  final String stationName;
  final int sequence;
  final bool collected;
  final String? stampDesignUrl;
  final DateTime? collectedAt;
  final String? stampId;
  final String? collectMethod;

  @override
  List<Object?> get props => [
        stationId,
        stationName,
        sequence,
        collected,
        stampDesignUrl,
        collectedAt,
        stampId,
        collectMethod,
      ];
}
