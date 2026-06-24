import 'package:bloc_test/bloc_test.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/di/injection.dart';
import 'package:metro_stamp_app/core/network/api_client.dart';
import 'package:metro_stamp_app/core/storage/local_preferences.dart';
import 'package:metro_stamp_app/core/storage/secure_token_storage.dart';
import 'package:metro_stamp_app/features/auth/domain/repositories/auth_repository.dart';
import 'package:metro_stamp_app/features/auth/domain/usecases/forgot_password_usecase.dart';
import 'package:metro_stamp_app/features/auth/domain/usecases/login_usecase.dart';
import 'package:metro_stamp_app/features/auth/domain/usecases/logout_usecase.dart';
import 'package:metro_stamp_app/features/auth/domain/usecases/refresh_session_usecase.dart';
import 'package:metro_stamp_app/features/auth/domain/usecases/register_usecase.dart';
import 'package:metro_stamp_app/features/auth/presentation/cubit/auth_cubit.dart';
import 'package:metro_stamp_app/features/auth/presentation/cubit/auth_state.dart';
import 'package:metro_stamp_app/features/auth/presentation/screens/forgot_password_screen.dart';
import 'package:metro_stamp_app/features/auth/presentation/screens/login_screen.dart';
import 'package:metro_stamp_app/features/auth/presentation/screens/register_screen.dart';
import 'package:mocktail/mocktail.dart';
import 'package:shared_preferences/shared_preferences.dart';

class MockAuthRepository extends Mock implements AuthRepository {}

class MockSecureTokenStorage extends Mock implements SecureTokenStorage {}

class MockApiClient extends Mock implements ApiClient {}

class MockAuthCubit extends MockCubit<AuthState> implements AuthCubit {}

Widget _wrapWithAuthCubit(Widget child, AuthCubit authCubit) {
  return BlocProvider<AuthCubit>.value(
    value: authCubit,
    child: MaterialApp(home: child),
  );
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late MockAuthRepository authRepository;
  late MockSecureTokenStorage tokenStorage;
  late MockApiClient apiClient;
  late AuthCubit authCubit;

  setUp(() async {
    Injection.instance.reset();
    SharedPreferences.setMockInitialValues({'onboarding_completed': true});

    authRepository = MockAuthRepository();
    tokenStorage = MockSecureTokenStorage();
    apiClient = MockApiClient();

    when(() => tokenStorage.hasAccessToken()).thenAnswer((_) async => false);
    when(() => authRepository.hasStoredSession())
        .thenAnswer((_) async => false);
    when(() => apiClient.clearSession()).thenAnswer((_) async {});

    final localPreferences = LocalPreferences();
    await localPreferences.init();

    authCubit = AuthCubit(
      loginUseCase: LoginUseCase(authRepository),
      registerUseCase: RegisterUseCase(authRepository),
      forgotPasswordUseCase: ForgotPasswordUseCase(authRepository),
      refreshSessionUseCase: RefreshSessionUseCase(authRepository),
      logoutUseCase: LogoutUseCase(authRepository),
    );

    await Injection.instance.init(
      tokenStorageOverride: tokenStorage,
      localPreferencesOverride: localPreferences,
      apiClientOverride: apiClient,
      authRepositoryOverride: authRepository,
      authCubitOverride: authCubit,
      restoreSession: false,
    );
  });

  tearDown(() {
    Injection.instance.reset();
  });

  group('LoginScreen', () {
    testWidgets('submit can access AuthCubit without ProviderNotFoundException',
        (tester) async {
      when(
        () => authRepository.login(
          identifier: any(named: 'identifier'),
          password: any(named: 'password'),
        ),
      ).thenAnswer(
        (_) async => throw Exception('login invoked'),
      );

      await tester.pumpWidget(
        _wrapWithAuthCubit(const LoginScreen(), authCubit),
      );
      await tester.pumpAndSettle();

      await tester.enterText(
        find.byType(TextField).at(0),
        'user@example.com',
      );
      await tester.enterText(
        find.byType(TextField).at(1),
        'password123',
      );

      await tester.tap(find.text('Đăng nhập'));
      await tester.pump();

      expect(tester.takeException(), isNull);
      verify(
        () => authRepository.login(
          identifier: 'user@example.com',
          password: 'password123',
        ),
      ).called(1);
    });
  });

  group('RegisterScreen', () {
    testWidgets('submit can access AuthCubit without ProviderNotFoundException',
        (tester) async {
      tester.view.physicalSize = const Size(800, 1600);
      tester.view.devicePixelRatio = 1;
      addTearDown(tester.view.resetPhysicalSize);
      addTearDown(tester.view.resetDevicePixelRatio);

      when(
        () => authRepository.register(
          firstname: any(named: 'firstname'),
          lastname: any(named: 'lastname'),
          username: any(named: 'username'),
          email: any(named: 'email'),
          phoneNumber: any(named: 'phoneNumber'),
          password: any(named: 'password'),
        ),
      ).thenAnswer(
        (_) async => 'registered',
      );

      await tester.pumpWidget(
        _wrapWithAuthCubit(const RegisterScreen(), authCubit),
      );
      await tester.pumpAndSettle();

      await tester.enterText(find.byType(TextField).at(0), 'Nguyen Van A');
      await tester.enterText(find.byType(TextField).at(1), 'user@example.com');
      await tester.enterText(find.byType(TextField).at(2), '901234567');
      await tester.enterText(find.byType(TextField).at(3), 'password123');
      await tester.enterText(find.byType(TextField).at(4), 'password123');
      await tester.ensureVisible(find.byType(Checkbox));
      await tester.tap(find.byType(Checkbox));
      await tester.pumpAndSettle();

      await tester.ensureVisible(find.text('Đăng Ký Ngay'));
      await tester.tap(find.text('Đăng Ký Ngay'));
      await tester.pump();

      expect(tester.takeException(), isNull);
      verify(
        () => authRepository.register(
          firstname: any(named: 'firstname'),
          lastname: any(named: 'lastname'),
          username: any(named: 'username'),
          email: 'user@example.com',
          phoneNumber: any(named: 'phoneNumber'),
          password: 'password123',
        ),
      ).called(1);
    });
  });

  group('ForgotPasswordScreen', () {
    testWidgets('submit can access AuthCubit without ProviderNotFoundException',
        (tester) async {
      when(
        () => authRepository.forgotPassword(email: any(named: 'email')),
      ).thenAnswer(
        (_) async => throw Exception('forgotPassword invoked'),
      );

      await tester.pumpWidget(
        _wrapWithAuthCubit(const ForgotPasswordScreen(), authCubit),
      );
      await tester.pumpAndSettle();

      await tester.enterText(
        find.byType(TextField),
        'user@example.com',
      );

      await tester.tap(find.text('Gửi mã xác thực'));
      await tester.pump();

      expect(tester.takeException(), isNull);
      verify(
        () => authRepository.forgotPassword(email: 'user@example.com'),
      ).called(1);
    });
  });
}
