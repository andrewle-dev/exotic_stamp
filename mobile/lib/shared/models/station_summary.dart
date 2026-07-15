/// Shared presentation models for list cards.
class StationSummary {
  const StationSummary({
    required this.id,
    required this.name,
    this.lineLabel,
    this.stationCode,
    this.distanceLabel,
    this.imageUrl,
    this.collected,
  });

  final String id;
  final String name;
  final String? lineLabel;
  final String? stationCode;
  final String? distanceLabel;
  final String? imageUrl;

  /// Null means unknown; true/false from repository only.
  final bool? collected;
}
