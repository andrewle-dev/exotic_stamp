import 'dart:io';

import 'package:dio/dio.dart';

import '../../../../core/constants/app_constants.dart';
import '../../../../core/errors/error_mapper.dart';
import '../../../../core/errors/failure.dart';
import '../../../../core/network/api_client.dart';
import '../../../../core/network/api_response_parser.dart';
import '../../../../core/network/auth_interceptor.dart';
import '../../../../core/network/error_interceptor.dart';
import '../../domain/entities/scan_payload.dart';
import '../models/collect_stamp_request_model.dart';
import '../models/collect_stamp_response_model.dart';
import '../models/collect_status_response_model.dart';
import '../models/scan_resolve_request_model.dart';
import '../models/scan_resolve_response_model.dart';

class ScanRemoteDataSource {
  ScanRemoteDataSource({
    required ApiClient apiClient,
    ErrorMapper? errorMapper,
  })  : _apiClient = apiClient,
        _errorMapper = errorMapper ?? const ErrorMapper();

  final ApiClient _apiClient;
  final ErrorMapper _errorMapper;

  Future<ScanResolveResponseModel> resolveScan(ScanPayload payload) async {
    try {
      final request = ScanResolveRequestModel(
        scanType: payload.scanType,
        payload: payload.payload,
        devicePlatform: _devicePlatform,
        appVersion: AppConstants.appVersion,
      );

      final response = await _apiClient.post<dynamic>(
        '/metro/scan/resolve',
        data: request.toJson(),
        options: Options(extra: {AuthInterceptor.skipAuthKey: true}),
      );

      final map = ApiResponseParser.asMap(response.data);
      if (map == null) {
        throw const Failure(
          code: FailureCode.unknown,
          message: 'Không thể xác minh mã quét.',
        );
      }

      return ScanResolveResponseModel.fromJson(map);
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<CollectStampResponseModel> collectStamp({
    required ScanPayload payload,
    required double latitude,
    required double longitude,
    required double accuracyMeters,
    required String idempotencyKey,
  }) async {
    try {
      final request = CollectStampRequestModel(
        scanType: payload.scanType,
        payload: payload.payload,
        latitude: latitude,
        longitude: longitude,
        accuracyMeters: accuracyMeters,
        devicePlatform: _devicePlatform,
        appVersion: AppConstants.appVersion,
        idempotencyKey: idempotencyKey,
      );

      final response = await _apiClient.post<dynamic>(
        '/collection/collect',
        data: request.toJson(),
      );

      final map = ApiResponseParser.asMap(response.data);
      if (map == null) {
        throw const Failure(
          code: FailureCode.unknown,
          message: 'Không thể thu thập stamp.',
        );
      }

      return CollectStampResponseModel.fromJson(map);
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<CollectStatusResponseModel> getCollectStatus({
    required String idempotencyKey,
  }) async {
    try {
      final response = await _apiClient.get<dynamic>(
        '/collection/collect/status',
        queryParameters: {'idempotencyKey': idempotencyKey},
      );

      final map = ApiResponseParser.asMap(response.data);
      if (map == null) {
        throw const Failure(
          code: FailureCode.unknown,
          message: 'Không thể kiểm tra trạng thái thu thập.',
        );
      }

      return CollectStatusResponseModel.fromJson(map);
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  String get _devicePlatform {
    if (Platform.isIOS) {
      return 'ios';
    }
    if (Platform.isAndroid) {
      return 'android';
    }
    return 'unknown';
  }

  Failure _toFailure(DioException error) {
    final mapped = ErrorInterceptor.failureFrom(error);
    if (mapped != null) {
      return mapped;
    }
    return _errorMapper.fromDioException(error);
  }
}
