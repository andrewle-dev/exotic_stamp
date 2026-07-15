import 'package:equatable/equatable.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/entities/voucher_detail.dart';

enum VoucherDetailStatus {
  initial,
  loading,
  loaded,
  redeeming,
  failure,
}

class VoucherDetailState extends Equatable {
  const VoucherDetailState({
    this.status = VoucherDetailStatus.initial,
    this.detail,
    this.failure,
  });

  final VoucherDetailStatus status;
  final VoucherDetail? detail;
  final Failure? failure;

  VoucherDetailState copyWith({
    VoucherDetailStatus? status,
    VoucherDetail? detail,
    Failure? failure,
    bool clearFailure = false,
    bool clearDetail = false,
  }) {
    return VoucherDetailState(
      status: status ?? this.status,
      detail: clearDetail ? null : detail ?? this.detail,
      failure: clearFailure ? null : failure ?? this.failure,
    );
  }

  @override
  List<Object?> get props => [status, detail, failure];
}
