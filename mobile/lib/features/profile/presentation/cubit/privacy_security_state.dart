import 'package:equatable/equatable.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/entities/profile.dart';

enum PrivacySecurityStatus {
  initial,
  loading,
  loaded,
  loggingOut,
  error,
  unauthorized,
}

class PrivacySecurityState extends Equatable {
  const PrivacySecurityState({
    this.status = PrivacySecurityStatus.initial,
    this.profile,
    this.failure,
  });

  final PrivacySecurityStatus status;
  final Profile? profile;
  final Failure? failure;

  PrivacySecurityState copyWith({
    PrivacySecurityStatus? status,
    Profile? profile,
    Failure? failure,
    bool clearProfile = false,
    bool clearFailure = false,
  }) {
    return PrivacySecurityState(
      status: status ?? this.status,
      profile: clearProfile ? null : (profile ?? this.profile),
      failure: clearFailure ? null : (failure ?? this.failure),
    );
  }

  @override
  List<Object?> get props => [status, profile, failure];
}
