import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/app_config/data/models/mobile_app_config_model.dart';

void main() {
  test('parses app-config JSON envelope payload', () {
    final model = MobileAppConfigModel.fromJson({
      'android': {
        'minimumSupportedVersion': '0.1.0',
        'latestVersion': '0.2.0',
        'forceUpdate': false,
        'storeUrl': 'https://play.example',
      },
      'ios': {
        'minimumSupportedVersion': '0.1.0',
        'latestVersion': '0.2.0',
        'forceUpdate': true,
        'storeUrl': '',
      },
      'maintenance': {
        'enabled': false,
        'message': null,
      },
    });

    final entity = model.toEntity();
    expect(entity.android.latestVersion, '0.2.0');
    expect(entity.android.storeUrl, 'https://play.example');
    expect(entity.ios.forceUpdate, isTrue);
    expect(entity.ios.storeUrl, isNull);
    expect(entity.maintenance.enabled, isFalse);
    expect(entity.maintenance.message, isNull);
  });
}
