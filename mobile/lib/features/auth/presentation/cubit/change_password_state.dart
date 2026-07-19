import 'package:equatable/equatable.dart';

import '../../../../core/errors/failure.dart';

enum ChangePasswordStatus {
  initial,
  submitting,
  success,
  failure,
}

class ChangePasswordState extends Equatable {
  const ChangePasswordState({
    this.status = ChangePasswordStatus.initial,
    this.failure,
  });

  final ChangePasswordStatus status;
  final Failure? failure;

  bool get isSubmitting => status == ChangePasswordStatus.submitting;

  ChangePasswordState copyWith({
    ChangePasswordStatus? status,
    Failure? failure,
    bool clearFailure = false,
  }) {
    return ChangePasswordState(
      status: status ?? this.status,
      failure: clearFailure ? null : (failure ?? this.failure),
    );
  }

  @override
  List<Object?> get props => [status, failure];
}
