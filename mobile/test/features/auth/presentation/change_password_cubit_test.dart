import 'package:bloc_test/bloc_test.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/errors/failure.dart';
import 'package:metro_stamp_app/features/auth/domain/usecases/change_password_usecase.dart';
import 'package:metro_stamp_app/features/auth/presentation/cubit/change_password_cubit.dart';
import 'package:metro_stamp_app/features/auth/presentation/cubit/change_password_state.dart';
import 'package:mocktail/mocktail.dart';

class MockChangePasswordUseCase extends Mock implements ChangePasswordUseCase {}

void main() {
  late MockChangePasswordUseCase useCase;
  late ChangePasswordCubit cubit;

  setUpAll(() {
    registerFallbackValue(
      const ChangePasswordParams(
        currentPassword: 'a',
        newPassword: 'b',
        confirmNewPassword: 'b',
      ),
    );
  });

  setUp(() {
    useCase = MockChangePasswordUseCase();
    cubit = ChangePasswordCubit(changePasswordUseCase: useCase);
  });

  tearDown(() async {
    await cubit.close();
  });

  blocTest<ChangePasswordCubit, ChangePasswordState>(
    'success emits submitting then success',
    build: () {
      when(() => useCase(any())).thenAnswer((_) async {});
      return cubit;
    },
    act: (c) => c.submit(
      currentPassword: 'CurrentPass1!',
      newPassword: 'NewSecurePass2!',
      confirmNewPassword: 'NewSecurePass2!',
    ),
    expect: () => [
      const ChangePasswordState(status: ChangePasswordStatus.submitting),
      const ChangePasswordState(status: ChangePasswordStatus.success),
    ],
  );

  blocTest<ChangePasswordCubit, ChangePasswordState>(
    'maps CURRENT_PASSWORD_INCORRECT failure',
    build: () {
      when(() => useCase(any())).thenThrow(
        const Failure(
          code: FailureCode.invalidCredentials,
          message: 'Current password is incorrect',
          backendCode: 'CURRENT_PASSWORD_INCORRECT',
        ),
      );
      return cubit;
    },
    act: (c) => c.submit(
      currentPassword: 'wrong',
      newPassword: 'NewSecurePass2!',
      confirmNewPassword: 'NewSecurePass2!',
    ),
    expect: () => [
      const ChangePasswordState(status: ChangePasswordStatus.submitting),
      isA<ChangePasswordState>()
          .having((s) => s.status, 'status', ChangePasswordStatus.failure)
          .having(
            (s) => s.failure?.backendCode,
            'backendCode',
            'CURRENT_PASSWORD_INCORRECT',
          ),
    ],
  );

  blocTest<ChangePasswordCubit, ChangePasswordState>(
    'ignores duplicate submit while submitting',
    build: () {
      when(() => useCase(any())).thenAnswer((_) async {
        await Future<void>.delayed(const Duration(milliseconds: 50));
      });
      return cubit;
    },
    act: (c) async {
      final first = c.submit(
        currentPassword: 'CurrentPass1!',
        newPassword: 'NewSecurePass2!',
        confirmNewPassword: 'NewSecurePass2!',
      );
      final second = c.submit(
        currentPassword: 'CurrentPass1!',
        newPassword: 'NewSecurePass2!',
        confirmNewPassword: 'NewSecurePass2!',
      );
      await Future.wait([first, second]);
    },
    verify: (_) {
      verify(() => useCase(any())).called(1);
    },
  );
}
