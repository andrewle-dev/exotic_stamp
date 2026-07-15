import 'package:flutter/foundation.dart';

import '../entities/app_update_decision.dart';
import '../repositories/app_config_repository.dart';
import 'app_update_policy_evaluator.dart';
import 'app_version_reader.dart';

/// Fetches backend app-config and evaluates update/maintenance policy.
class AppConfigStartupChecker {
  AppConfigStartupChecker({
    required AppConfigRepository repository,
    required AppVersionReader versionReader,
  })  : _repository = repository,
        _versionReader = versionReader;

  final AppConfigRepository _repository;
  final AppVersionReader _versionReader;

  /// Fail-open: any error returns [AppUpdateDecision.supported].
  Future<AppUpdateDecision> check() async {
    try {
      final installedVersion = await _versionReader.readVersion();
      final config = await _repository.fetchAppConfig();
      return AppUpdatePolicyEvaluator.evaluate(
        installedVersion: installedVersion,
        config: config,
      );
    } catch (error, stackTrace) {
      if (kDebugMode) {
        debugPrint('[app-config] startup check failed (continuing): $error');
        debugPrint('$stackTrace');
      }
      return const AppUpdateDecision.supported();
    }
  }
}
