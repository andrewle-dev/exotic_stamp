import 'package:dio/dio.dart';

import '../config/api_config.dart';
import '../storage/secure_token_storage.dart';
import 'auth_interceptor.dart';

/// Single-flight refresh using native body refresh contract (no cookies).
class NativeRefreshInterceptor extends QueuedInterceptor {
  NativeRefreshInterceptor({
    required Dio dio,
    required SecureTokenStorage tokenStorage,
    Future<void> Function()? onSessionInvalidated,
  })  : _dio = dio,
        _tokenStorage = tokenStorage,
        _onSessionInvalidated = onSessionInvalidated;

  final Dio _dio;
  final SecureTokenStorage _tokenStorage;
  final Future<void> Function()? _onSessionInvalidated;

  static const _retriedExtraKey = 'retried_after_refresh';

  Future<void>? _refreshFuture;
  bool _clearing = false;

  @override
  Future<void> onError(
    DioException err,
    ErrorInterceptorHandler handler,
  ) async {
    if (!_shouldAttemptRefresh(err)) {
      return handler.next(err);
    }

    try {
      await _refreshAccessTokenOnce();
      final response = await _retryRequest(err.requestOptions);
      return handler.resolve(response);
    } catch (_) {
      await _invalidateOnce();
      return handler.next(err);
    }
  }

  bool _shouldAttemptRefresh(DioException err) {
    if (err.response?.statusCode != 401) {
      return false;
    }

    final path = err.requestOptions.uri.path;
    if (path.endsWith(ApiConfig.refreshPath) ||
        path.endsWith('/auth/login') ||
        path.endsWith('/auth/register')) {
      return false;
    }

    if (err.requestOptions.extra[_retriedExtraKey] == true) {
      return false;
    }

    return true;
  }

  Future<void> _refreshAccessTokenOnce() {
    _refreshFuture ??=
        _performRefresh().whenComplete(() => _refreshFuture = null);
    return _refreshFuture!;
  }

  Future<void> _performRefresh() async {
    final refreshToken = await _tokenStorage.readRefreshToken();
    if (refreshToken == null || refreshToken.isEmpty) {
      throw DioException(
        requestOptions: RequestOptions(path: ApiConfig.refreshPath),
        message: 'Missing refresh token',
      );
    }

    final response = await _dio.post<Map<String, dynamic>>(
      ApiConfig.refreshPath,
      data: {'refreshToken': refreshToken},
      options: Options(
        headers: const {'X-Client-Transport': 'body'},
        extra: {AuthInterceptor.skipAuthKey: true},
      ),
    );

    final data = response.data;
    final access = data?['accessToken'] as String?;
    final nextRefresh = data?['refreshToken'] as String?;
    if (access == null || access.isEmpty || nextRefresh == null || nextRefresh.isEmpty) {
      throw DioException(
        requestOptions: response.requestOptions,
        message: 'Refresh response missing tokens',
      );
    }

    try {
      await _tokenStorage.writeRefreshToken(nextRefresh);
      await _tokenStorage.writeAccessToken(access);
    } catch (_) {
      await _tokenStorage.clearSessionTokens();
      throw DioException(
        requestOptions: response.requestOptions,
        message: 'Failed to persist rotated refresh token',
        type: DioExceptionType.unknown,
      );
    }
  }

  Future<Response<dynamic>> _retryRequest(RequestOptions requestOptions) {
    final headers = Map<String, dynamic>.from(requestOptions.headers);
    final token = _tokenStorage.readAccessToken();
    if (token != null && token.isNotEmpty) {
      headers['Authorization'] = 'Bearer $token';
    }
    return _dio.request<dynamic>(
      requestOptions.path,
      data: requestOptions.data,
      queryParameters: requestOptions.queryParameters,
      options: Options(
        method: requestOptions.method,
        headers: headers,
        extra: {
          ...requestOptions.extra,
          _retriedExtraKey: true,
        },
      ),
    );
  }

  Future<void> _invalidateOnce() async {
    if (_clearing) {
      return;
    }
    _clearing = true;
    try {
      await _tokenStorage.clearSessionTokens();
      await _onSessionInvalidated?.call();
    } finally {
      _clearing = false;
    }
  }
}
