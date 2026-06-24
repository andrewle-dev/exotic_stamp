import 'package:dio/dio.dart';

import '../../../../core/errors/error_mapper.dart';
import '../../../../core/errors/failure.dart';
import '../../../../core/network/api_client.dart';
import '../../../../core/network/api_response_parser.dart';
import '../../../../core/network/auth_interceptor.dart';
import '../../../../core/network/error_interceptor.dart';
import '../models/profile_model.dart';
import '../models/update_profile_request_model.dart';

class ProfileRemoteDataSource {
  ProfileRemoteDataSource({
    required ApiClient apiClient,
    ErrorMapper? errorMapper,
  })  : _apiClient = apiClient,
        _errorMapper = errorMapper ?? const ErrorMapper();

  final ApiClient _apiClient;
  final ErrorMapper _errorMapper;

  Future<ProfileModel> getMe() async {
    try {
      final response = await _apiClient.get<Map<String, dynamic>>('/users/me');
      final data = response.data;
      if (data == null) {
        throw const Failure(
          code: FailureCode.unknown,
          message: 'Không thể tải thông tin hồ sơ.',
        );
      }
      return ProfileModel.fromUserResponse(data);
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<ProfileModel> updateMe(UpdateProfileRequestModel request) async {
    try {
      final response = await _apiClient.put<Map<String, dynamic>>(
        '/users/me',
        data: request.toJson(),
      );
      final data = response.data;
      if (data == null) {
        throw const Failure(
          code: FailureCode.unknown,
          message: 'Không thể cập nhật hồ sơ.',
        );
      }
      return ProfileModel.fromUserResponse(data);
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<void> logout() async {
    try {
      await _apiClient.post<void>('/auth/logout');
    } on DioException catch (error) {
      final failure = _toFailure(error);
      if (failure.code == FailureCode.unauthorized ||
          failure.code == FailureCode.tokenExpired) {
        return;
      }
      throw failure;
    }
  }

  /// Best-effort memories count from share history (optional).
  Future<int?> getMemoriesCount() async {
    try {
      final response = await _apiClient.get<dynamic>(
        '/community/share-events/me',
        queryParameters: {'page': 0, 'size': 1},
      );
      final map = ApiResponseParser.asMap(response.data);
      final total = map?['totalElements'];
      if (total is int) {
        return total;
      }
      if (total is num) {
        return total.toInt();
      }
      return null;
    } on DioException {
      return null;
    }
  }

  /// Best-effort collected stamp count from collection progress (optional).
  ///
  /// MVP limitation: uses the first active metro line from `GET /metro/lines`,
  /// not an aggregated count across all lines. Returns null on any failure so
  /// the UI shows a neutral unavailable state instead of a fake zero.
  Future<int?> getCollectedStampsCount() async {
    try {
      final linesResponse = await _apiClient.get<dynamic>(
        '/metro/lines',
        options: Options(extra: {AuthInterceptor.skipAuthKey: true}),
      );
      final lines = ApiResponseParser.asMapList(linesResponse.data);
      if (lines.isEmpty) {
        return null;
      }

      final primaryLine = lines.firstWhere(
        (line) => line['status'] == 'ACTIVE',
        orElse: () => lines.first,
      );
      final lineId = primaryLine['id'] as String?;
      if (lineId == null || lineId.isEmpty) {
        return null;
      }

      final progressResponse = await _apiClient.get<dynamic>(
        '/collection/progress',
        queryParameters: {'lineId': lineId},
      );
      final progress = ApiResponseParser.asMap(progressResponse.data);
      final collected = progress?['collected'];
      if (collected is int) {
        return collected;
      }
      if (collected is num) {
        return collected.toInt();
      }
      return null;
    } on DioException {
      return null;
    }
  }

  Failure _toFailure(DioException error) {
    final mapped = ErrorInterceptor.failureFrom(error);
    if (mapped != null) {
      return mapped;
    }
    return _errorMapper.fromDioException(error);
  }
}
