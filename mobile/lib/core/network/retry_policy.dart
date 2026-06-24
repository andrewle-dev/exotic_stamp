import 'package:dio/dio.dart';

/// Retry policy for transient network failures.
class RetryPolicy {
  const RetryPolicy({
    this.maxAttempts = 2,
    this.retryDelay = const Duration(milliseconds: 400),
  });

  final int maxAttempts;
  final Duration retryDelay;

  bool shouldRetry(DioException exception, int attempt) {
    if (attempt >= maxAttempts) {
      return false;
    }

    return exception.type == DioExceptionType.connectionError ||
        exception.type == DioExceptionType.connectionTimeout ||
        exception.type == DioExceptionType.receiveTimeout;
  }

  Future<void> waitBeforeRetry(int attempt) async {
    await Future<void>.delayed(retryDelay * attempt);
  }
}
