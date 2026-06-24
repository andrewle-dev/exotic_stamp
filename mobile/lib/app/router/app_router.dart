import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../core/di/injection.dart';
import '../../features/app_shell/presentation/screens/main_shell_screen.dart';
import '../../features/auth/presentation/screens/forgot_password_screen.dart';
import '../../features/auth/presentation/screens/login_screen.dart';
import '../../features/auth/presentation/screens/register_screen.dart';
import '../../features/home/presentation/screens/home_screen.dart';
import '../../features/memories/domain/entities/photo_share_context.dart';
import '../../features/memories/presentation/screens/photo_share_screen.dart';
import '../../features/profile/presentation/screens/profile_screen.dart';
import '../../features/profile/presentation/screens/settings_screen.dart';
import '../../features/rewards/presentation/screens/rewards_screen.dart';
import '../../features/rewards/presentation/screens/voucher_detail_screen.dart';
import '../../features/scan/presentation/screens/location_verification_screen.dart';
import '../../features/scan/presentation/screens/scan_error_screen.dart';
import '../../features/scan/presentation/screens/scan_screen.dart';
import '../../features/scan/presentation/screens/stamp_collected_success_screen.dart';
import '../../features/scan/presentation/widgets/scan_flow_scope.dart';
import '../../features/stamp_book/presentation/screens/stamp_book_screen.dart';
import '../../features/stamp_book/presentation/screens/stamp_detail_screen.dart';
import '../../features/stations/presentation/screens/station_detail_screen.dart';
import '../../features/stations/presentation/screens/stations_screen.dart';
import '../theme/app_colors.dart';
import '../theme/app_spacing.dart';
import '../theme/app_text_styles.dart';
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
        builder: (context, state) => const WelcomePlaceholderScreen(),
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
        path: RouteNames.settings,
        builder: (context, state) => const SettingsScreen(),
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

/// Temporary welcome screen until onboarding feature is implemented.
class WelcomePlaceholderScreen extends StatelessWidget {
  const WelcomePlaceholderScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundWhite,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(AppSpacing.xl),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Spacer(),
              Text(
                'Exotic Stamp',
                style: AppTextStyles.headlineLarge.copyWith(
                  color: AppColors.primaryBlue,
                ),
              ),
              const SizedBox(height: AppSpacing.md),
              const Text(
                'Chạm NFC để nhận Stamp tại các ga metro.',
                style: AppTextStyles.bodyLarge,
              ),
              const SizedBox(height: AppSpacing.sm),
              const Text(
                'QR chỉ dùng khi NFC không khả dụng.',
                style: AppTextStyles.bodyMedium,
              ),
              const Spacer(),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: () async {
                    await Injection.instance.localPreferences
                        .setOnboardingCompleted(value: true);
                    Injection.instance.notifySessionChanged();
                    if (context.mounted) {
                      context.go(RouteNames.login);
                    }
                  },
                  child: const Text('Bắt đầu'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
