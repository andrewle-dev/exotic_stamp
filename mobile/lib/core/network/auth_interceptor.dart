import 'package:dio/dio.dart';

import '../storage/secure_token_storage.dart';

/// Attaches the Bearer access token to outgoing requests.
class AuthInterceptor extends Interceptor {
  AuthInterceptor({required SecureTokenStorage tokenStorage})
      : _tokenStorage = tokenStorage;

  static const skipAuthKey = 'skip_auth';

  final SecureTokenStorage _tokenStorage;

  @override
  Future<void> onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    if (options.extra[skipAuthKey] == true) {
      return handler.next(options);
    }

    final token = await _tokenStorage.readAccessToken();
    if (token != null && token.isNotEmpty) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }
}
