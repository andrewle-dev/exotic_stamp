import 'package:dio/dio.dart';

import '../../../../core/errors/error_mapper.dart';
import '../../../../core/errors/failure.dart';
import '../../../../core/network/api_client.dart';
import '../../../../core/network/api_response_parser.dart';
import '../../../../core/network/error_interceptor.dart';
import '../models/station_scan_key_models.dart';

class AdminScanKeyRemoteDataSource {
  AdminScanKeyRemoteDataSource({
    required ApiClient apiClient,
    ErrorMapper? errorMapper,
  })  : _apiClient = apiClient,
        _errorMapper = errorMapper ?? const ErrorMapper();

  final ApiClient _apiClient;
  final ErrorMapper _errorMapper;

  Future<StationScanKeyCreatedModel> createScanKey({
    required String stationId,
    required String scanType,
    String? label,
    String? placementNote,
  }) async {
    try {
      final response = await _apiClient.post<dynamic>(
        '/admin/metro/stations/$stationId/scan-keys',
        data: {
          'scanType': scanType,
          if (label != null && label.isNotEmpty) 'label': label,
          if (placementNote != null && placementNote.isNotEmpty)
            'placementNote': placementNote,
        },
      );
      final map = ApiResponseParser.asMap(response.data);
      if (map == null) {
        throw const Failure(
          code: FailureCode.unknown,
          message: 'Invalid create scan key response',
        );
      }
      return StationScanKeyCreatedModel.fromJson(map);
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<void> activateScanKey(String id) async {
    try {
      await _apiClient.patch<dynamic>(
        '/admin/metro/scan-keys/$id/activate',
      );
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<StationScanKeyVerifyModel> verifyInstallation({
    required String id,
    required String payloadReadBack,
    required double latitude,
    required double longitude,
    required double accuracyMeters,
    required String devicePlatform,
    required String appVersion,
  }) async {
    try {
      final response = await _apiClient.post<dynamic>(
        '/admin/metro/scan-keys/$id/verify-installation',
        data: {
          'payloadReadBack': payloadReadBack,
          'latitude': latitude,
          'longitude': longitude,
          'accuracyMeters': accuracyMeters,
          'devicePlatform': devicePlatform,
          'appVersion': appVersion,
        },
      );
      final map = ApiResponseParser.asMap(response.data);
      if (map == null) {
        throw const Failure(
          code: FailureCode.unknown,
          message: 'Invalid verify installation response',
        );
      }
      return StationScanKeyVerifyModel.fromJson(map);
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Failure _toFailure(DioException error) {
    return ErrorInterceptor.failureFrom(error) ??
        _errorMapper.fromDioException(error);
  }
}
