import 'package:equatable/equatable.dart';

class Line extends Equatable {
  const Line({
    required this.id,
    required this.name,
    this.displayName,
    this.colorHex,
    this.status,
    this.totalStations,
    this.sortOrder,
  });

  final String id;
  final String name;
  final String? displayName;
  final String? colorHex;
  final String? status;
  final int? totalStations;
  final int? sortOrder;

  String get label => displayName ?? name;

  @override
  List<Object?> get props => [
        id,
        name,
        displayName,
        colorHex,
        status,
        totalStations,
        sortOrder,
      ];
}
