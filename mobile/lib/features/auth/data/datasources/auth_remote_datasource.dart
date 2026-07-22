import 'package:dio/dio.dart';

import '../../../../core/errors/error_mapper.dart';
import '../../../../core/errors/failure.dart';
import '../../../../core/network/api_client.dart';
import '../../../../core/network/auth_interceptor.dart';
import '../../../../core/network/error_interceptor.dart';
import '../../../../core/storage/secure_token_storage.dart';
import '../models/auth_response_model.dart';
import '../models/change_password_request.dart';
import '../models/forgot_password_request.dart';
import '../models/login_request.dart';
import '../models/register_request.dart';
import '../models/resend_verification_otp_request.dart';
import '../models/verify_account_request.dart';
import '../models/user_model.dart';

class AuthRemoteDataSource {
  AuthRemoteDataSource({
    required ApiClient apiClient,
    required SecureTokenStorage tokenStorage,
    ErrorMapper? errorMapper,
  })  : _apiClient = apiClient,
        _tokenStorage = tokenStorage,
        _errorMapper = errorMapper ?? const ErrorMapper();

  final ApiClient _apiClient;
  final SecureTokenStorage _tokenStorage;
  final ErrorMapper _errorMapper;

  Future<AuthResponseModel> login({
    required String identifier,
    required String password,
  }) async {
    final deviceId = await _tokenStorage.getOrCreateDeviceId();
    final request = LoginRequest(
      identifier: identifier,
      password: password,
      deviceFingerprint: deviceId,
    );

    return _postAuthResponse(
      '/auth/login',
      data: request.toJson(),
      skipAuth: true,
    );
  }

  Future<String> register(RegisterRequest request) async {
    try {
      final response = await _apiClient.post<dynamic>(
        '/auth/register',
        data: request.toJson(),
        options: Options(extra: {AuthInterceptor.skipAuthKey: true}),
      );

      final data = response.data;
      if (data is String && data.isNotEmpty) {
        return data;
      }
      return 'Registered successfully! Please check your email for your verification code.';
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<void> forgotPassword({required String email}) async {
    try {
      await _apiClient.post<void>(
        '/auth/forgot-password',
        data: ForgotPasswordRequest(email: email).toJson(),
        options: Options(extra: {AuthInterceptor.skipAuthKey: true}),
      );
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<void> verifyAccount({
    required String email,
    required String otp,
  }) async {
    try {
      await _apiClient.post<Map<String, dynamic>>(
        '/auth/verify-account',
        data: VerifyAccountRequest(email: email, otp: otp).toJson(),
        options: Options(extra: {AuthInterceptor.skipAuthKey: true}),
      );
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<void> resendVerificationOtp({required String email}) async {
    try {
      await _apiClient.post<Map<String, dynamic>>(
        '/auth/resend-verification-otp',
        data: ResendVerificationOtpRequest(email: email).toJson(),
        options: Options(extra: {AuthInterceptor.skipAuthKey: true}),
      );
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<AuthResponseModel> refresh() async {
    final refreshToken = await _tokenStorage.readRefreshToken();
    if (refreshToken == null || refreshToken.isEmpty) {
      throw const Failure(
        code: FailureCode.unauthorized,
        message: 'Bạn cần đăng nhập để tiếp tục.',
      );
    }
    return _postAuthResponse(
      '/auth/refresh',
      data: {'refreshToken': refreshToken},
      skipAuth: true,
    );
  }

  Future<void> logout() async {
    try {
      final refreshToken = await _tokenStorage.readRefreshToken();
      await _apiClient.post<void>(
        '/auth/logout',
        data: refreshToken == null ? null : {'refreshToken': refreshToken},
      );
    } on DioException catch (error) {
      final failure = _toFailure(error);
      if (failure.code == FailureCode.unauthorized ||
          failure.code == FailureCode.tokenExpired) {
        return;
      }
      throw failure;
    }
  }

  Future<void> changePassword(ChangePasswordRequest request) async {
    try {
      await _apiClient.post<Map<String, dynamic>>(
        '/auth/change-password',
        data: request.toJson(),
      );
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<UserModel> getMe() async {
    try {
      final response = await _apiClient.get<Map<String, dynamic>>('/users/me');
      final data = response.data;
      if (data == null) {
        throw const Failure(
          code: FailureCode.unknown,
          message: 'Không thể tải thông tin người dùng.',
        );
      }
      return UserModel.fromUserResponse(data);
    } on DioException catch (error) {
      throw _toFailure(error);
    }
  }

  Future<AuthResponseModel> _postAuthResponse(
    String path, {
    Map<String, dynamic>? data,
    bool skipAuth = false,
  }) async {
    try {
      final response = await _apiClient.post<Map<String, dynamic>>(
        path,
        data: data,
        options: Options(
          headers: const {'X-Client-Transport': 'body'},
          extra: skipAuth ? {AuthInterceptor.skipAuthKey: true} : null,
        ),
      );

      final body = response.data;
      if (body == null) {
        throw const Failure(
          code: FailureCode.unknown,
          message: 'Phản hồi không hợp lệ. Vui lòng thử lại.',
        );
      }

      return AuthResponseModel.fromJson(body);
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
