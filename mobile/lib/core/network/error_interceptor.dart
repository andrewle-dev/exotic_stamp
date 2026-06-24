import 'package:dio/dio.dart';

import '../errors/error_mapper.dart';
import '../errors/failure.dart';

/// Converts Dio errors into typed [Failure] instances on the response chain.
class ErrorInterceptor extends Interceptor {
  ErrorInterceptor({ErrorMapper? errorMapper})
      : _errorMapper = errorMapper ?? const ErrorMapper();

  final ErrorMapper _errorMapper;

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    final failure = _errorMapper.fromDioException(err);
    handler.reject(
      DioException(
        requestOptions: err.requestOptions,
        response: err.response,
        type: err.type,
        error: failure,
        message: failure.message,
      ),
    );
  }

  static Failure? failureFrom(DioException exception) {
    final error = exception.error;
    if (error is Failure) {
      return error;
    }
    return null;
  }
}
