import '../../domain/entities/line.dart';

class LineModel {
  LineModel({
    required this.id,
    required this.name,
    this.displayName,
    this.colorHex,
    this.status,
    this.totalStations,
  });

  factory LineModel.fromJson(Map<String, dynamic> json) {
    return LineModel(
      id: json['id'] as String? ?? '',
      name: json['name'] as String? ?? '',
      displayName: json['displayName'] as String?,
      colorHex: json['colorHex'] as String?,
      status: json['status'] as String?,
      totalStations: (json['totalStations'] as num?)?.toInt(),
    );
  }

  final String id;
  final String name;
  final String? displayName;
  final String? colorHex;
  final String? status;
  final int? totalStations;

  Line toEntity() {
    return Line(
      id: id,
      name: name,
      displayName: displayName,
      colorHex: colorHex,
      status: status,
      totalStations: totalStations,
    );
  }
}
