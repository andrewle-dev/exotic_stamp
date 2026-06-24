import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../core/di/injection.dart';
import 'route_names.dart';

/// Redirect logic for onboarding and authentication guards.
abstract final class RouteGuards {
  static Future<String?> redirect(
    BuildContext context,
    GoRouterState state,
  ) async {
    final injection = Injection.instance;
    final location = state.matchedLocation;

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

    if (!hasToken) {
      return RouteNames.login;
    }

    return null;
  }
}
