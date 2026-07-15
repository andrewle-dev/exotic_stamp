import 'package:equatable/equatable.dart';

/// Collected stamp option for photo share overlay selection.
class StampShareOption extends Equatable {
  const StampShareOption({
    required this.stationId,
    required this.stationName,
    this.stampId,
    this.stampDesignUrl,
    this.collectedAt,
    this.lineName,
  });

  final String stationId;
  final String stationName;
  final String? stampId;
  final String? stampDesignUrl;
  final DateTime? collectedAt;
  final String? lineName;

  @override
  List<Object?> get props => [
        stationId,
        stationName,
        stampId,
        stampDesignUrl,
        collectedAt,
        lineName,
      ];
}
