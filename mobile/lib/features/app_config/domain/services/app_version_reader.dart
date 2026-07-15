import 'package:package_info_plus/package_info_plus.dart';

/// Reads the installed app version from the mobile binary.
abstract class AppVersionReader {
  Future<String> readVersion();
}

class PackageInfoAppVersionReader implements AppVersionReader {
  const PackageInfoAppVersionReader();

  @override
  Future<String> readVersion() async {
    final info = await PackageInfo.fromPlatform();
    return info.version;
  }
}
