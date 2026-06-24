import 'package:equatable/equatable.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/entities/profile.dart';

enum ProfileStatus {
  initial,
  loading,
  loaded,
  unauthorized,
  error,
}

class ProfileState extends Equatable {
  const ProfileState({
    this.status = ProfileStatus.initial,
    this.profile,
    this.failure,
  });

  final ProfileStatus status;
  final Profile? profile;
  final Failure? failure;

  ProfileState copyWith({
    ProfileStatus? status,
    Profile? profile,
    Failure? failure,
    bool clearProfile = false,
    bool clearFailure = false,
  }) {
    return ProfileState(
      status: status ?? this.status,
      profile: clearProfile ? null : (profile ?? this.profile),
      failure: clearFailure ? null : (failure ?? this.failure),
    );
  }

  @override
  List<Object?> get props => [status, profile, failure];
}
