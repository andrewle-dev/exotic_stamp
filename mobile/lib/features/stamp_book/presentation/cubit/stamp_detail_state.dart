import 'package:equatable/equatable.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/entities/stamp_detail.dart';

enum StampDetailStatus {
  initial,
  loading,
  loaded,
  failure,
}

class StampDetailState extends Equatable {
  const StampDetailState({
    this.status = StampDetailStatus.initial,
    this.detail,
    this.failure,
  });

  final StampDetailStatus status;
  final StampDetail? detail;
  final Failure? failure;

  StampDetailState copyWith({
    StampDetailStatus? status,
    StampDetail? detail,
    Failure? failure,
    bool clearFailure = false,
    bool clearDetail = false,
  }) {
    return StampDetailState(
      status: status ?? this.status,
      detail: clearDetail ? null : (detail ?? this.detail),
      failure: clearFailure ? null : (failure ?? this.failure),
    );
  }

  @override
  List<Object?> get props => [status, detail, failure];
}
