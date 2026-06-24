import 'package:dio/dio.dart';

import '../../../../core/errors/error_mapper.dart';
import '../../../../core/errors/failure.dart';
import '../../../../core/network/api_client.dart';
import '../../../../core/network/api_response_parser.dart';
import '../../../../core/network/error_interceptor.dart';
import '../models/record_share_event_request_model.dart';
import '../models/share_event_model.dart';

class MemoriesRemoteDataSource {
  MemoriesRemoteDataSource({
    required ApiClient apiClient,
    ErrorMapper? errorMapper,
  })  : _apiClient = apiClient,
        _errorMapper = errorMapper ?? const ErrorMapper();

  final ApiClient _apiClient;
  final ErrorMapper _errorMapper;

  Future<ShareEventModel> recordShareEvent(
    RecordShareEventRequestModel request,
  ) async {
    try {
      final response = await _apiClient.post<dynamic>(
        '/community/share-events',
        data: request.toJson(),
      );
      final map = ApiResponseParser.asMap(response.data);
      if (map == null) {
        throw const Failure(
          code: FailureCode.unknown,
          message: 'Không thể ghi nhận sự kiện chia sẻ.',
        );
      }
      return ShareEventModel.fromJson(map);
    } on DioException catch (error) {
      throw _toFailure(error);
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
