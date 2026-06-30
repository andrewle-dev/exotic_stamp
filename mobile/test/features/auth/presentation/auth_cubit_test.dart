import 'package:bloc_test/bloc_test.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/errors/failure.dart';
import 'package:metro_stamp_app/features/auth/domain/entities/session.dart';
import 'package:metro_stamp_app/features/auth/domain/entities/user.dart';
import 'package:metro_stamp_app/features/auth/domain/repositories/auth_repository.dart';
import 'package:metro_stamp_app/features/auth/domain/usecases/forgot_password_usecase.dart';
import 'package:metro_stamp_app/features/auth/domain/usecases/login_usecase.dart';
import 'package:metro_stamp_app/features/auth/domain/usecases/logout_usecase.dart';
import 'package:metro_stamp_app/features/auth/domain/usecases/refresh_session_usecase.dart';
import 'package:metro_stamp_app/features/auth/domain/usecases/register_usecase.dart';
import 'package:metro_stamp_app/features/auth/domain/usecases/resend_verification_otp_usecase.dart';
import 'package:metro_stamp_app/features/auth/domain/usecases/verify_account_usecase.dart';
import 'package:metro_stamp_app/features/auth/presentation/cubit/auth_cubit.dart';
import 'package:metro_stamp_app/features/auth/presentation/cubit/auth_state.dart';
import 'package:mocktail/mocktail.dart';

class MockAuthRepository extends Mock implements AuthRepository {}

void main() {
  late MockAuthRepository repository;
  late AuthCubit cubit;

  const session = Session(
    accessToken: 'token',
    user: User(
      id: 'user-1',
      email: 'an@example.com',
      username: 'an.nguyen',
    ),
  );

  setUp(() {
    repository = MockAuthRepository();
    cubit = AuthCubit(
      loginUseCase: LoginUseCase(repository),
      registerUseCase: RegisterUseCase(repository),
      forgotPasswordUseCase: ForgotPasswordUseCase(repository),
      verifyAccountUseCase: VerifyAccountUseCase(repository),
      resendVerificationOtpUseCase: ResendVerificationOtpUseCase(repository),
      refreshSessionUseCase: RefreshSessionUseCase(repository),
      logoutUseCase: LogoutUseCase(repository),
    );
  });

  tearDown(() => cubit.close());

  blocTest<AuthCubit, AuthState>(
    'emits authenticated on login success',
    build: () {
      when(
        () => repository.login(
          identifier: any(named: 'identifier'),
          password: any(named: 'password'),
        ),
      ).thenAnswer((_) async => session);
      return cubit;
    },
    act: (cubit) => cubit.login(
      identifier: 'an@example.com',
      password: 'secret',
    ),
    expect: () => [
      isA<AuthState>().having((s) => s.status, 'status', AuthStatus.loading),
      isA<AuthState>()
          .having((s) => s.status, 'status', AuthStatus.authenticated)
          .having((s) => s.session?.accessToken, 'token', 'token'),
    ],
  );

  blocTest<AuthCubit, AuthState>(
    'emits failure on invalid credentials',
    build: () {
      when(
        () => repository.login(
          identifier: any(named: 'identifier'),
          password: any(named: 'password'),
        ),
      ).thenThrow(
        const Failure(
          code: FailureCode.invalidCredentials,
          message: 'Invalid email or password',
        ),
      );
      return cubit;
    },
    act: (cubit) => cubit.login(
      identifier: 'an@example.com',
      password: 'wrong',
    ),
    expect: () => [
      isA<AuthState>().having((s) => s.status, 'status', AuthStatus.loading),
      isA<AuthState>()
          .having((s) => s.status, 'status', AuthStatus.failure)
          .having(
            (s) => s.failure?.code,
            'code',
            FailureCode.invalidCredentials,
          ),
    ],
  );
}
