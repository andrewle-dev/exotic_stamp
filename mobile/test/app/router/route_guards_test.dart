import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:go_router/go_router.dart';
import 'package:metro_stamp_app/app/router/route_guards.dart';
import 'package:metro_stamp_app/app/router/route_names.dart';
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
import 'package:mocktail/mocktail.dart';
import 'package:shared_preferences/shared_preferences.dart';

class MockAuthRepository extends Mock implements AuthRepository {}

class MockSecureTokenStorage extends Mock implements SecureTokenStorage {}

class MockApiClient extends Mock implements ApiClient {}

class FakeGoRouterState extends Fake implements GoRouterState {
  FakeGoRouterState(this.matchedLocation);

  @override
  final String matchedLocation;
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late MockAuthRepository authRepository;
  late MockSecureTokenStorage tokenStorage;
  late MockApiClient apiClient;
  late LocalPreferences localPreferences;

  setUp(() async {
    Injection.instance.reset();
    SharedPreferences.setMockInitialValues({'onboarding_completed': true});
    localPreferences = LocalPreferences();
    await localPreferences.init();

    authRepository = MockAuthRepository();
    tokenStorage = MockSecureTokenStorage();
    apiClient = MockApiClient();

    when(() => tokenStorage.hasAccessToken()).thenAnswer((_) async => false);
    when(() => authRepository.hasStoredSession())
        .thenAnswer((_) async => false);
    when(() => apiClient.clearSession()).thenAnswer((_) async {});

    await Injection.instance.init(
      tokenStorageOverride: tokenStorage,
      localPreferencesOverride: localPreferences,
      apiClientOverride: apiClient,
      authRepositoryOverride: authRepository,
      authCubitOverride: AuthCubit(
        loginUseCase: LoginUseCase(authRepository),
        registerUseCase: RegisterUseCase(authRepository),
        forgotPasswordUseCase: ForgotPasswordUseCase(authRepository),
        refreshSessionUseCase: RefreshSessionUseCase(authRepository),
        logoutUseCase: LogoutUseCase(authRepository),
      ),
      routerOverride: GoRouter(routes: []),
      restoreSession: false,
    );
  });

  tearDown(() {
    Injection.instance.reset();
  });

  test('redirects unauthenticated user away from protected route', () async {
    final redirect = await RouteGuards.redirect(
      _FakeBuildContext(),
      FakeGoRouterState(RouteNames.home),
    );

    expect(redirect, RouteNames.login);
  });

  test('allows public login route without token', () async {
    final redirect = await RouteGuards.redirect(
      _FakeBuildContext(),
      FakeGoRouterState(RouteNames.login),
    );

    expect(redirect, isNull);
  });

  test('allows register and forgot-password without redirect loop', () async {
    for (final route in [
      RouteNames.register,
      RouteNames.forgotPassword,
      RouteNames.auth,
    ]) {
      final redirect = await RouteGuards.redirect(
        _FakeBuildContext(),
        FakeGoRouterState(route),
      );
      expect(redirect, isNull, reason: '$route should stay public');
    }
  });

  test('redirects unauthenticated user away from all shell routes', () async {
    for (final route in RouteNames.shellTabRoutes) {
      final redirect = await RouteGuards.redirect(
        _FakeBuildContext(),
        FakeGoRouterState(route),
      );
      expect(redirect, RouteNames.login, reason: '$route should require auth');
    }
  });

  test('authenticated user on login redirects to home', () async {
    when(() => tokenStorage.hasAccessToken()).thenAnswer((_) async => true);

    final redirect = await RouteGuards.redirect(
      _FakeBuildContext(),
      FakeGoRouterState(RouteNames.login),
    );

    expect(redirect, RouteNames.home);
  });

  test('authenticated user can still open register without loop', () async {
    when(() => tokenStorage.hasAccessToken()).thenAnswer((_) async => true);

    final redirect = await RouteGuards.redirect(
      _FakeBuildContext(),
      FakeGoRouterState(RouteNames.register),
    );

    expect(redirect, isNull);
  });
}

class _FakeBuildContext extends Fake implements BuildContext {}
