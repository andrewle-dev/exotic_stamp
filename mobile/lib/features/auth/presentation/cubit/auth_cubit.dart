import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/usecases/forgot_password_usecase.dart';
import '../../domain/usecases/login_usecase.dart';
import '../../domain/usecases/logout_usecase.dart';
import '../../domain/usecases/refresh_session_usecase.dart';
import '../../domain/usecases/register_usecase.dart';
import 'auth_state.dart';

class AuthCubit extends Cubit<AuthState> {
  AuthCubit({
    required LoginUseCase loginUseCase,
    required RegisterUseCase registerUseCase,
    required ForgotPasswordUseCase forgotPasswordUseCase,
    required RefreshSessionUseCase refreshSessionUseCase,
    required LogoutUseCase logoutUseCase,
  })  : _loginUseCase = loginUseCase,
        _registerUseCase = registerUseCase,
        _forgotPasswordUseCase = forgotPasswordUseCase,
        _refreshSessionUseCase = refreshSessionUseCase,
        _logoutUseCase = logoutUseCase,
        super(const AuthState());

  final LoginUseCase _loginUseCase;
  final RegisterUseCase _registerUseCase;
  final ForgotPasswordUseCase _forgotPasswordUseCase;
  final RefreshSessionUseCase _refreshSessionUseCase;
  final LogoutUseCase _logoutUseCase;

  Future<void> restoreSession() async {
    emit(state.copyWith(status: AuthStatus.loading, clearFailure: true));
    try {
      final session = await _refreshSessionUseCase.restore();
      if (session == null) {
        emit(
          state.copyWith(
            status: AuthStatus.unauthenticated,
            clearSession: true,
            clearMessage: true,
          ),
        );
        return;
      }

      emit(
        state.copyWith(
          status: AuthStatus.authenticated,
          session: session,
          clearFailure: true,
          clearMessage: true,
        ),
      );
    } catch (_) {
      emit(
        state.copyWith(
          status: AuthStatus.unauthenticated,
          clearSession: true,
        ),
      );
    }
  }

  Future<void> login({
    required String identifier,
    required String password,
  }) async {
    emit(state.copyWith(status: AuthStatus.loading, clearFailure: true));
    try {
      final session = await _loginUseCase(
        identifier: identifier,
        password: password,
      );
      emit(
        state.copyWith(
          status: AuthStatus.authenticated,
          session: session,
          clearFailure: true,
          clearMessage: true,
        ),
      );
    } on Failure catch (failure) {
      emit(
        state.copyWith(
          status: AuthStatus.failure,
          failure: failure,
        ),
      );
    } catch (_) {
      emit(
        state.copyWith(
          status: AuthStatus.failure,
          failure: const Failure(
            code: FailureCode.networkError,
            message: 'Không thể kết nối máy chủ. Kiểm tra backend và mạng.',
          ),
        ),
      );
    }
  }

  Future<void> register(RegisterParams params) async {
    emit(state.copyWith(status: AuthStatus.loading, clearFailure: true));
    try {
      final message = await _registerUseCase(params);
      emit(
        state.copyWith(
          status: AuthStatus.actionSuccess,
          message: message,
          clearFailure: true,
        ),
      );
    } on Failure catch (failure) {
      emit(
        state.copyWith(
          status: AuthStatus.failure,
          failure: failure,
        ),
      );
    } catch (_) {
      emit(
        state.copyWith(
          status: AuthStatus.failure,
          failure: const Failure(
            code: FailureCode.networkError,
            message: 'Không thể kết nối máy chủ. Kiểm tra backend và mạng.',
          ),
        ),
      );
    }
  }

  Future<void> forgotPassword({required String email}) async {
    emit(state.copyWith(status: AuthStatus.loading, clearFailure: true));
    try {
      await _forgotPasswordUseCase(email: email);
      emit(
        state.copyWith(
          status: AuthStatus.actionSuccess,
          message:
              'Nếu email tồn tại trong hệ thống, bạn sẽ nhận được hướng dẫn khôi phục.',
          clearFailure: true,
        ),
      );
    } on Failure catch (failure) {
      emit(
        state.copyWith(
          status: AuthStatus.failure,
          failure: failure,
        ),
      );
    } catch (_) {
      emit(
        state.copyWith(
          status: AuthStatus.failure,
          failure: const Failure(
            code: FailureCode.networkError,
            message: 'Không thể kết nối máy chủ. Kiểm tra backend và mạng.',
          ),
        ),
      );
    }
  }

  Future<void> logout() async {
    emit(state.copyWith(status: AuthStatus.loading, clearFailure: true));
    try {
      await _logoutUseCase();
    } finally {
      emit(const AuthState(status: AuthStatus.unauthenticated));
    }
  }

  void markUnauthenticated() {
    emit(const AuthState(status: AuthStatus.unauthenticated));
  }

  void clearTransientState() {
    if (state.status == AuthStatus.failure ||
        state.status == AuthStatus.actionSuccess) {
      emit(
        state.copyWith(
          status: state.session != null
              ? AuthStatus.authenticated
              : AuthStatus.unauthenticated,
          clearFailure: true,
          clearMessage: true,
        ),
      );
    }
  }
}
