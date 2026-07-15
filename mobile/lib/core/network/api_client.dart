import 'package:cookie_jar/cookie_jar.dart';
import 'package:dio/dio.dart';
import 'package:dio_cookie_manager/dio_cookie_manager.dart';
import 'package:path_provider/path_provider.dart';

import '../config/api_config.dart';
import '../storage/secure_token_storage.dart';
import 'auth_interceptor.dart';
import 'cookie_refresh_interceptor.dart';
import 'error_interceptor.dart';
import 'retry_policy.dart';

/// Dio-based HTTP client with auth, cookies, refresh, and retry support.
class ApiClient {
  ApiClient._({
    required Dio dio,
    required CookieJar cookieJar,
    required SecureTokenStorage tokenStorage,
    RetryPolicy? retryPolicy,
  })  : _dio = dio,
        _cookieJar = cookieJar,
        _tokenStorage = tokenStorage,
        _retryPolicy = retryPolicy ?? const RetryPolicy();

  final Dio _dio;
  final CookieJar _cookieJar;
  final SecureTokenStorage _tokenStorage;
  final RetryPolicy _retryPolicy;

  Dio get dio => _dio;

  static Future<ApiClient> create({
    SecureTokenStorage? tokenStorage,
    RetryPolicy? retryPolicy,
    CookieJar? cookieJar,
    Future<void> Function()? onSessionInvalidated,
  }) async {
    final resolvedTokenStorage = tokenStorage ?? SecureTokenStorage();
    final resolvedCookieJar = cookieJar ?? await _createPersistCookieJar();

    final dio = Dio(
      BaseOptions(
        baseUrl: ApiConfig.baseUrl,
        connectTimeout: ApiConfig.connectTimeout,
        receiveTimeout: ApiConfig.receiveTimeout,
        sendTimeout: ApiConfig.sendTimeout,
        headers: const {
          'Accept': 'application/json',
          'Content-Type': 'application/json',
        },
      ),
    );

    dio.interceptors.add(CookieManager(resolvedCookieJar));
    dio.interceptors.add(AuthInterceptor(tokenStorage: resolvedTokenStorage));
    dio.interceptors.add(
      CookieRefreshInterceptor(
        dio: dio,
        tokenStorage: resolvedTokenStorage,
        cookieJar: resolvedCookieJar,
        onSessionInvalidated: onSessionInvalidated,
      ),
    );
    dio.interceptors.add(ErrorInterceptor());

    return ApiClient._(
      dio: dio,
      cookieJar: resolvedCookieJar,
      tokenStorage: resolvedTokenStorage,
      retryPolicy: retryPolicy,
    );
  }

  static Future<CookieJar> _createPersistCookieJar() async {
    final appDir = await getApplicationDocumentsDirectory();
    return PersistCookieJar(
      storage: FileStorage('${appDir.path}/.cookies/'),
    );
  }

  Future<Response<T>> get<T>(
    String path, {
    Map<String, dynamic>? queryParameters,
    Options? options,
  }) {
    return _withRetry(
      () => _dio.get<T>(
        path,
        queryParameters: queryParameters,
        options: options,
      ),
    );
  }

  Future<Response<T>> post<T>(
    String path, {
    Object? data,
    Map<String, dynamic>? queryParameters,
    Options? options,
  }) {
    return _withRetry(
      () => _dio.post<T>(
        path,
        data: data,
        queryParameters: queryParameters,
        options: options,
      ),
    );
  }

  Future<Response<T>> put<T>(
    String path, {
    Object? data,
    Map<String, dynamic>? queryParameters,
    Options? options,
  }) {
    return _withRetry(
      () => _dio.put<T>(
        path,
        data: data,
        queryParameters: queryParameters,
        options: options,
      ),
    );
  }

  Future<Response<T>> patch<T>(
    String path, {
    Object? data,
    Map<String, dynamic>? queryParameters,
    Options? options,
  }) {
    return _withRetry(
      () => _dio.patch<T>(
        path,
        data: data,
        queryParameters: queryParameters,
        options: options,
      ),
    );
  }

  void updateBaseUrl(String baseUrl) {
    _dio.options.baseUrl = baseUrl;
  }

  Future<Response<T>> delete<T>(
    String path, {
    Object? data,
    Map<String, dynamic>? queryParameters,
    Options? options,
  }) {
    return _withRetry(
      () => _dio.delete<T>(
        path,
        data: data,
        queryParameters: queryParameters,
        options: options,
      ),
    );
  }

  Future<void> clearSession() async {
    await _tokenStorage.clear();
    await _cookieJar.deleteAll();
  }

  Future<void> clearCookies() {
    return _cookieJar.deleteAll();
  }

  Future<Response<T>> _withRetry<T>(
    Future<Response<T>> Function() request,
  ) async {
    var attempt = 0;
    while (true) {
      attempt++;
      try {
        return await request();
      } on DioException catch (exception) {
        if (!_retryPolicy.shouldRetry(exception, attempt)) {
          rethrow;
        }
        await _retryPolicy.waitBeforeRetry(attempt);
      }
    }
  }
}
