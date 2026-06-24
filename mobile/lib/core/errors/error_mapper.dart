import 'package:dio/dio.dart';

import 'failure.dart';

/// Maps backend error envelopes and transport errors into [Failure].
class ErrorMapper {
  const ErrorMapper();

  static const backendCodeMap = <String, FailureCode>{
    'INVALID_CREDENTIALS': FailureCode.invalidCredentials,
    'TOKEN_EXPIRED': FailureCode.tokenExpired,
    'EMAIL_TAKEN': FailureCode.emailTaken,
    'VALIDATION_ERROR': FailureCode.validationError,
    'OTP_INVALID': FailureCode.validationError,
    'UNAUTHORIZED': FailureCode.unauthorized,
    'INVALID_TOKEN': FailureCode.unauthorized,
    'STAMP_ALREADY_COLLECTED': FailureCode.stampDuplicate,
    'STAMP_DUPLICATE': FailureCode.stampDuplicate,
    'SCAN_KEY_NOT_FOUND': FailureCode.nfcInvalid,
    'SCAN_PAYLOAD_INVALID': FailureCode.nfcInvalid,
    'INVALID_SCAN_METHOD': FailureCode.nfcInvalid,
    'NFC_INVALID': FailureCode.nfcInvalid,
    'SCAN_KEY_INACTIVE': FailureCode.qrExpired,
    'QR_EXPIRED': FailureCode.qrExpired,
    'GPS_OUT_OF_RANGE': FailureCode.gpsOutsideRange,
    'GPS_OUTSIDE_RANGE': FailureCode.gpsOutsideRange,
    'STATION_INACTIVE': FailureCode.stationInactive,
    'CAMPAIGN_NOT_ACTIVE': FailureCode.campaignInactive,
    'CAMPAIGN_ARCHIVED': FailureCode.campaignInactive,
    'CAMPAIGN_INACTIVE': FailureCode.campaignInactive,
    'REDEEM_NOT_SUPPORTED': FailureCode.redeemNotSupported,
    'NETWORK_ERROR': FailureCode.networkError,
  };

  Failure fromDioException(DioException exception) {
    if (_isNetworkFailure(exception)) {
      return const Failure(
        code: FailureCode.networkError,
        message: 'Không thể kết nối máy chủ. Kiểm tra mạng và thử lại.',
      );
    }

    final response = exception.response;
    if (response?.data is Map<String, dynamic>) {
      return fromJson(
        response!.data as Map<String, dynamic>,
        statusCode: response.statusCode,
      );
    }

    return Failure(
      code: FailureCode.unknown,
      message: exception.message ?? 'Đã xảy ra lỗi không xác định.',
      statusCode: response?.statusCode,
    );
  }

  Failure fromJson(
    Map<String, dynamic> json, {
    int? statusCode,
  }) {
    final backendCode = json['code'] as String?;
    final message = (json['message'] as String?)?.trim();
    final resolvedCode = backendCode != null
        ? backendCodeMap[backendCode] ?? FailureCode.unknown
        : FailureCode.unknown;

    return Failure(
      code: resolvedCode,
      message: message ?? _defaultMessage(resolvedCode),
      statusCode: statusCode ?? json['status'] as int?,
      backendCode: backendCode,
    );
  }

  bool _isNetworkFailure(DioException exception) {
    return exception.type == DioExceptionType.connectionError ||
        exception.type == DioExceptionType.connectionTimeout ||
        exception.type == DioExceptionType.receiveTimeout ||
        exception.type == DioExceptionType.sendTimeout;
  }

  String _defaultMessage(FailureCode code) {
    return switch (code) {
      FailureCode.invalidCredentials =>
        'Email/tên đăng nhập hoặc mật khẩu không đúng.',
      FailureCode.tokenExpired => 'Phiên đăng nhập đã hết hạn.',
      FailureCode.emailTaken => 'Email này đã được đăng ký.',
      FailureCode.validationError =>
        'Thông tin chưa hợp lệ. Vui lòng kiểm tra lại.',
      FailureCode.unauthorized => 'Bạn cần đăng nhập để tiếp tục.',
      FailureCode.stampDuplicate => 'Bạn đã thu thập stamp tại ga này.',
      FailureCode.nfcInvalid => 'Thẻ NFC hoặc mã quét không hợp lệ.',
      FailureCode.qrExpired => 'Mã QR đã hết hạn hoặc không còn hiệu lực.',
      FailureCode.gpsOutsideRange =>
        'Bạn cần ở trong phạm vi ga để thu thập stamp.',
      FailureCode.stationInactive => 'Ga này hiện không hoạt động.',
      FailureCode.campaignInactive => 'Chiến dịch hiện không khả dụng.',
      FailureCode.redeemNotSupported =>
        'Đổi voucher trong app chưa được hỗ trợ.',
      FailureCode.networkError =>
        'Không thể kết nối máy chủ. Kiểm tra mạng và thử lại.',
      FailureCode.unknown => 'Đã xảy ra lỗi không xác định.',
    };
  }
}
