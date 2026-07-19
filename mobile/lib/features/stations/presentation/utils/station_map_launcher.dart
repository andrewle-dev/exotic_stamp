import 'package:url_launcher/url_launcher.dart';

/// Opens an external maps app for a station coordinate (MVP — no embedded SDK).
abstract final class StationMapLauncher {
  static Future<bool> openCoordinates({
    required double latitude,
    required double longitude,
    String? label,
  }) async {
    final queryValue = (label == null || label.trim().isEmpty)
        ? '$latitude,$longitude'
        : '$latitude,$longitude (${label.trim()})';
    final webUri = Uri.https(
      'www.google.com',
      '/maps/search/',
      <String, String>{'api': '1', 'query': queryValue},
    );
    if (await canLaunchUrl(webUri)) {
      return launchUrl(webUri, mode: LaunchMode.externalApplication);
    }

    final geoUri = Uri.parse('geo:$latitude,$longitude?q=$latitude,$longitude');
    if (await canLaunchUrl(geoUri)) {
      return launchUrl(geoUri, mode: LaunchMode.externalApplication);
    }
    return false;
  }

  static Future<bool> openDirections({
    required double latitude,
    required double longitude,
  }) async {
    final uri = Uri.parse(
      'https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude',
    );
    if (await canLaunchUrl(uri)) {
      return launchUrl(uri, mode: LaunchMode.externalApplication);
    }
    return false;
  }
}
