import 'package:flutter/foundation.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/app_config/domain/entities/app_update_decision.dart';
import 'package:metro_stamp_app/features/app_config/domain/entities/maintenance_policy.dart';
import 'package:metro_stamp_app/features/app_config/domain/entities/mobile_app_config.dart';
import 'package:metro_stamp_app/features/app_config/domain/entities/platform_version_policy.dart';
import 'package:metro_stamp_app/features/app_config/domain/services/app_update_policy_evaluator.dart';

void main() {
  const android = PlatformVersionPolicy(
    minimumSupportedVersion: '1.0.0',
    latestVersion: '1.2.0',
    forceUpdate: false,
    storeUrl: 'https://play.example/app',
  );
  const ios = PlatformVersionPolicy(
    minimumSupportedVersion: '1.0.0',
    latestVersion: '1.2.0',
    forceUpdate: false,
    storeUrl: 'https://apps.example/app',
  );

  MobileAppConfig config({
    PlatformVersionPolicy? androidPolicy,
    PlatformVersionPolicy? iosPolicy,
    MaintenancePolicy maintenance = const MaintenancePolicy(enabled: false),
  }) {
    return MobileAppConfig(
      android: androidPolicy ?? android,
      ios: iosPolicy ?? ios,
      maintenance: maintenance,
    );
  }

  test('installed below minimum => forceUpdate', () {
    final decision = AppUpdatePolicyEvaluator.evaluate(
      installedVersion: '0.9.9',
      config: config(),
      platformOverride: TargetPlatform.android,
    );
    expect(decision.type, AppUpdateDecisionType.forceUpdate);
  });

  test('installed below latest but supported => optionalUpdate', () {
    final decision = AppUpdatePolicyEvaluator.evaluate(
      installedVersion: '1.1.0',
      config: config(),
      platformOverride: TargetPlatform.android,
    );
    expect(decision.type, AppUpdateDecisionType.optionalUpdate);
    expect(decision.storeUrl, 'https://play.example/app');
  });

  test('installed at latest => supported', () {
    final decision = AppUpdatePolicyEvaluator.evaluate(
      installedVersion: '1.2.0',
      config: config(),
      platformOverride: TargetPlatform.android,
    );
    expect(decision.type, AppUpdateDecisionType.supported);
  });

  test('backend forceUpdate true => forceUpdate even when current', () {
    final decision = AppUpdatePolicyEvaluator.evaluate(
      installedVersion: '1.2.0',
      config: config(
        androidPolicy: const PlatformVersionPolicy(
          minimumSupportedVersion: '1.0.0',
          latestVersion: '1.2.0',
          forceUpdate: true,
          storeUrl: 'https://play.example/app',
        ),
      ),
      platformOverride: TargetPlatform.android,
    );
    expect(decision.type, AppUpdateDecisionType.forceUpdate);
  });

  test('maintenance enabled takes priority', () {
    final decision = AppUpdatePolicyEvaluator.evaluate(
      installedVersion: '0.1.0',
      config: config(
        maintenance: const MaintenancePolicy(
          enabled: true,
          message: 'Down for upgrade',
        ),
      ),
      platformOverride: TargetPlatform.android,
    );
    expect(decision.type, AppUpdateDecisionType.maintenance);
    expect(decision.maintenanceMessage, 'Down for upgrade');
  });

  test('iOS uses ios store url', () {
    final decision = AppUpdatePolicyEvaluator.evaluate(
      installedVersion: '1.1.0',
      config: config(),
      platformOverride: TargetPlatform.iOS,
    );
    expect(decision.type, AppUpdateDecisionType.optionalUpdate);
    expect(decision.storeUrl, 'https://apps.example/app');
  });
}
