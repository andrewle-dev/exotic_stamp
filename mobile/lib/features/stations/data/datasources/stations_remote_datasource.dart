import 'package:dio/dio.dart';

import '../../../../core/errors/error_mapper.dart';
import '../../../../core/errors/failure.dart';
import '../../../../core/network/api_client.dart';
import '../../../../core/network/api_response_parser.dart';
import '../../../../core/network/auth_interceptor.dart';
import '../../../../core/network/error_interceptor.dart';
import '../models/line_model.dart';
import '../models/station_detail_model.dart';
import '../models/station_model.dart';

class StationsRemoteDataSource {
  StationsRemoteDataSource({
    required ApiClient apiClient,
    ErrorMapper? errorMapper,
  })  : _apiClient = apiClient,
        _errorMapper = errorMapper ?? const ErrorMapper();

  final ApiClient _apiClient;
  final ErrorMapper _errorMapper;

  Future<List<LineModel>> getLines() async {
    try {
      final response = await _apiClient.get<dynamic>(
        '/metro/lines',
        options: Options(extra: {AuthInterceptor.skipAuthKey: true}),
      );
      return ApiResponseParser.asMapList(response.data)
          .map(LineModel.fromJson)
          .toList();
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<List<StationModel>> getStations({String? lineId}) async {
    try {
      final response = await _apiClient.get<dynamic>(
        '/metro/stations',
        queryParameters: lineId == null ? null : {'lineId': lineId},
        options: Options(extra: {AuthInterceptor.skipAuthKey: true}),
      );
      return ApiResponseParser.asMapList(response.data)
          .map(StationModel.fromJson)
          .toList();
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<StationDetailModel> getStationDetail(String stationId) async {
    try {
      final response = await _apiClient.get<dynamic>(
        '/metro/stations/$stationId',
        options: Options(extra: {AuthInterceptor.skipAuthKey: true}),
      );
      final map = ApiResponseParser.asMap(response.data);
      if (map == null) {
        throw const Failure(
          code: FailureCode.unknown,
          message: 'Không tìm thấy thông tin ga.',
        );
      }
      return StationDetailModel.fromJson(map);
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<Map<String, bool>> getCollectedStationIds({String? lineId}) async {
    try {
      final response = await _apiClient.get<dynamic>(
        '/collection/stamp-book',
        queryParameters: lineId == null ? null : {'lineId': lineId},
      );
      final map = ApiResponseParser.asMap(response.data);
      final stations = map?['stations'];
      if (stations is! List) {
        return const {};
      }

      final collected = <String, bool>{};
      for (final item in stations) {
        if (item is Map<String, dynamic>) {
          final cell = StampBookStationCellModel.fromJson(item);
          if (cell.stationId.isNotEmpty) {
            collected[cell.stationId] = cell.collected;
          }
        }
      }
      return collected;
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
