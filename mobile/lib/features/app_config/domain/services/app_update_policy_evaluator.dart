import 'package:flutter/foundation.dart';

import '../../../../core/utils/semantic_version.dart';
import '../entities/app_update_decision.dart';
import '../entities/mobile_app_config.dart';
import '../entities/platform_version_policy.dart';

/// Evaluates installed binary version against backend update/maintenance policy.
abstract final class AppUpdatePolicyEvaluator {
  static AppUpdateDecision evaluate({
    required String installedVersion,
    required MobileAppConfig config,
    TargetPlatform? platformOverride,
  }) {
    if (config.maintenance.enabled) {
      return AppUpdateDecision.maintenance(
        installedVersion: installedVersion,
        message: config.maintenance.message,
      );
    }

    final policy = selectPlatformPolicy(
      config,
      platformOverride: platformOverride,
    );

    if (policy.forceUpdate ||
        SemanticVersion.isBelow(
          installedVersion,
          policy.minimumSupportedVersion,
        )) {
      return AppUpdateDecision.forceUpdate(
        installedVersion: installedVersion,
        policy: policy,
      );
    }

    if (SemanticVersion.isBelow(installedVersion, policy.latestVersion)) {
      return AppUpdateDecision.optionalUpdate(
        installedVersion: installedVersion,
        policy: policy,
      );
    }

    return AppUpdateDecision(
      type: AppUpdateDecisionType.supported,
      installedVersion: installedVersion,
      policy: policy,
      storeUrl: policy.storeUrl,
      latestVersion: policy.latestVersion,
    );
  }

  /// Android/iOS use platform policy; desktop/web debug falls back to Android.
  static PlatformVersionPolicy selectPlatformPolicy(
    MobileAppConfig config, {
    TargetPlatform? platformOverride,
  }) {
    final platform = platformOverride ?? defaultTargetPlatform;
    if (platform == TargetPlatform.iOS) {
      return config.ios;
    }
    return config.android;
  }
}
