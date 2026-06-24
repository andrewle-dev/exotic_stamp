import 'package:equatable/equatable.dart';

class ResolvedStation extends Equatable {
  const ResolvedStation({
    required this.id,
    required this.name,
    this.lineName,
    this.latitude,
    this.longitude,
    this.zoneRadiusMeters,
    this.imageUrl,
    this.stampPreviewUrl,
  });

  final String id;
  final String name;
  final String? lineName;
  final double? latitude;
  final double? longitude;
  final int? zoneRadiusMeters;
  final String? imageUrl;
  final String? stampPreviewUrl;

  @override
  List<Object?> get props => [
        id,
        name,
        lineName,
        latitude,
        longitude,
        zoneRadiusMeters,
        imageUrl,
        stampPreviewUrl,
      ];
}
