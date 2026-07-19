import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/usecases/change_password_usecase.dart';
import 'change_password_state.dart';

class ChangePasswordCubit extends Cubit<ChangePasswordState> {
  ChangePasswordCubit({required ChangePasswordUseCase changePasswordUseCase})
      : _changePasswordUseCase = changePasswordUseCase,
        super(const ChangePasswordState());

  final ChangePasswordUseCase _changePasswordUseCase;

  Future<void> submit({
    required String currentPassword,
    required String newPassword,
    required String confirmNewPassword,
  }) async {
    if (state.isSubmitting) {
      return;
    }

    emit(
      state.copyWith(
        status: ChangePasswordStatus.submitting,
        clearFailure: true,
      ),
    );

    try {
      await _changePasswordUseCase(
        ChangePasswordParams(
          currentPassword: currentPassword,
          newPassword: newPassword,
          confirmNewPassword: confirmNewPassword,
        ),
      );
      emit(state.copyWith(status: ChangePasswordStatus.success));
    } on Failure catch (failure) {
      emit(
        state.copyWith(
          status: ChangePasswordStatus.failure,
          failure: failure,
        ),
      );
    } catch (_) {
      emit(
        state.copyWith(
          status: ChangePasswordStatus.failure,
          failure: const Failure(
            code: FailureCode.unknown,
            message: 'Unable to change password. Please try again.',
          ),
        ),
      );
    }
  }
}
