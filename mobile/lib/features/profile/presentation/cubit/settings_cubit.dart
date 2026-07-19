import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/usecases/get_profile_usecase.dart';
import '../../domain/usecases/update_profile_usecase.dart';
import 'settings_state.dart';

class SettingsCubit extends Cubit<SettingsState> {
  SettingsCubit({
    required GetProfileUseCase getProfileUseCase,
    required UpdateProfileUseCase updateProfileUseCase,
  })  : _getProfileUseCase = getProfileUseCase,
        _updateProfileUseCase = updateProfileUseCase,
        super(const SettingsState());

  final GetProfileUseCase _getProfileUseCase;
  final UpdateProfileUseCase _updateProfileUseCase;

  Future<void> load() async {
    emit(state.copyWith(status: SettingsStatus.loading, clearFailure: true));
    try {
      final profile = await _getProfileUseCase();
      emit(
        state.copyWith(
          status: SettingsStatus.loaded,
          profile: profile,
          clearFailure: true,
        ),
      );
    } on Failure catch (failure) {
      emit(
        state.copyWith(
          status: failure.isAuthFailure
              ? SettingsStatus.unauthorized
              : SettingsStatus.error,
          failure: failure,
          clearProfile: true,
        ),
      );
    } catch (_) {
      emit(
        state.copyWith(
          status: SettingsStatus.error,
          failure: const Failure(
            code: FailureCode.unknown,
            message: 'Unable to load personal information.',
          ),
          clearProfile: true,
        ),
      );
    }
  }

  Future<void> updateProfile({
    required String firstname,
    required String lastname,
    String? bio,
  }) async {
    final trimmedFirst = firstname.trim();
    final trimmedLast = lastname.trim();
    final trimmedBio = bio?.trim();
    final fieldErrors = <String, String>{};

    if (trimmedFirst.isEmpty) {
      fieldErrors['firstname'] = 'Please enter your first name.';
    }
    if (trimmedLast.isEmpty) {
      fieldErrors['lastname'] = 'Please enter your last name.';
    }
    if (trimmedBio != null && trimmedBio.length > 100) {
      fieldErrors['bio'] = 'Bio must be 100 characters or fewer.';
    }

    if (fieldErrors.isNotEmpty) {
      emit(
        state.copyWith(
          fieldErrors: fieldErrors,
          clearFailure: true,
          clearSuccessMessage: true,
        ),
      );
      return;
    }

    emit(
      state.copyWith(
        status: SettingsStatus.saving,
        clearFailure: true,
        clearFieldErrors: true,
        clearSuccessMessage: true,
      ),
    );

    try {
      final profile = await _updateProfileUseCase(
        UpdateProfileParams(
          firstname: trimmedFirst,
          lastname: trimmedLast,
          bio: trimmedBio,
        ),
      );
      emit(
        state.copyWith(
          status: SettingsStatus.saveSuccess,
          profile: profile,
          successMessage: 'Profile updated.',
          clearFailure: true,
          clearFieldErrors: true,
        ),
      );
    } on Failure catch (failure) {
      if (failure.isAuthFailure) {
        emit(
          state.copyWith(
            status: SettingsStatus.unauthorized,
            failure: failure,
            clearFieldErrors: true,
          ),
        );
        return;
      }
      emit(
        state.copyWith(
          status: SettingsStatus.saveFailure,
          failure: failure,
          fieldErrors: _mapValidationErrors(failure),
        ),
      );
    } catch (_) {
      emit(
        state.copyWith(
          status: SettingsStatus.saveFailure,
          failure: const Failure(
            code: FailureCode.networkError,
            message: 'Unable to update profile.',
          ),
        ),
      );
    }
  }

  Map<String, String> _mapValidationErrors(Failure failure) {
    if (failure.code != FailureCode.validationError) {
      return const {};
    }

    final message = failure.message.toLowerCase();
    final errors = <String, String>{};

    if (message.contains('firstname') || message.contains('first name')) {
      errors['firstname'] = failure.message;
    }
    if (message.contains('lastname') || message.contains('last name')) {
      errors['lastname'] = failure.message;
    }
    if (message.contains('bio')) {
      errors['bio'] = failure.message;
    }

    return errors;
  }
}
