import '../../domain/entities/maintenance_policy.dart';
import '../../domain/entities/mobile_app_config.dart';
import '../../domain/entities/platform_version_policy.dart';

class MobileAppConfigModel {
  const MobileAppConfigModel({
    required this.android,
    required this.ios,
    required this.maintenance,
  });

  final PlatformVersionPolicyModel android;
  final PlatformVersionPolicyModel ios;
  final MaintenancePolicyModel maintenance;

  factory MobileAppConfigModel.fromJson(Map<String, dynamic> json) {
    return MobileAppConfigModel(
      android: PlatformVersionPolicyModel.fromJson(
        _asMap(json['android']) ?? const {},
      ),
      ios: PlatformVersionPolicyModel.fromJson(
        _asMap(json['ios']) ?? const {},
      ),
      maintenance: MaintenancePolicyModel.fromJson(
        _asMap(json['maintenance']) ?? const {},
      ),
    );
  }

  MobileAppConfig toEntity() {
    return MobileAppConfig(
      android: android.toEntity(),
      ios: ios.toEntity(),
      maintenance: maintenance.toEntity(),
    );
  }

  static Map<String, dynamic>? _asMap(Object? value) {
    if (value is Map<String, dynamic>) {
      return value;
    }
    if (value is Map) {
      return value.map((key, dynamic v) => MapEntry(key.toString(), v));
    }
    return null;
  }
}

class PlatformVersionPolicyModel {
  const PlatformVersionPolicyModel({
    required this.minimumSupportedVersion,
    required this.latestVersion,
    required this.forceUpdate,
    this.storeUrl,
  });

  final String minimumSupportedVersion;
  final String latestVersion;
  final bool forceUpdate;
  final String? storeUrl;

  factory PlatformVersionPolicyModel.fromJson(Map<String, dynamic> json) {
    return PlatformVersionPolicyModel(
      minimumSupportedVersion:
          (json['minimumSupportedVersion'] as String?)?.trim().isNotEmpty ==
                  true
              ? (json['minimumSupportedVersion'] as String).trim()
              : '0.0.0',
      latestVersion:
          (json['latestVersion'] as String?)?.trim().isNotEmpty == true
              ? (json['latestVersion'] as String).trim()
              : '0.0.0',
      forceUpdate: json['forceUpdate'] == true,
      storeUrl: _nullableString(json['storeUrl']),
    );
  }

  PlatformVersionPolicy toEntity() {
    return PlatformVersionPolicy(
      minimumSupportedVersion: minimumSupportedVersion,
      latestVersion: latestVersion,
      forceUpdate: forceUpdate,
      storeUrl: storeUrl,
    );
  }

  static String? _nullableString(Object? value) {
    if (value is! String) {
      return null;
    }
    final trimmed = value.trim();
    return trimmed.isEmpty ? null : trimmed;
  }
}

class MaintenancePolicyModel {
  const MaintenancePolicyModel({
    required this.enabled,
    this.message,
  });

  final bool enabled;
  final String? message;

  factory MaintenancePolicyModel.fromJson(Map<String, dynamic> json) {
    return MaintenancePolicyModel(
      enabled: json['enabled'] == true,
      message: _nullableString(json['message']),
    );
  }

  MaintenancePolicy toEntity() {
    return MaintenancePolicy(enabled: enabled, message: message);
  }

  static String? _nullableString(Object? value) {
    if (value is! String) {
      return null;
    }
    final trimmed = value.trim();
    return trimmed.isEmpty ? null : trimmed;
  }
}
