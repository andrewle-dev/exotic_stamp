import 'package:dio/dio.dart';

import '../../../../core/errors/error_mapper.dart';
import '../../../../core/errors/failure.dart';
import '../../../../core/network/api_client.dart';
import '../../../../core/network/api_response_parser.dart';
import '../../../../core/network/auth_interceptor.dart';
import '../../../../core/network/error_interceptor.dart';
import '../models/home_summary_model.dart';

class HomeRemoteDataSource {
  HomeRemoteDataSource({
    required ApiClient apiClient,
    ErrorMapper? errorMapper,
  })  : _apiClient = apiClient,
        _errorMapper = errorMapper ?? const ErrorMapper();

  final ApiClient _apiClient;
  final ErrorMapper _errorMapper;

  Future<UserProfileModel> getMe() async {
    try {
      final response = await _apiClient.get<Map<String, dynamic>>('/users/me');
      final data = response.data;
      if (data == null) {
        throw const Failure(
          code: FailureCode.unknown,
          message: 'Không thể tải thông tin người dùng.',
        );
      }
      return UserProfileModel.fromJson(data);
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<List<MetroLineModel>> getLines() async {
    try {
      final response = await _apiClient.get<dynamic>(
        '/metro/lines',
        options: Options(extra: {AuthInterceptor.skipAuthKey: true}),
      );
      return ApiResponseParser.asMapList(response.data)
          .map(MetroLineModel.fromJson)
          .toList();
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<CollectionProgressModel> getProgress({required String lineId}) async {
    try {
      final response = await _apiClient.get<dynamic>(
        '/collection/progress',
        queryParameters: {'lineId': lineId},
      );
      final map = ApiResponseParser.asMap(response.data);
      if (map == null) {
        throw const Failure(
          code: FailureCode.unknown,
          message: 'Không thể tải tiến độ sưu tập.',
        );
      }
      try {
        return CollectionProgressModel.fromJson(map);
      } on FormatException catch (error) {
        assert(() {
          // ignore: avoid_print
          print('Home progress parse error: ${error.message}');
          return true;
        }());
        throw const Failure(
          code: FailureCode.unknown,
          message: 'Dữ liệu tiến độ sưu tập không hợp lệ.',
        );
      }
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<List<RecentStampModel>> getRecentStamps({
    required String lineId,
    int size = 5,
  }) async {
    try {
      final response = await _apiClient.get<dynamic>(
        '/collection/my-stamps',
        queryParameters: {
          'lineId': lineId,
          'page': 0,
          'size': size,
        },
      );
      return ApiResponseParser.paginatedContent(response.data)
          .map(RecentStampModel.fromJson)
          .toList();
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<ActiveBannerModel?> getActiveCampaign() async {
    try {
      final response = await _apiClient.get<dynamic>(
        '/campaigns/active',
        options: Options(extra: {AuthInterceptor.skipAuthKey: true}),
      );
      final map = ApiResponseParser.asMap(response.data);
      final campaigns = map?['campaigns'];
      if (campaigns is! List || campaigns.isEmpty) {
        return null;
      }
      final first = campaigns.first;
      if (first is! Map<String, dynamic>) {
        return null;
      }
      return ActiveBannerModel.fromCampaignJson(first);
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<List<PartnerBannerModel>> getPromotionalBanners() async {
    try {
      final response = await _apiClient.get<dynamic>(
        '/partners/promotional-banners',
        options: Options(extra: {AuthInterceptor.skipAuthKey: true}),
      );
      return ApiResponseParser.asMapList(response.data)
          .map(PartnerBannerModel.fromJson)
          .where((banner) => banner.bannerImageUrl.trim().isNotEmpty)
          .toList();
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<List<MilestoneModel>> getMilestones({
    required String campaignId,
  }) async {
    try {
      final response = await _apiClient.get<dynamic>(
        '/rewards/milestones',
        queryParameters: {
          'campaignId': campaignId,
          'page': 0,
          'size': 20,
        },
      );
      return ApiResponseParser.paginatedContent(response.data)
          .map(MilestoneModel.fromJson)
          .toList()
        ..sort((a, b) => a.requiredStampCount.compareTo(b.requiredStampCount));
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
