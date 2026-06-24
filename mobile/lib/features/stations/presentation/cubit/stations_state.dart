import 'package:equatable/equatable.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/entities/line.dart';
import '../../domain/entities/station.dart';

enum StationsStatus {
  initial,
  loading,
  loaded,
  failure,
}

class StationsState extends Equatable {
  const StationsState({
    this.status = StationsStatus.initial,
    this.lines = const [],
    this.stations = const [],
    this.selectedLineId,
    this.searchQuery = '',
    this.failure,
  });

  final StationsStatus status;
  final List<Line> lines;
  final List<Station> stations;
  final String? selectedLineId;
  final String searchQuery;
  final Failure? failure;

  Line? get selectedLine {
    if (selectedLineId == null) {
      return null;
    }
    for (final line in lines) {
      if (line.id == selectedLineId) {
        return line;
      }
    }
    return null;
  }

  StationsState copyWith({
    StationsStatus? status,
    List<Line>? lines,
    List<Station>? stations,
    String? selectedLineId,
    String? searchQuery,
    Failure? failure,
    bool clearFailure = false,
  }) {
    return StationsState(
      status: status ?? this.status,
      lines: lines ?? this.lines,
      stations: stations ?? this.stations,
      selectedLineId: selectedLineId ?? this.selectedLineId,
      searchQuery: searchQuery ?? this.searchQuery,
      failure: clearFailure ? null : (failure ?? this.failure),
    );
  }

  @override
  List<Object?> get props => [
        status,
        lines,
        stations,
        selectedLineId,
        searchQuery,
        failure,
      ];
}
