import 'dart:async';

import 'package:equatable/equatable.dart';
import 'package:geolocator/geolocator.dart';

enum LocationIssue {
  permissionDenied,
  serviceDisabled,
  timeout,
  lowAccuracy,
}

class GpsReading extends Equatable {
  const GpsReading({
    required this.latitude,
    required this.longitude,
    required this.accuracyMeters,
  });

  final double latitude;
  final double longitude;
  final double accuracyMeters;

  @override
  List<Object?> get props => [latitude, longitude, accuracyMeters];
}

class LocationReadResult extends Equatable {
  const LocationReadResult.success(this.reading)
      : issue = null,
        message = null;

  const LocationReadResult.failure({
    required this.issue,
    required this.message,
  }) : reading = null;

  final GpsReading? reading;
  final LocationIssue? issue;
  final String? message;

  bool get isSuccess => reading != null;

  @override
  List<Object?> get props => [reading, issue, message];
}

/// Reads device GPS for collect requests.
class AppLocationService {
  AppLocationService({
    this.locationTimeout = const Duration(seconds: 12),
    this.lowAccuracyThresholdMeters = 200,
  });

  final Duration locationTimeout;
  final double lowAccuracyThresholdMeters;

  Future<LocationReadResult> getCurrentReading() async {
    final serviceEnabled = await Geolocator.isLocationServiceEnabled();
    if (!serviceEnabled) {
      return const LocationReadResult.failure(
        issue: LocationIssue.serviceDisabled,
        message: 'Hãy bật Location Services để xác minh vị trí tại ga.',
      );
    }

    var permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
    }

    if (permission == LocationPermission.denied) {
      return const LocationReadResult.failure(
        issue: LocationIssue.permissionDenied,
        message: 'Ứng dụng cần quyền vị trí để thu thập stamp tại ga.',
      );
    }

    if (permission == LocationPermission.deniedForever) {
      return const LocationReadResult.failure(
        issue: LocationIssue.permissionDenied,
        message: 'Quyền vị trí đã bị chặn. Hãy bật lại trong Cài đặt hệ thống.',
      );
    }

    try {
      final position = await Geolocator.getCurrentPosition(
        locationSettings: LocationSettings(
          accuracy: LocationAccuracy.high,
          timeLimit: locationTimeout,
        ),
      );

      final accuracy = position.accuracy;
      if (accuracy > lowAccuracyThresholdMeters) {
        return LocationReadResult.failure(
          issue: LocationIssue.lowAccuracy,
          message:
              'Độ chính xác GPS thấp (${accuracy.round()} m). Hãy ra ngoài trời hoặc thử lại.',
        );
      }

      return LocationReadResult.success(
        GpsReading(
          latitude: position.latitude,
          longitude: position.longitude,
          accuracyMeters: accuracy,
        ),
      );
    } on TimeoutException {
      return const LocationReadResult.failure(
        issue: LocationIssue.timeout,
        message: 'Không lấy được vị trí kịp thời. Vui lòng thử lại.',
      );
    } catch (_) {
      return const LocationReadResult.failure(
        issue: LocationIssue.timeout,
        message: 'Không lấy được vị trí hiện tại. Vui lòng thử lại.',
      );
    }
  }
}
