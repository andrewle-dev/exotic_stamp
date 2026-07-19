import '../../domain/entities/line.dart';

class LineModel {
  LineModel({
    required this.id,
    required this.name,
    this.displayName,
    this.colorHex,
    this.status,
    this.totalStations,
    this.sortOrder,
  });

  factory LineModel.fromJson(Map<String, dynamic> json) {
    return LineModel(
      id: json['id'] as String? ?? '',
      name: json['name'] as String? ?? '',
      displayName: json['displayName'] as String?,
      colorHex: json['colorHex'] as String?,
      status: _statusToString(json['status']),
      totalStations: (json['totalStations'] as num?)?.toInt(),
      sortOrder: (json['sortOrder'] as num?)?.toInt(),
    );
  }

  final String id;
  final String name;
  final String? displayName;
  final String? colorHex;
  final String? status;
  final int? totalStations;
  final int? sortOrder;

  Line toEntity() {
    return Line(
      id: id,
      name: name,
      displayName: displayName,
      colorHex: colorHex,
      status: status,
      totalStations: totalStations,
      sortOrder: sortOrder,
    );
  }

  static String? _statusToString(Object? raw) {
    if (raw == null) return null;
    if (raw is String) return raw;
    return raw.toString();
  }
}
