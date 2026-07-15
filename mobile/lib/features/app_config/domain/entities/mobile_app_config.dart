import 'package:equatable/equatable.dart';

import 'maintenance_policy.dart';
import 'platform_version_policy.dart';

class MobileAppConfig extends Equatable {
  const MobileAppConfig({
    required this.android,
    required this.ios,
    required this.maintenance,
  });

  final PlatformVersionPolicy android;
  final PlatformVersionPolicy ios;
  final MaintenancePolicy maintenance;

  @override
  List<Object?> get props => [android, ios, maintenance];
}
