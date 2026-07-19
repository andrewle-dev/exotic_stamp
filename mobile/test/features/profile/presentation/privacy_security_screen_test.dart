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
import 'package:metro_stamp_app/features/profile/presentation/cubit/privacy_security_cubit.dart';
import 'package:metro_stamp_app/features/profile/presentation/cubit/privacy_security_state.dart';
import 'package:metro_stamp_app/features/profile/presentation/screens/privacy_security_screen.dart';
import 'package:mocktail/mocktail.dart';
import 'package:shared_preferences/shared_preferences.dart';

class MockPrivacySecurityCubit extends MockCubit<PrivacySecurityState>
    implements PrivacySecurityCubit {}

class MockAuthCubit extends MockCubit<AuthState> implements AuthCubit {}

class MockApiClient extends Mock implements ApiClient {}

class MockSecureTokenStorage extends Mock implements SecureTokenStorage {}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late MockPrivacySecurityCubit privacyCubit;
  late MockAuthCubit authCubit;

  setUp(() async {
    Injection.instance.reset();
    SharedPreferences.setMockInitialValues({});
    final localPreferences = LocalPreferences();
    await localPreferences.init();

    privacyCubit = MockPrivacySecurityCubit();
    authCubit = MockAuthCubit();

    await Injection.instance.init(
      localPreferencesOverride: localPreferences,
      apiClientOverride: MockApiClient(),
      tokenStorageOverride: MockSecureTokenStorage(),
      authCubitOverride: authCubit,
      restoreSession: false,
    );

    when(() => privacyCubit.state).thenReturn(
      const PrivacySecurityState(
        status: PrivacySecurityStatus.loaded,
        profile: Profile(
          id: 'user-1',
          email: 'an@example.com',
          username: 'an.nguyen',
          firstname: 'An',
          lastname: 'Nguyen',
        ),
      ),
    );
    when(() => privacyCubit.load()).thenAnswer((_) async {});
    when(() => privacyCubit.logout()).thenAnswer((_) async {});
    when(() => privacyCubit.logoutAll()).thenAnswer((_) async {});
    when(() => authCubit.markUnauthenticated()).thenReturn(null);
    when(() => authCubit.isClosed).thenReturn(false);
    when(() => authCubit.close()).thenAnswer((_) async {});
  });

  tearDown(() {
    Injection.instance.reset();
  });

  testWidgets('shows change password entry', (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: PrivacySecurityScreen(cubit: privacyCubit),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Change Password'), findsOneWidget);
    expect(find.text('Update your account password'), findsOneWidget);
    expect(find.text('Coming soon'), findsNothing);
    expect(find.text('an@example.com'), findsOneWidget);
  });

  testWidgets('logout this device navigates to /login', (tester) async {
    final router = GoRouter(
      initialLocation: RouteNames.privacySecurity,
      routes: [
        GoRoute(
          path: RouteNames.privacySecurity,
          builder: (context, state) =>
              PrivacySecurityScreen(cubit: privacyCubit),
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

    await tester.tap(find.text('Log out this device'));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Log Out').last);
    await tester.pumpAndSettle();

    expect(find.text('Login Screen'), findsOneWidget);
    verify(() => privacyCubit.logout()).called(1);
    verify(() => authCubit.markUnauthenticated()).called(1);
  });

  testWidgets('logout all devices navigates to /login', (tester) async {
    final router = GoRouter(
      initialLocation: RouteNames.privacySecurity,
      routes: [
        GoRoute(
          path: RouteNames.privacySecurity,
          builder: (context, state) =>
              PrivacySecurityScreen(cubit: privacyCubit),
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

    await tester.tap(find.text('Log out all devices'));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Log Out All'));
    await tester.pumpAndSettle();

    expect(find.text('Login Screen'), findsOneWidget);
    verify(() => privacyCubit.logoutAll()).called(1);
    verify(() => authCubit.markUnauthenticated()).called(1);
  });
}
