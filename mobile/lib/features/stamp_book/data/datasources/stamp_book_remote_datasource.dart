import 'package:dio/dio.dart';

import '../../../../core/errors/error_mapper.dart';
import '../../../../core/errors/failure.dart';
import '../../../../core/network/api_client.dart';
import '../../../../core/network/api_response_parser.dart';
import '../../../../core/network/error_interceptor.dart';
import '../models/stamp_book_model.dart';
import '../models/stamp_item_model.dart';

class StampBookRemoteDataSource {
  StampBookRemoteDataSource({
    required ApiClient apiClient,
    ErrorMapper? errorMapper,
  })  : _apiClient = apiClient,
        _errorMapper = errorMapper ?? const ErrorMapper();

  final ApiClient _apiClient;
  final ErrorMapper _errorMapper;

  Future<StampBookModel> getStampBook({String? lineId}) async {
    try {
      final response = await _apiClient.get<dynamic>(
        '/collection/stamp-book',
        queryParameters: lineId == null ? null : {'lineId': lineId},
      );
      final map = ApiResponseParser.asMap(response.data);
      if (map == null) {
        throw const Failure(
          code: FailureCode.unknown,
          message: 'Không thể tải Sổ stamp.',
        );
      }
      return StampBookModel.fromJson(map);
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<List<StampItemModel>> getMyStamps({
    String? lineId,
    int page = 0,
    int size = 100,
  }) async {
    try {
      final response = await _apiClient.get<dynamic>(
        '/collection/my-stamps',
        queryParameters: {
          if (lineId != null) 'lineId': lineId,
          'page': page,
          'size': size,
        },
      );
      return ApiResponseParser.paginatedContent(response.data)
          .map(StampItemModel.fromUserStampJson)
          .toList();
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
