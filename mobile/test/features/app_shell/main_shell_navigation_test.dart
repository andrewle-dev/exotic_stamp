import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:go_router/go_router.dart';
import 'package:metro_stamp_app/app/router/app_router.dart';
import 'package:metro_stamp_app/app/router/route_names.dart';
import 'package:metro_stamp_app/app/theme/app_colors.dart';
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

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late MockSecureTokenStorage tokenStorage;
  late GoRouter router;

  setUp(() async {
    Injection.instance.reset();
    SharedPreferences.setMockInitialValues({'onboarding_completed': true});
    final localPreferences = LocalPreferences();
    await localPreferences.init();

    tokenStorage = MockSecureTokenStorage();
    when(() => tokenStorage.hasAccessToken()).thenAnswer((_) async => true);
    when(() => tokenStorage.readAccessToken()).thenAnswer((_) async => 'token');

    router = createAppRouter();

    await Injection.instance.init(
      tokenStorageOverride: tokenStorage,
      localPreferencesOverride: localPreferences,
      apiClientOverride: MockApiClient(),
      authRepositoryOverride: MockAuthRepository(),
      authCubitOverride: AuthCubit(
        loginUseCase: LoginUseCase(MockAuthRepository()),
        registerUseCase: RegisterUseCase(MockAuthRepository()),
        forgotPasswordUseCase: ForgotPasswordUseCase(MockAuthRepository()),
        refreshSessionUseCase: RefreshSessionUseCase(MockAuthRepository()),
        logoutUseCase: LogoutUseCase(MockAuthRepository()),
      ),
      routerOverride: router,
      restoreSession: false,
    );
  });

  tearDown(() {
    Injection.instance.reset();
  });

  Future<void> pumpShell(WidgetTester tester) async {
    tester.view.physicalSize = const Size(1080, 2400);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);

    await tester.pumpWidget(
      MaterialApp.router(routerConfig: router),
    );
    router.go(RouteNames.profile);
    await tester.pump();
  }

  testWidgets('shell tab tap navigates to matching route',
      (WidgetTester tester) async {
    await pumpShell(tester);

    await tester.tap(find.text('Stations'));
    await tester.pump();
    expect(router.state.matchedLocation, RouteNames.stations);

    await tester.tap(find.text('Rewards'));
    await tester.pump();
    expect(router.state.matchedLocation, RouteNames.rewards);

    await tester.tap(find.text('Book'));
    await tester.pump();
    expect(router.state.matchedLocation, RouteNames.stampBook);
  });

  testWidgets('active tab highlights stations after navigation',
      (WidgetTester tester) async {
    await pumpShell(tester);

    final stationsTab = find.descendant(
      of: find.byType(BottomAppBar),
      matching: find.text('Stations'),
    );
    await tester.tap(stationsTab);
    await tester.pump();

    final stationsLabel = tester.widget<Text>(stationsTab);
    expect(stationsLabel.style?.color, AppColors.primaryBlue);
  });

  testWidgets('settings route is outside shell tab routes',
      (WidgetTester tester) async {
    await pumpShell(tester);

    router.go(RouteNames.settings);
    await tester.pump();

    expect(router.state.matchedLocation, RouteNames.settings);
    expect(RouteNames.shellRoutes.contains(RouteNames.settings), isFalse);
  });

  testWidgets('shell pages render a single bottom navigation bar',
      (WidgetTester tester) async {
    await pumpShell(tester);

    expect(find.byType(BottomAppBar), findsOneWidget);
  });

  testWidgets('center scan action opens scan route',
      (WidgetTester tester) async {
    await pumpShell(tester);

    await tester.tap(find.byType(FloatingActionButton));
    await tester.pump();

    expect(router.state.matchedLocation, RouteNames.scan);
  });

  testWidgets('home and stations shell routes resolve correctly',
      (WidgetTester tester) async {
    await pumpShell(tester);

    router.go(RouteNames.home);
    await tester.pump();
    expect(router.state.matchedLocation, RouteNames.home);

    router.go(RouteNames.stations);
    await tester.pump();
    expect(router.state.matchedLocation, RouteNames.stations);
  });

  testWidgets('station detail route resolves outside shell',
      (WidgetTester tester) async {
    await pumpShell(tester);

    router.push(RouteNames.stationDetail('station-1'));
    await tester.pump();
    expect(router.state.matchedLocation, '/stations/station-1');
  });
}
