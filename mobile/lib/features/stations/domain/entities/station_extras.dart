import 'package:equatable/equatable.dart';

class StationSocialProof extends Equatable {
  const StationSocialProof({
    required this.message,
    this.collectorCount,
    this.overflowLabel,
  });

  final String message;
  final int? collectorCount;
  final String? overflowLabel;

  @override
  List<Object?> get props => [message, collectorCount, overflowLabel];
}

class NearbyPlace extends Equatable {
  const NearbyPlace({
    required this.id,
    required this.name,
    required this.category,
    this.imageUrl,
    this.distanceMeters,
  });

  final String id;
  final String name;
  final String category;
  final String? imageUrl;
  final int? distanceMeters;

  @override
  List<Object?> get props => [id, name, category, imageUrl, distanceMeters];
}
