import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../core/di/injection.dart';
import '../../features/app_config/presentation/screens/force_update_route_screen.dart';
import '../../features/app_config/presentation/screens/maintenance_route_screen.dart';
import '../../features/app_shell/presentation/screens/main_shell_screen.dart';
import '../../features/auth/presentation/screens/change_password_screen.dart';
import '../../features/auth/presentation/screens/forgot_password_screen.dart';
import '../../features/auth/presentation/screens/login_screen.dart';
import '../../features/auth/presentation/screens/register_screen.dart';
import '../../features/auth/presentation/screens/verify_account_otp_screen.dart';
import '../../features/home/presentation/screens/home_screen.dart';
import '../../features/rewards/domain/entities/reward_unlocked_share_payload.dart';
import '../../features/memories/domain/entities/photo_share_context.dart';
import '../../features/memories/presentation/screens/photo_share_screen.dart';
import '../../core/auth/role_gates.dart';
import '../../features/admin_nfc/presentation/screens/nfc_tag_writer_screen.dart';
import '../../features/debug/presentation/screens/api_debug_screen.dart';
import '../../features/profile/presentation/screens/help_center_screen.dart';
import '../../features/profile/presentation/screens/personal_information_screen.dart';
import '../../features/profile/presentation/screens/privacy_security_screen.dart';
import '../../features/profile/presentation/screens/profile_screen.dart';

import '../../features/rewards/presentation/screens/reward_unlocked_share_screen.dart';
import '../../features/rewards/presentation/screens/rewards_screen.dart';
import '../../features/rewards/presentation/screens/voucher_detail_screen.dart';
import '../../features/scan/presentation/screens/location_verification_screen.dart';
import '../../features/scan/presentation/screens/scan_error_screen.dart';
import '../../features/scan/presentation/screens/scan_screen.dart';
import '../../features/scan/presentation/screens/stamp_collected_success_screen.dart';
import '../../features/scan/presentation/screens/tap_to_collect_screen.dart';
import '../../features/scan/presentation/widgets/scan_flow_scope.dart';
import '../../features/stamp_book/presentation/screens/stamp_book_screen.dart';
import '../../features/stamp_book/presentation/screens/stamp_detail_screen.dart';
import '../../features/stations/presentation/screens/station_detail_screen.dart';
import '../../features/stations/presentation/screens/stations_screen.dart';
import '../../features/onboarding/presentation/screens/welcome_screen.dart';
import 'route_guards.dart';
import 'route_names.dart';

final _rootNavigatorKey = GlobalKey<NavigatorState>();

