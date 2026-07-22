import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../core/di/injection.dart';
import '../../features/app_config/domain/entities/app_update_decision.dart';
import '../../features/auth/presentation/cubit/auth_state.dart';
import 'route_names.dart';

/// Redirect logic for onboarding, update policy, and authentication guards.
abstract final class RouteGuards {
  static bool _isUnauthenticated(Injection injection) {
    return injection.authCubit.state.status == AuthStatus.unauthenticated;
  }

  static Future<String?> redirect(
    BuildContext context,
    GoRouterState state,
  ) async {
    final injection = Injection.instance;
    final location = state.matchedLocation;
    final decision = injection.appUpdateDecision;

    if (decision.type == AppUpdateDecisionType.forceUpdate) {
      return location == RouteNames.forceUpdate ? null : RouteNames.forceUpdate;
    }

    if (decision.type == AppUpdateDecisionType.maintenance) {
      return location == RouteNames.maintenance ? null : RouteNames.maintenance;
    }

    if (location == RouteNames.forceUpdate ||
        location == RouteNames.maintenance) {
      final onboardingDone = injection.localPreferences.onboardingCompleted;
      if (!onboardingDone) {
        return RouteNames.welcome;
      }
      if (_isUnauthenticated(injection)) {
        return RouteNames.login;
      }
      final hasToken = await injection.tokenStorage.hasAccessToken();
      return hasToken ? RouteNames.home : RouteNames.login;
    }

    final onboardingDone = injection.localPreferences.onboardingCompleted;

    if (!onboardingDone) {
      return location == RouteNames.welcome ? null : RouteNames.welcome;
    }

    if (location == RouteNames.welcome) {
      return RouteNames.login;
    }

    final hasToken = await injection.tokenStorage.hasAccessToken();

    if (hasToken &&
        (location == RouteNames.login || location == RouteNames.auth)) {
      return RouteNames.home;
    }

    if (RouteNames.publicRoutes.contains(location)) {
      return null;
    }

    if (_isUnauthenticated(injection) || !hasToken) {
      return RouteNames.login;
    }

    return null;
  }
}
