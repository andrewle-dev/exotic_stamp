import 'package:equatable/equatable.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/entities/station_detail.dart';

enum StationDetailStatus {
  initial,
  loading,
  loaded,
  inactive,
  notFound,
  failure,
}

class StationDetailState extends Equatable {
  const StationDetailState({
    this.status = StationDetailStatus.initial,
    this.detail,
    this.failure,
  });

  final StationDetailStatus status;
  final StationDetail? detail;
  final Failure? failure;

  StationDetailState copyWith({
    StationDetailStatus? status,
    StationDetail? detail,
    Failure? failure,
    bool clearDetail = false,
    bool clearFailure = false,
  }) {
    return StationDetailState(
      status: status ?? this.status,
      detail: clearDetail ? null : (detail ?? this.detail),
      failure: clearFailure ? null : (failure ?? this.failure),
    );
  }

  @override
  List<Object?> get props => [status, detail, failure];
}
