import 'package:cookie_jar/cookie_jar.dart';
import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';

import '../config/api_config.dart';
import '../storage/secure_token_storage.dart';
import 'auth_interceptor.dart';

/// Handles a single refresh attempt on 401 using the HttpOnly refresh cookie.
class CookieRefreshInterceptor extends QueuedInterceptor {
  CookieRefreshInterceptor({
    required Dio dio,
    required SecureTokenStorage tokenStorage,
    required CookieJar cookieJar,
    Future<void> Function()? onSessionInvalidated,
  })  : _dio = dio,
        _tokenStorage = tokenStorage,
        _cookieJar = cookieJar,
        _onSessionInvalidated = onSessionInvalidated;

  final Dio _dio;
  final SecureTokenStorage _tokenStorage;
  final CookieJar _cookieJar;
  final Future<void> Function()? _onSessionInvalidated;

  static const _retriedExtraKey = 'retried_after_refresh';

  Future<void>? _refreshFuture;

  @override
  Future<void> onError(
    DioException err,
    ErrorInterceptorHandler handler,
  ) async {
    if (err.response?.statusCode == 401) {
      // TODO(debug-bug2): remove
      debugPrint(
        '[refresh] 401 on ${err.requestOptions.path} '
        'attemptRefresh=${_shouldAttemptRefresh(err)}',
      );
    }

    if (!_shouldAttemptRefresh(err)) {
      return handler.next(err);
    }

    // TODO(debug-bug2): remove
    debugPrint('[refresh] starting refresh for ${err.requestOptions.path}');
    try {
      await _refreshAccessTokenOnce();
      final response = await _retryRequest(err.requestOptions);
      // TODO(debug-bug2): remove
      debugPrint('[refresh] success, retry resolved ${err.requestOptions.path}');
      return handler.resolve(response);
    } catch (error) {
      // TODO(debug-bug2): remove
      debugPrint('[refresh] failed: $error');
      await _tokenStorage.clear();
      await _cookieJar.deleteAll();
      await _onSessionInvalidated?.call();
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
    final response = await _dio.post<Map<String, dynamic>>(
      ApiConfig.refreshPath,
      options: Options(
        extra: {AuthInterceptor.skipAuthKey: true},
      ),
    );

    final data = response.data;
    final token = data?['accessToken'] as String?;
    if (token == null || token.isEmpty) {
      throw DioException(
        requestOptions: response.requestOptions,
        message: 'Refresh response missing accessToken',
      );
    }

    await _tokenStorage.writeAccessToken(token);
    // TODO(debug-bug2): remove
    debugPrint('[refresh] new access token length=${token.length}');
  }

  Future<Response<dynamic>> _retryRequest(RequestOptions requestOptions) {
    final headers = Map<String, dynamic>.from(requestOptions.headers);
    return _dio.request<dynamic>(
      requestOptions.path,
      data: requestOptions.data,
      queryParameters: requestOptions.queryParameters,
      options: Options(
        method: requestOptions.method,
        headers: headers,
        contentType: requestOptions.contentType,
        responseType: requestOptions.responseType,
        followRedirects: requestOptions.followRedirects,
        validateStatus: requestOptions.validateStatus,
        receiveTimeout: requestOptions.receiveTimeout,
        sendTimeout: requestOptions.sendTimeout,
        extra: {
          ...requestOptions.extra,
          _retriedExtraKey: true,
        },
      ),
    );
  }
}
