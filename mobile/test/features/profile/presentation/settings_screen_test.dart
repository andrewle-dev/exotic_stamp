import 'package:bloc_test/bloc_test.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:go_router/go_router.dart';
import 'package:metro_stamp_app/app/router/route_names.dart';
import 'package:metro_stamp_app/core/di/injection.dart';
import 'package:metro_stamp_app/core/network/api_client.dart';
import 'package:metro_stamp_app/core/storage/local_preferences.dart';
import 'package:metro_stamp_app/core/storage/secure_token_storage.dart';
import 'package:metro_stamp_app/features/auth/presentation/cubit/auth_cubit.dart';
import 'package:metro_stamp_app/features/auth/presentation/cubit/auth_state.dart';
import 'package:metro_stamp_app/features/profile/domain/entities/profile.dart';
import 'package:metro_stamp_app/features/profile/presentation/cubit/settings_cubit.dart';
import 'package:metro_stamp_app/features/profile/presentation/cubit/settings_state.dart';
import 'package:metro_stamp_app/features/profile/presentation/screens/settings_screen.dart';
import 'package:mocktail/mocktail.dart';
import 'package:shared_preferences/shared_preferences.dart';

class MockSettingsCubit extends MockCubit<SettingsState>
    implements SettingsCubit {}

class MockAuthCubit extends MockCubit<AuthState> implements AuthCubit {}

class MockApiClient extends Mock implements ApiClient {}

class MockSecureTokenStorage extends Mock implements SecureTokenStorage {}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late MockSettingsCubit settingsCubit;
  late MockAuthCubit authCubit;

  setUp(() async {
    Injection.instance.reset();
    SharedPreferences.setMockInitialValues({});
    final localPreferences = LocalPreferences();
    await localPreferences.init();

    settingsCubit = MockSettingsCubit();
    authCubit = MockAuthCubit();

    await Injection.instance.init(
      localPreferencesOverride: localPreferences,
      apiClientOverride: MockApiClient(),
      tokenStorageOverride: MockSecureTokenStorage(),
      authCubitOverride: authCubit,
      restoreSession: false,
    );

    when(() => settingsCubit.state).thenReturn(
      const SettingsState(
        status: SettingsStatus.loaded,
        profile: Profile(
          id: 'user-1',
          email: 'an@example.com',
          username: 'an.nguyen',
          firstname: 'An',
          lastname: 'Nguyen',
        ),
      ),
    );
    when(() => settingsCubit.load()).thenAnswer((_) async {});
    when(() => settingsCubit.logout()).thenAnswer((_) async {});
    when(() => authCubit.markUnauthenticated()).thenReturn(null);
    when(() => authCubit.isClosed).thenReturn(false);
    when(() => authCubit.close()).thenAnswer((_) async {});
  });

  tearDown(() {
    Injection.instance.reset();
  });

  testWidgets('logout navigates to /login', (tester) async {
    final router = GoRouter(
      initialLocation: RouteNames.settings,
      routes: [
        GoRoute(
          path: RouteNames.settings,
          builder: (context, state) => SettingsScreen(cubit: settingsCubit),
        ),
        GoRoute(
          path: RouteNames.login,
          builder: (context, state) =>
              const Scaffold(body: Text('Login Screen')),
        ),
      ],
    );

    await tester.pumpWidget(
      MaterialApp.router(routerConfig: router),
    );
    await tester.pumpAndSettle();

    final logoutFinder = find.descendant(
      of: find.byType(SingleChildScrollView),
      matching: find.text('Đăng xuất'),
    );
    await tester.ensureVisible(logoutFinder);
    await tester.pumpAndSettle();
    await tester.tap(logoutFinder);
    await tester.pumpAndSettle();

    await tester.tap(find.text('Đăng xuất').last);
    await tester.pumpAndSettle();

    expect(find.text('Login Screen'), findsOneWidget);
    verify(() => settingsCubit.logout()).called(1);
    verify(() => authCubit.markUnauthenticated()).called(1);
  });
}
