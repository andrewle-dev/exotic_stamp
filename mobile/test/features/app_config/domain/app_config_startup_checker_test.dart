import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/app_config/domain/entities/app_update_decision.dart';
import 'package:metro_stamp_app/features/app_config/domain/entities/maintenance_policy.dart';
import 'package:metro_stamp_app/features/app_config/domain/entities/mobile_app_config.dart';
import 'package:metro_stamp_app/features/app_config/domain/entities/platform_version_policy.dart';
import 'package:metro_stamp_app/features/app_config/domain/repositories/app_config_repository.dart';
import 'package:metro_stamp_app/features/app_config/domain/services/app_config_startup_checker.dart';
import 'package:metro_stamp_app/features/app_config/domain/services/app_version_reader.dart';
import 'package:mocktail/mocktail.dart';

class _MockAppConfigRepository extends Mock implements AppConfigRepository {}

class _FakeVersionReader implements AppVersionReader {
  _FakeVersionReader(this.version);
  final String version;

  @override
  Future<String> readVersion() async => version;
}

void main() {
  late _MockAppConfigRepository repository;

  setUp(() {
    repository = _MockAppConfigRepository();
  });

  test('maps repository config to decision', () async {
    when(() => repository.fetchAppConfig()).thenAnswer(
      (_) async => const MobileAppConfig(
        android: PlatformVersionPolicy(
          minimumSupportedVersion: '1.0.0',
          latestVersion: '2.0.0',
          forceUpdate: false,
        ),
        ios: PlatformVersionPolicy(
          minimumSupportedVersion: '1.0.0',
          latestVersion: '2.0.0',
          forceUpdate: false,
        ),
        maintenance: MaintenancePolicy(enabled: false),
      ),
    );

    final checker = AppConfigStartupChecker(
      repository: repository,
      versionReader: _FakeVersionReader('1.5.0'),
    );

    final decision = await checker.check();
    expect(decision.type, AppUpdateDecisionType.optionalUpdate);
  });

  test('fails open when repository throws', () async {
    when(() => repository.fetchAppConfig()).thenThrow(Exception('offline'));

    final checker = AppConfigStartupChecker(
      repository: repository,
      versionReader: _FakeVersionReader('0.1.0'),
    );

    final decision = await checker.check();
    expect(decision.type, AppUpdateDecisionType.supported);
  });
}