GoRouter createAppRouter() {
  return GoRouter(
    navigatorKey: _rootNavigatorKey,
    initialLocation: RouteNames.welcome,
    refreshListenable: Injection.instance.sessionListenable,
    redirect: RouteGuards.redirect,
    routes: [
      GoRoute(
        path: RouteNames.welcome,
        builder: (context, state) => const WelcomeScreen(),
      ),
      GoRoute(
        path: RouteNames.forceUpdate,
        builder: (context, state) => const ForceUpdateRouteScreen(),
      ),
      GoRoute(
        path: RouteNames.maintenance,
        builder: (context, state) => const MaintenanceRouteScreen(),
      ),
      GoRoute(
        path: RouteNames.login,
        builder: (context, state) => const LoginScreen(),
      ),
      GoRoute(
        path: RouteNames.auth,
        redirect: (context, state) => RouteNames.login,
      ),
      GoRoute(
        path: RouteNames.register,
        builder: (context, state) => const RegisterScreen(),
      ),
      GoRoute(
        path: RouteNames.forgotPassword,
        builder: (context, state) => const ForgotPasswordScreen(),
      ),
      GoRoute(
        path: RouteNames.verifyAccountOtp,
        builder: (context, state) {
          final email = state.uri.queryParameters['email'] ?? '';
          return VerifyAccountOtpScreen(initialEmail: email);
        },
      ),
      StatefulShellRoute.indexedStack(
        builder: (context, state, navigationShell) {
          return MainShellScreen(navigationShell: navigationShell);
        },
        branches: [
          _shellBranch(RouteNames.home, const HomeScreen()),
          _shellBranch(RouteNames.stampBook, const StampBookScreen()),
          _scanShellBranch(),
          _shellBranch(RouteNames.stations, const StationsScreen()),
          _shellBranch(RouteNames.rewards, const RewardsScreen()),
          _shellBranch(RouteNames.profile, const ProfileScreen()),
        ],
      ),
      GoRoute(
        path: '/stations/:stationId',
        parentNavigatorKey: _rootNavigatorKey,
        builder: (context, state) {
          final stationId = state.pathParameters['stationId']!;
          return StationDetailScreen(stationId: stationId);
        },
      ),
      GoRoute(
        path: '/stamps/:stationId',
        parentNavigatorKey: _rootNavigatorKey,
        builder: (context, state) {
          // MVP: stationId param — see RouteNames.stampDetail doc.
          final stationId = state.pathParameters['stationId']!;
          final lineId = state.extra as String?;
          return StampDetailScreen(
            stationId: stationId,
            lineId: lineId,
          );
        },
      ),
      GoRoute(
        path: '/rewards/vouchers/:voucherId',
        parentNavigatorKey: _rootNavigatorKey,
        builder: (context, state) {
          final voucherId = state.pathParameters['voucherId']!;
          return VoucherDetailScreen(voucherId: voucherId);
        },
      ),
      GoRoute(
        path: RouteNames.personalInformation,
        parentNavigatorKey: _rootNavigatorKey,
        builder: (context, state) => const PersonalInformationScreen(),
      ),
      GoRoute(
        path: RouteNames.privacySecurity,
        parentNavigatorKey: _rootNavigatorKey,
        builder: (context, state) => const PrivacySecurityScreen(),
      ),
      GoRoute(
        path: RouteNames.changePassword,
        parentNavigatorKey: _rootNavigatorKey,
        builder: (context, state) => const ChangePasswordScreen(),
      ),
      GoRoute(
        path: RouteNames.helpCenter,
        parentNavigatorKey: _rootNavigatorKey,
        builder: (context, state) => const HelpCenterScreen(),
      ),
      GoRoute(
        path: RouteNames.settings,
        redirect: (context, state) => RouteNames.personalInformation,
      ),
      GoRoute(
        path: RouteNames.apiDebug,
        parentNavigatorKey: _rootNavigatorKey,
        redirect: (context, state) {
          if (!kDebugMode) {
            return RouteNames.profile;
          }
          return null;
        },
        builder: (context, state) => const ApiDebugScreen(),
      ),
      GoRoute(
        path: RouteNames.adminNfcWriter,
        parentNavigatorKey: _rootNavigatorKey,
        redirect: (context, state) {
          final user = Injection.instance.authCubit.state.session?.user;
          if (!RoleGates.isAdmin(user)) {
            return RouteNames.profile;
          }
          return null;
        },
        builder: (context, state) => const NfcTagWriterScreen(),
      ),

      GoRoute(
        path: RouteNames.memoriesCreate,
        parentNavigatorKey: _rootNavigatorKey,
        builder: (context, state) {
          final shareContext = state.extra;
          return PhotoShareScreen(
            initialContext:
                shareContext is PhotoShareContext ? shareContext : null,
          );
        },
      ),
      GoRoute(
        path: RouteNames.rewardsShare,
        parentNavigatorKey: _rootNavigatorKey,
        builder: (context, state) {
          final payload = state.extra;
          return RewardUnlockedShareScreen(
            payload: payload is RewardUnlockedSharePayload ? payload : null,
          );
        },
      ),
      GoRoute(
        path: RouteNames.scanRewardUnlocked,
        parentNavigatorKey: _rootNavigatorKey,
        builder: (context, state) {
          final payload = state.extra;
          return RewardUnlockedShareScreen(
            payload: payload is RewardUnlockedSharePayload ? payload : null,
          );
        },
      ),
    ],
  );
}

StatefulShellBranch _shellBranch(String path, Widget child) {
  return StatefulShellBranch(
    routes: [
      GoRoute(
        path: path,
        pageBuilder: (context, state) => NoTransitionPage<void>(
          key: state.pageKey,
          child: child,
        ),
      ),
    ],
  );
}

StatefulShellBranch _scanShellBranch() {
  return StatefulShellBranch(
    routes: [
      GoRoute(
        path: RouteNames.scan,
        pageBuilder: (context, state) => NoTransitionPage<void>(
          key: state.pageKey,
          child: const ScanFlowScope(child: ScanScreen()),
        ),
        routes: [
          GoRoute(
            path: 'tap-to-collect',
            parentNavigatorKey: _rootNavigatorKey,
            builder: (context, state) => const ScanFlowScope(
              child: TapToCollectScreen(),
            ),
          ),
          GoRoute(
            path: 'location-verification',
            parentNavigatorKey: _rootNavigatorKey,
            builder: (context, state) => const LocationVerificationScreen(),
          ),
          GoRoute(
            path: 'success',
            parentNavigatorKey: _rootNavigatorKey,
            builder: (context, state) => const StampCollectedSuccessScreen(),
          ),
          GoRoute(
            path: 'error',
            parentNavigatorKey: _rootNavigatorKey,
            builder: (context, state) => const ScanErrorScreen(),
          ),
        ],
      ),
    ],
  );
}
