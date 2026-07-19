import 'package:equatable/equatable.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/entities/profile.dart';

enum SettingsStatus {
  initial,
  loading,
  loaded,
  saving,
  saveSuccess,
  saveFailure,
  unauthorized,
  error,
}

class SettingsState extends Equatable {
  const SettingsState({
    this.status = SettingsStatus.initial,
    this.profile,
    this.failure,
    this.fieldErrors = const {},
    this.successMessage,
  });

  final SettingsStatus status;
  final Profile? profile;
  final Failure? failure;
  final Map<String, String> fieldErrors;
  final String? successMessage;

  SettingsState copyWith({
    SettingsStatus? status,
    Profile? profile,
    Failure? failure,
    Map<String, String>? fieldErrors,
    String? successMessage,
    bool clearProfile = false,
    bool clearFailure = false,
    bool clearFieldErrors = false,
    bool clearSuccessMessage = false,
  }) {
    return SettingsState(
      status: status ?? this.status,
      profile: clearProfile ? null : (profile ?? this.profile),
      failure: clearFailure ? null : (failure ?? this.failure),
      fieldErrors:
          clearFieldErrors ? const {} : (fieldErrors ?? this.fieldErrors),
      successMessage:
          clearSuccessMessage ? null : (successMessage ?? this.successMessage),
    );
  }

  @override
  List<Object?> get props =>
      [status, profile, failure, fieldErrors, successMessage];
}
