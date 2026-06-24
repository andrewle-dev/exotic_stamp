import 'package:dio/dio.dart';

import '../../../../core/errors/error_mapper.dart';
import '../../../../core/errors/failure.dart';
import '../../../../core/network/api_client.dart';
import '../../../../core/network/api_response_parser.dart';
import '../../../../core/network/auth_interceptor.dart';
import '../../../../core/network/error_interceptor.dart';
import '../models/milestone_model.dart';
import '../models/user_reward_model.dart';
import '../models/voucher_detail_model.dart';

class RewardsRemoteDataSource {
  RewardsRemoteDataSource({
    required ApiClient apiClient,
    ErrorMapper? errorMapper,
  })  : _apiClient = apiClient,
        _errorMapper = errorMapper ?? const ErrorMapper();

  final ApiClient _apiClient;
  final ErrorMapper _errorMapper;

  Future<List<UserRewardModel>> getMyRewards({
    String? status,
    int page = 0,
    int size = 50,
  }) async {
    try {
      final response = await _apiClient.get<dynamic>(
        '/rewards/my',
        queryParameters: {
          if (status != null) 'status': status,
          'page': page,
          'size': size,
        },
      );
      return ApiResponseParser.paginatedContent(response.data)
          .map(UserRewardModel.fromJson)
          .toList();
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<VoucherDetailModel> getVoucherDetail({required String id}) async {
    try {
      final response = await _apiClient.get<dynamic>('/rewards/my/$id');
      final map = ApiResponseParser.asMap(response.data);
      if (map == null) {
        throw const Failure(
          code: FailureCode.unknown,
          message: 'Không thể tải chi tiết voucher.',
        );
      }
      return VoucherDetailModel.fromJson(map);
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<List<MilestoneModel>> getMilestones({
    required String campaignId,
    int page = 0,
    int size = 50,
  }) async {
    try {
      final response = await _apiClient.get<dynamic>(
        '/rewards/milestones',
        queryParameters: {
          'campaignId': campaignId,
          'page': page,
          'size': size,
        },
      );
      final milestones = ApiResponseParser.paginatedContent(response.data)
          .map(MilestoneModel.fromJson)
          .toList()
        ..sort((a, b) => a.requiredStampCount.compareTo(b.requiredStampCount));
      return milestones;
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<Map<String, dynamic>?> getActiveCampaign() async {
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
      return first;
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<Map<String, dynamic>> getCollectionProgress({
    required String lineId,
  }) async {
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
      return map;
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<List<Map<String, dynamic>>> getLines() async {
    try {
      final response = await _apiClient.get<dynamic>(
        '/metro/lines',
        options: Options(extra: {AuthInterceptor.skipAuthKey: true}),
      );
      return ApiResponseParser.asMapList(response.data);
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
