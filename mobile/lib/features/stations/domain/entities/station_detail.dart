import 'package:equatable/equatable.dart';

import 'station_collected_status.dart';
import 'station_extras.dart';

class StationDetail extends Equatable {
  const StationDetail({
    required this.id,
    required this.lineId,
    required this.name,
    this.lineName,
    this.lineHubLabel,
    this.displayName,
    this.description,
    this.address,
    this.districtLabel,
    this.imageUrl,
    this.stampPreviewUrl,
    this.latitude,
    this.longitude,
    this.zoneRadiusMeters,
    this.status,
    this.collectedStatus = StationCollectedStatus.unknown,
    this.socialProof,
    this.nearbyPlaces = const [],
    this.openingHoursLabel,
    this.accessibilityLabel,
    this.virtualTourUrl,
  });

  final String id;
  final String lineId;
  final String? lineName;
  final String? lineHubLabel;
  final String name;
  final String? displayName;
  final String? description;
  final String? address;
  final String? districtLabel;
  final String? imageUrl;
  final String? stampPreviewUrl;
  final double? latitude;
  final double? longitude;
  final int? zoneRadiusMeters;
  final String? status;
  final StationCollectedStatus collectedStatus;
  final StationSocialProof? socialProof;
  final List<NearbyPlace> nearbyPlaces;
  final String? openingHoursLabel;
  final String? accessibilityLabel;
  final String? virtualTourUrl;

  String get label => displayName ?? name;

  bool get isActive => status == null || status == 'ACTIVE';

  bool get isCollected => collectedStatus == StationCollectedStatus.collected;

  @override
  List<Object?> get props => [
        id,
        lineId,
        lineName,
        lineHubLabel,
        name,
        displayName,
        description,
        address,
        districtLabel,
        imageUrl,
        stampPreviewUrl,
        latitude,
        longitude,
        zoneRadiusMeters,
        status,
        collectedStatus,
        socialProof,
        nearbyPlaces,
        openingHoursLabel,
        accessibilityLabel,
        virtualTourUrl,
      ];
}
