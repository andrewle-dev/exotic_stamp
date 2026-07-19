import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/usecases/get_profile_usecase.dart';
import '../../domain/usecases/logout_all_profile_usecase.dart';
import '../../domain/usecases/logout_profile_usecase.dart';
import 'privacy_security_state.dart';

class PrivacySecurityCubit extends Cubit<PrivacySecurityState> {
  PrivacySecurityCubit({
    required GetProfileUseCase getProfileUseCase,
    required LogoutProfileUseCase logoutProfileUseCase,
    required LogoutAllProfileUseCase logoutAllProfileUseCase,
  })  : _getProfileUseCase = getProfileUseCase,
        _logoutProfileUseCase = logoutProfileUseCase,
        _logoutAllProfileUseCase = logoutAllProfileUseCase,
        super(const PrivacySecurityState());

  final GetProfileUseCase _getProfileUseCase;
  final LogoutProfileUseCase _logoutProfileUseCase;
  final LogoutAllProfileUseCase _logoutAllProfileUseCase;

  Future<void> load() async {
    emit(
      state.copyWith(
        status: PrivacySecurityStatus.loading,
        clearFailure: true,
      ),
    );
    try {
      final profile = await _getProfileUseCase();
      emit(
        state.copyWith(
          status: PrivacySecurityStatus.loaded,
          profile: profile,
          clearFailure: true,
        ),
      );
    } on Failure catch (failure) {
      emit(
        state.copyWith(
          status: failure.isAuthFailure
              ? PrivacySecurityStatus.unauthorized
              : PrivacySecurityStatus.error,
          failure: failure,
          clearProfile: true,
        ),
      );
    } catch (_) {
      emit(
        state.copyWith(
          status: PrivacySecurityStatus.error,
          failure: const Failure(
            code: FailureCode.unknown,
            message: 'Unable to load privacy settings.',
          ),
          clearProfile: true,
        ),
      );
    }
  }

  Future<void> logout() async {
    emit(
      state.copyWith(
        status: PrivacySecurityStatus.loggingOut,
        clearFailure: true,
      ),
    );
    await _logoutProfileUseCase();
  }

  Future<void> logoutAll() async {
    emit(
      state.copyWith(
        status: PrivacySecurityStatus.loggingOut,
        clearFailure: true,
      ),
    );
    await _logoutAllProfileUseCase();
  }
}
