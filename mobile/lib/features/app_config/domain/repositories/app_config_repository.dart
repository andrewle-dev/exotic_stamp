import '../entities/mobile_app_config.dart';

abstract class AppConfigRepository {
  Future<MobileAppConfig> fetchAppConfig();
}
