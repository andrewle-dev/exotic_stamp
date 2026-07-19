import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:go_router/go_router.dart';
import 'package:metro_stamp_app/app/router/route_names.dart';
import 'package:metro_stamp_app/core/di/injection.dart';
import 'package:metro_stamp_app/core/errors/failure.dart';
import 'package:metro_stamp_app/core/network/api_client.dart';
import 'package:metro_stamp_app/core/storage/local_preferences.dart';
import 'package:metro_stamp_app/core/storage/secure_token_storage.dart';
import 'package:metro_stamp_app/features/auth/domain/usecases/change_password_usecase.dart';
import 'package:metro_stamp_app/features/auth/presentation/cubit/change_password_cubit.dart';
import 'package:metro_stamp_app/features/auth/presentation/cubit/change_password_state.dart';
import 'package:metro_stamp_app/features/auth/presentation/cubit/auth_cubit.dart';
import 'package:metro_stamp_app/features/auth/presentation/cubit/auth_state.dart';
import 'package:metro_stamp_app/features/auth/presentation/screens/change_password_screen.dart';
import 'package:bloc_test/bloc_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:shared_preferences/shared_preferences.dart';

class MockChangePasswordCubit extends MockCubit<ChangePasswordState>
    implements ChangePasswordCubit {}

class MockAuthCubit extends MockCubit<AuthState> implements AuthCubit {}

class MockApiClient extends Mock implements ApiClient {}

class MockSecureTokenStorage extends Mock implements SecureTokenStorage {}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late MockChangePasswordCubit changePasswordCubit;
  late MockAuthCubit authCubit;
  late MockApiClient apiClient;

  setUp(() async {
    Injection.instance.reset();
    SharedPreferences.setMockInitialValues({});
    final localPreferences = LocalPreferences();
    await localPreferences.init();

    changePasswordCubit = MockChangePasswordCubit();
    authCubit = MockAuthCubit();
    apiClient = MockApiClient();

    await Injection.instance.init(
      localPreferencesOverride: localPreferences,
      apiClientOverride: apiClient,
      tokenStorageOverride: MockSecureTokenStorage(),
      authCubitOverride: authCubit,
      restoreSession: false,
    );

    when(() => changePasswordCubit.state).thenReturn(const ChangePasswordState());
    when(() => changePasswordCubit.isClosed).thenReturn(false);
    when(() => changePasswordCubit.close()).thenAnswer((_) async {});
    when(() => changePasswordCubit.submit(
          currentPassword: any(named: 'currentPassword'),
          newPassword: any(named: 'newPassword'),
          confirmNewPassword: any(named: 'confirmNewPassword'),
        )).thenAnswer((_) async {});
    when(() => authCubit.markUnauthenticated()).thenReturn(null);
    when(() => authCubit.isClosed).thenReturn(false);
    when(() => authCubit.close()).thenAnswer((_) async {});
    when(() => apiClient.clearSession()).thenAnswer((_) async {});
  });

  tearDown(() {
    Injection.instance.reset();
  });

  testWidgets('form validates confirmation mismatch without calling cubit',
      (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: ChangePasswordScreen(cubit: changePasswordCubit),
      ),
    );
    await tester.pumpAndSettle();

    await tester.enterText(
      find.byType(TextFormField).at(0),
      'CurrentPass1!',
    );
    await tester.enterText(
      find.byType(TextFormField).at(1),
      'NewSecurePass2!',
    );
    await tester.enterText(
      find.byType(TextFormField).at(2),
      'DifferentPass3!',
    );
    await tester.tap(find.text('Change password'));
    await tester.pump();

    expect(
      find.text('New password and confirmation do not match.'),
      findsOneWidget,
    );
    verifyNever(
      () => changePasswordCubit.submit(
        currentPassword: any(named: 'currentPassword'),
        newPassword: any(named: 'newPassword'),
        confirmNewPassword: any(named: 'confirmNewPassword'),
      ),
    );
  });

  testWidgets('success clears session and redirects to login', (tester) async {
    whenListen(
      changePasswordCubit,
      Stream.fromIterable([
        const ChangePasswordState(status: ChangePasswordStatus.submitting),
        const ChangePasswordState(status: ChangePasswordStatus.success),
      ]),
      initialState: const ChangePasswordState(),
    );

    final router = GoRouter(
      initialLocation: RouteNames.changePassword,
      routes: [
        GoRoute(
          path: RouteNames.changePassword,
          builder: (context, state) =>
              ChangePasswordScreen(cubit: changePasswordCubit),
        ),
        GoRoute(
          path: RouteNames.login,
          builder: (context, state) =>
              const Scaffold(body: Text('Login Screen')),
        ),
      ],
    );

    await tester.pumpWidget(MaterialApp.router(routerConfig: router));
    await tester.pumpAndSettle();

    expect(find.text('Login Screen'), findsOneWidget);
    verify(() => apiClient.clearSession()).called(1);
    verify(() => authCubit.markUnauthenticated()).called(1);
  });

  testWidgets('maps backend current-password error', (tester) async {
    whenListen(
      changePasswordCubit,
      Stream.fromIterable([
        const ChangePasswordState(status: ChangePasswordStatus.submitting),
        const ChangePasswordState(
          status: ChangePasswordStatus.failure,
          failure: Failure(
            code: FailureCode.invalidCredentials,
            message: 'Current password is incorrect',
            backendCode: 'CURRENT_PASSWORD_INCORRECT',
          ),
        ),
      ]),
      initialState: const ChangePasswordState(),
    );

    await tester.pumpWidget(
      MaterialApp(
        home: ChangePasswordScreen(cubit: changePasswordCubit),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Current password is incorrect.'), findsOneWidget);
  });
}
