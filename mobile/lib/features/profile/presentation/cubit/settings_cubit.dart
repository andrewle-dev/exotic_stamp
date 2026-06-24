import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/usecases/get_profile_usecase.dart';
import '../../domain/usecases/logout_profile_usecase.dart';
import '../../domain/usecases/update_profile_usecase.dart';
import 'settings_state.dart';

class SettingsCubit extends Cubit<SettingsState> {
  SettingsCubit({
    required GetProfileUseCase getProfileUseCase,
    required UpdateProfileUseCase updateProfileUseCase,
    required LogoutProfileUseCase logoutProfileUseCase,
  })  : _getProfileUseCase = getProfileUseCase,
        _updateProfileUseCase = updateProfileUseCase,
        _logoutProfileUseCase = logoutProfileUseCase,
        super(const SettingsState());

  final GetProfileUseCase _getProfileUseCase;
  final UpdateProfileUseCase _updateProfileUseCase;
  final LogoutProfileUseCase _logoutProfileUseCase;

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
          status: SettingsStatus.saveFailure,
          failure: failure,
          clearProfile: true,
        ),
      );
    } catch (_) {
      emit(
        state.copyWith(
          status: SettingsStatus.saveFailure,
          failure: const Failure(
            code: FailureCode.unknown,
            message: 'Không thể tải cài đặt.',
          ),
          clearProfile: true,
        ),
      );
    }
  }

  Future<void> updateProfile({
    required String firstname,
    required String lastname,
  }) async {
    final trimmedFirst = firstname.trim();
    final trimmedLast = lastname.trim();
    final fieldErrors = <String, String>{};

    if (trimmedFirst.isEmpty) {
      fieldErrors['firstname'] = 'Vui lòng nhập tên.';
    }
    if (trimmedLast.isEmpty) {
      fieldErrors['lastname'] = 'Vui lòng nhập họ.';
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
        ),
      );
      emit(
        state.copyWith(
          status: SettingsStatus.saveSuccess,
          profile: profile,
          successMessage: 'Đã cập nhật hồ sơ.',
          clearFailure: true,
          clearFieldErrors: true,
        ),
      );
    } on Failure catch (failure) {
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
            message: 'Không thể cập nhật hồ sơ.',
          ),
        ),
      );
    }
  }

  Future<void> logout() async {
    emit(
      state.copyWith(
        status: SettingsStatus.loggingOut,
        clearFailure: true,
      ),
    );
    await _logoutProfileUseCase();
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

    return errors;
  }
}
