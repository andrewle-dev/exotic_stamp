import 'package:equatable/equatable.dart';

import 'platform_version_policy.dart';

enum AppUpdateDecisionType {
  supported,
  optionalUpdate,
  forceUpdate,
  maintenance,
}

class AppUpdateDecision extends Equatable {
  const AppUpdateDecision({
    required this.type,
    required this.installedVersion,
    this.policy,
    this.storeUrl,
    this.latestVersion,
    this.maintenanceMessage,
  });

  const AppUpdateDecision.supported({
    this.installedVersion = '',
  })  : type = AppUpdateDecisionType.supported,
        policy = null,
        storeUrl = null,
        latestVersion = null,
        maintenanceMessage = null;

  factory AppUpdateDecision.forceUpdate({
    required String installedVersion,
    required PlatformVersionPolicy policy,
  }) {
    return AppUpdateDecision(
      type: AppUpdateDecisionType.forceUpdate,
      installedVersion: installedVersion,
      policy: policy,
      storeUrl: policy.storeUrl,
      latestVersion: policy.latestVersion,
    );
  }

  factory AppUpdateDecision.optionalUpdate({
    required String installedVersion,
    required PlatformVersionPolicy policy,
  }) {
    return AppUpdateDecision(
      type: AppUpdateDecisionType.optionalUpdate,
      installedVersion: installedVersion,
      policy: policy,
      storeUrl: policy.storeUrl,
      latestVersion: policy.latestVersion,
    );
  }

  factory AppUpdateDecision.maintenance({
    required String installedVersion,
    String? message,
  }) {
    return AppUpdateDecision(
      type: AppUpdateDecisionType.maintenance,
      installedVersion: installedVersion,
      maintenanceMessage: message,
    );
  }

  final AppUpdateDecisionType type;
  final String installedVersion;
  final PlatformVersionPolicy? policy;
  final String? storeUrl;
  final String? latestVersion;
  final String? maintenanceMessage;

  bool get blocksApp =>
      type == AppUpdateDecisionType.forceUpdate ||
      type == AppUpdateDecisionType.maintenance;

  @override
  List<Object?> get props => [
        type,
        installedVersion,
        policy,
        storeUrl,
        latestVersion,
        maintenanceMessage,
      ];
}
