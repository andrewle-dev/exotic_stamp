import 'package:dio/dio.dart';

import '../storage/secure_token_storage.dart';

/// Attaches in-memory Bearer access token. Supports [skipAuthKey].
class AuthInterceptor extends Interceptor {
  AuthInterceptor({required SecureTokenStorage tokenStorage})
      : _tokenStorage = tokenStorage;

  final SecureTokenStorage _tokenStorage;

  static const skipAuthKey = 'skip_auth';

  @override
  void onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) {
    if (options.extra[skipAuthKey] == true) {
      return handler.next(options);
    }

    final token = _tokenStorage.readAccessToken();
    if (token != null && token.isNotEmpty) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }
}
