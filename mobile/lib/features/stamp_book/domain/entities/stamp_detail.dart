import 'package:equatable/equatable.dart';

enum StampDetailAvailability {
  full,
  limited,
  notFound,
}

class StampDetail extends Equatable {
  const StampDetail({
    required this.stationId,
    required this.stationName,
    required this.collected,
    this.lineId,
    this.lineName,
    this.campaignName,
    this.stampDesignUrl,
    this.collectedAt,
    this.stampId,
    this.collectMethod,
    this.availability = StampDetailAvailability.full,
  });

  final String stationId;
  final String stationName;
  final bool collected;
  final String? lineId;
  final String? lineName;
  final String? campaignName;
  final String? stampDesignUrl;
  final DateTime? collectedAt;
  final String? stampId;
  final String? collectMethod;
  final StampDetailAvailability availability;

  @override
  List<Object?> get props => [
        stationId,
        stationName,
        collected,
        lineId,
        lineName,
        campaignName,
        stampDesignUrl,
        collectedAt,
        stampId,
        collectMethod,
        availability,
      ];
}
